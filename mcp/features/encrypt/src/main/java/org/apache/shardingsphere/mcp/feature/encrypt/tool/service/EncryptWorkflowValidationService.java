/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationReport;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationSection;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowValidationCoordinator;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowValidationSupport;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Encrypt workflow validation service.
 */
public final class EncryptWorkflowValidationService {
    
    private final WorkflowValidationCoordinator validationCoordinator = new WorkflowValidationCoordinator();
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final WorkflowContextStore contextStore;
    
    private final EncryptRuleInspectionService ruleInspectionService;
    
    public EncryptWorkflowValidationService() {
        this(null, new EncryptRuleInspectionService());
    }
    
    EncryptWorkflowValidationService(final WorkflowContextStore contextStore, final EncryptRuleInspectionService ruleInspectionService) {
        this.contextStore = contextStore;
        this.ruleInspectionService = ruleInspectionService;
    }
    
    /**
     * Validate workflow artifacts.
     *
     * @param requestContext request context
     * @param sessionId session id
     * @param planId plan identifier
     * @return validation payload
     */
    public Map<String, Object> validate(final MCPFeatureContext requestContext, final String sessionId, final String planId) {
        return validationCoordinator.validate(requestContext, sessionId, planId, contextStore, new EncryptValidationScenario());
    }
    
    private EncryptWorkflowState getEncryptWorkflowState(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof EncryptWorkflowState ? (EncryptWorkflowState) snapshot.getFeatureData() : new EncryptWorkflowState();
    }
    
    private ValidationSection validateDdl(final MCPFeatureContext requestContext, final WorkflowContextSnapshot snapshot, final EncryptWorkflowState workflowState,
                                          final List<Map<String, Object>> encryptRules, final ValidationReport validationReport) {
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            return new ValidationSection(WorkflowLifecycle.STATUS_SKIPPED, List.of(), "Encrypt drop does not validate physical cleanup in V1.");
        }
        if (null == workflowState.getDerivedColumnPlan()) {
            return new ValidationSection(WorkflowLifecycle.STATUS_SKIPPED, List.of(), "No derived column plan is available for validation.");
        }
        Optional<Map<String, Object>> actualRule = findEncryptRule(snapshot, encryptRules);
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.DDL_STATE_MISMATCH, "ddl", createExpectedDerivedColumnSummary(workflowState), "",
                    "Encrypt rule is missing, so derived column mappings cannot be validated.", "Create or alter the encrypt rule again."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Encrypt rule is missing.");
        }
        List<Map<String, Object>> mismatches = new LinkedList<>();
        addDerivedColumnMismatch(mismatches, "cipher_column", workflowState.getDerivedColumnPlan().getCipherColumnName(),
                WorkflowRuleValueUtils.findRuleValue(actualRule.get(), "cipher_column"), "Cipher column mapping does not match.");
        addDerivedColumnMismatch(mismatches, "assisted_query_column",
                workflowState.getDerivedColumnPlan().isAssistedQueryColumnRequired() ? workflowState.getDerivedColumnPlan().getAssistedQueryColumnName() : "",
                WorkflowRuleValueUtils.findRuleValue(actualRule.get(), "assisted_query_column", "assisted_query"), "Assisted-query column mapping does not match.");
        addDerivedColumnMismatch(mismatches, "like_query_column",
                workflowState.getDerivedColumnPlan().isLikeQueryColumnRequired() ? workflowState.getDerivedColumnPlan().getLikeQueryColumnName() : "",
                WorkflowRuleValueUtils.findRuleValue(actualRule.get(), "like_query_column", "like_query"), "LIKE-query column mapping does not match.");
        addMissingPhysicalDerivedColumnMismatches(requestContext, snapshot, workflowState, mismatches);
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createDdlEvidence(actualRule.get(), requestContext, snapshot, workflowState),
                    "Derived column mappings do not match the plan.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, createDdlEvidence(actualRule.get(), requestContext, snapshot, workflowState),
                "Derived column mappings match the planned physical layout.");
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final EncryptWorkflowState workflowState,
                                            final List<Map<String, Object>> encryptRules, final ValidationReport validationReport) {
        Optional<Map<String, Object>> actualRule = findEncryptRule(snapshot, encryptRules);
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, List.of(), "Encrypt rule has been removed.");
            }
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no encrypt rule", String.valueOf(actualRule.get()),
                    "Encrypt rule still exists after drop.", "Drop the encrypt rule again or investigate the failure."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(), "Encrypt rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Encrypt rule is missing.", "Create or alter the encrypt rule again."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Encrypt rule is missing.");
        }
        List<Map<String, Object>> mismatches = new LinkedList<>();
        addAlgorithmTypeMismatch(mismatches, "encryptor_type", snapshot.getRequest().getAlgorithmType(),
                WorkflowRuleValueUtils.findRuleValue(actualRule.get(), "encryptor_type"), "Encrypt algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "assisted_query_type",
                Boolean.TRUE.equals(workflowState.getOptions().getRequiresEqualityFilter()) ? workflowState.getOptions().getAssistedQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.findRuleValue(actualRule.get(), "assisted_query_type", "assisted_query_encryptor_type"), "Assisted-query algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "like_query_type",
                Boolean.TRUE.equals(workflowState.getOptions().getRequiresLikeQuery()) ? workflowState.getOptions().getLikeQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.findRuleValue(actualRule.get(), "like_query_type", "like_query_encryptor_type"), "LIKE-query algorithm type does not match.");
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(), "Encrypt algorithm configuration does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, actualRule.get(), "Encrypt rule matches the planned algorithm and mapping.");
    }
    
    private ValidationSection validateSqlExecutability(final MCPFeatureContext requestContext, final String sessionId,
                                                       final WorkflowContextSnapshot snapshot, final EncryptWorkflowState workflowState,
                                                       final ValidationReport validationReport) {
        List<String> validationSqls = createValidationSqls(snapshot, workflowState);
        for (String each : validationSqls) {
            try {
                requestContext.getExecutionFacade().execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), each, 1, 0));
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "sql_executability", each, ex.getMessage(),
                        "Validation SQL cannot be executed from the logical view.", "Inspect rule state and logical metadata."));
                return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, each, ex.getMessage());
            }
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, validationSqls, "Validation SQLs are executable from the logical view.");
    }
    
    private void addMissingPhysicalDerivedColumnMismatches(final MCPFeatureContext requestContext, final WorkflowContextSnapshot snapshot,
                                                           final EncryptWorkflowState workflowState, final List<Map<String, Object>> mismatches) {
        Set<String> expectedColumnNames = createExpectedDerivedColumnNames(workflowState);
        try {
            Set<String> actualColumnNames = requestContext.getQueryFacade().queryInformationSchemaColumnNames(
                    snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), snapshot.getRequest().getTable(), expectedColumnNames);
            for (String each : expectedColumnNames) {
                if (!actualColumnNames.contains(each)) {
                    mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.DDL_STATE_MISMATCH, "ddl", each, actualColumnNames.toString(),
                            "Derived column is not visible from Proxy information_schema.", "Create the physical derived columns again."));
                }
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.DDL_STATE_MISMATCH, "ddl", createExpectedDerivedColumnSummary(workflowState), ex.getMessage(),
                    "Failed to verify derived columns from Proxy information_schema.", "Inspect Proxy metadata access or verify the physical columns manually."));
        }
    }
    
    private Set<String> createExpectedDerivedColumnNames(final EncryptWorkflowState workflowState) {
        Set<String> result = new LinkedHashSet<>(4, 1F);
        addIfPresent(result, workflowState.getDerivedColumnPlan().getCipherColumnName());
        if (workflowState.getDerivedColumnPlan().isAssistedQueryColumnRequired()) {
            addIfPresent(result, workflowState.getDerivedColumnPlan().getAssistedQueryColumnName());
        }
        if (workflowState.getDerivedColumnPlan().isLikeQueryColumnRequired()) {
            addIfPresent(result, workflowState.getDerivedColumnPlan().getLikeQueryColumnName());
        }
        return result;
    }
    
    private void addIfPresent(final Set<String> target, final String value) {
        String actualValue = WorkflowSqlUtils.trimToEmpty(value);
        if (!actualValue.isEmpty()) {
            target.add(actualValue);
        }
    }
    
    private Map<String, Object> createDdlEvidence(final Map<String, Object> actualRule, final MCPFeatureContext requestContext,
                                                  final WorkflowContextSnapshot snapshot, final EncryptWorkflowState workflowState) {
        Map<String, Object> result = new LinkedHashMap<>(actualRule);
        try {
            result.put("physical_columns", requestContext.getQueryFacade().queryInformationSchemaColumnNames(
                    snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), snapshot.getRequest().getTable(), createExpectedDerivedColumnNames(workflowState)));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ignored) {
            // CHECKSTYLE:ON
        }
        return result;
    }
    
    private List<String> createValidationSqls(final WorkflowContextSnapshot snapshot, final EncryptWorkflowState workflowState) {
        WorkflowSqlUtils.checkSafeIdentifier("table", snapshot.getRequest().getTable());
        WorkflowSqlUtils.checkSafeIdentifier("column", snapshot.getRequest().getColumn());
        List<String> result = new LinkedList<>();
        result.add(String.format("SELECT %s FROM %s", snapshot.getRequest().getColumn(), snapshot.getRequest().getTable()));
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            return result;
        }
        if (Boolean.TRUE.equals(workflowState.getOptions().getRequiresEqualityFilter())) {
            result.add(String.format("SELECT %s FROM %s WHERE %s = 'sample'", snapshot.getRequest().getColumn(),
                    snapshot.getRequest().getTable(), snapshot.getRequest().getColumn()));
        }
        if (Boolean.TRUE.equals(workflowState.getOptions().getRequiresLikeQuery())) {
            result.add(String.format("SELECT %s FROM %s WHERE %s LIKE 'sample%%'", snapshot.getRequest().getColumn(),
                    snapshot.getRequest().getTable(), snapshot.getRequest().getColumn()));
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findEncryptRule(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> encryptRules) {
        return encryptRules.stream().filter(each -> snapshot.getRequest().getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.findRuleValue(each, "logic_column", "column"))).findFirst();
    }
    
    private void addDerivedColumnMismatch(final List<Map<String, Object>> mismatches, final String fieldName, final String expected, final String actual, final String impact) {
        if (matchesValue(expected, actual)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.DDL_STATE_MISMATCH, "ddl", formatFieldValue(fieldName, expected), formatFieldValue(fieldName, actual), impact,
                "Recheck DDL and encrypt rule state."));
    }
    
    private void addAlgorithmTypeMismatch(final List<Map<String, Object>> mismatches, final String fieldName, final String expected, final String actual, final String impact) {
        if (matchesValue(expected, actual)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue(fieldName, expected), formatFieldValue(fieldName, actual), impact,
                "Re-apply the intended encrypt rule."));
    }
    
    private boolean matchesValue(final String expected, final String actual) {
        return WorkflowSqlUtils.trimToEmpty(expected).equalsIgnoreCase(WorkflowSqlUtils.trimToEmpty(actual));
    }
    
    private String formatFieldValue(final String fieldName, final String value) {
        return String.format("%s=%s", fieldName, WorkflowSqlUtils.trimToEmpty(value));
    }
    
    private String createExpectedDerivedColumnSummary(final EncryptWorkflowState workflowState) {
        return String.format("cipher=%s, assisted_query=%s, like_query=%s", workflowState.getDerivedColumnPlan().getCipherColumnName(),
                WorkflowSqlUtils.trimToEmpty(workflowState.getDerivedColumnPlan().getAssistedQueryColumnName()),
                WorkflowSqlUtils.trimToEmpty(workflowState.getDerivedColumnPlan().getLikeQueryColumnName()));
    }
    
    private final class EncryptValidationScenario implements WorkflowValidationCoordinator.WorkflowValidationScenario<EncryptWorkflowState> {
        
        @Override
        public EncryptWorkflowState getWorkflowState(final WorkflowContextSnapshot snapshot) {
            return EncryptWorkflowValidationService.this.getEncryptWorkflowState(snapshot);
        }
        
        @Override
        public List<Map<String, Object>> queryRules(final MCPFeatureContext requestContext, final WorkflowContextSnapshot snapshot) {
            return ruleInspectionService.queryEncryptRules(requestContext, snapshot.getRequest().getDatabase(), snapshot.getRequest().getTable());
        }
        
        @Override
        public ValidationSection validateDdl(final MCPFeatureContext requestContext, final String sessionId, final WorkflowContextSnapshot snapshot,
                                             final EncryptWorkflowState workflowState, final List<Map<String, Object>> existingRules,
                                             final ValidationReport validationReport) {
            return EncryptWorkflowValidationService.this.validateDdl(requestContext, snapshot, workflowState, existingRules, validationReport);
        }
        
        @Override
        public ValidationSection validateRules(final MCPFeatureContext requestContext, final String sessionId, final WorkflowContextSnapshot snapshot,
                                               final EncryptWorkflowState workflowState, final List<Map<String, Object>> existingRules,
                                               final ValidationReport validationReport) {
            return EncryptWorkflowValidationService.this.validateRules(snapshot, workflowState, existingRules, validationReport);
        }
        
        @Override
        public ValidationSection validateSqlExecutability(final MCPFeatureContext requestContext, final String sessionId,
                                                          final WorkflowContextSnapshot snapshot, final EncryptWorkflowState workflowState,
                                                          final ValidationReport validationReport) {
            return EncryptWorkflowValidationService.this.validateSqlExecutability(requestContext, sessionId, snapshot, workflowState, validationReport);
        }
    }
}
