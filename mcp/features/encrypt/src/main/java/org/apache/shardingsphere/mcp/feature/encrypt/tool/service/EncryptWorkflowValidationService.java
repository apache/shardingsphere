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

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Encrypt workflow validation service.
 */
public final class EncryptWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final EncryptRuleInspectionService ruleInspectionService = new EncryptRuleInspectionService();
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    
    /**
     * Validate workflow artifacts.
     *
     * @param workflowSessionContext workflow session context
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade
     * @param executionFacade execution facade
     * @param sessionId session id
     * @param snapshot workflow snapshot
     * @return validation payload
     */
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        Map<String, Object> rejectedResponse = validationSupport.checkValidatePreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        ValidationReport validationReport = createValidationReport(snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId);
        snapshot.setValidationReport(validationReport);
        return validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId));
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                                                    final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        ValidationReport result = new ValidationReport();
        EncryptWorkflowRequest request = getWorkflowRequest(snapshot);
        EncryptWorkflowState workflowState = getWorkflowState(snapshot);
        List<Map<String, Object>> encryptRules = ruleInspectionService.queryEncryptRules(queryFacade, request.getDatabase(), request.getTable());
        result.setDdlValidation(validateDdl(snapshot, workflowState, encryptRules, result));
        result.setRuleValidation(validateRules(snapshot, request, encryptRules, result));
        result.setLogicalMetadataValidation(validationSupport.validateLogicalMetadata(snapshot, metadataQueryFacade, result));
        result.setSqlExecutabilityValidation(validateSqlExecutability(executionFacade, sessionId, snapshot, request, result));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getDdlValidation(), result.getRuleValidation(),
                result.getLogicalMetadataValidation(), result.getSqlExecutabilityValidation()));
        return result;
    }
    
    private EncryptWorkflowRequest getWorkflowRequest(final WorkflowContextSnapshot snapshot) {
        if (snapshot.getRequest() instanceof EncryptWorkflowRequest) {
            return (EncryptWorkflowRequest) snapshot.getRequest();
        }
        final EncryptWorkflowRequest result = EncryptWorkflowRequest.merge(snapshot.getRequest(), null);
        return null == result ? new EncryptWorkflowRequest() : result;
    }
    
    private EncryptWorkflowState getWorkflowState(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof EncryptWorkflowState ? (EncryptWorkflowState) snapshot.getFeatureData() : new EncryptWorkflowState();
    }
    
    private ValidationSection validateDdl(final WorkflowContextSnapshot snapshot, final EncryptWorkflowState workflowState,
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
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "cipher_column"), "Cipher column mapping does not match.");
        addDerivedColumnMismatch(mismatches, "assisted_query_column",
                workflowState.getDerivedColumnPlan().isAssistedQueryColumnRequired() ? workflowState.getDerivedColumnPlan().getAssistedQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "assisted_query_column"), "Assisted-query column mapping does not match.");
        addDerivedColumnMismatch(mismatches, "like_query_column",
                workflowState.getDerivedColumnPlan().isLikeQueryColumnRequired() ? workflowState.getDerivedColumnPlan().getLikeQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "like_query_column"), "LIKE-query column mapping does not match.");
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(),
                    "Derived column mappings do not match the plan.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, actualRule.get(),
                "Derived column mappings match the encrypt rule exposed by Proxy logical metadata.");
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot,
                                            final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules, final ValidationReport validationReport) {
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
        addAlgorithmTypeMismatch(mismatches, "encryptor_type", request.getAlgorithmType(),
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "encryptor_type"), "Encrypt algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "assisted_query_type",
                Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "assisted_query_type"), "Assisted-query algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "like_query_type",
                Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "like_query_type"), "LIKE-query algorithm type does not match.");
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, actualRule.get(), "Encrypt algorithm configuration does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, actualRule.get(), "Encrypt rule matches the planned algorithm and mapping.");
    }
    
    private ValidationSection validateSqlExecutability(final MCPFeatureExecutionFacade executionFacade, final String sessionId, final WorkflowContextSnapshot snapshot,
                                                       final EncryptWorkflowRequest request, final ValidationReport validationReport) {
        return validationSupport.validateSqlExecutability(executionFacade, sessionId, snapshot, validationReport,
                createValidationSqls(snapshot, request), "Validation SQLs are executable from the logical view.");
    }
    
    private List<String> createValidationSqls(final WorkflowContextSnapshot snapshot, final EncryptWorkflowRequest request) {
        List<String> result = new LinkedList<>();
        result.add(validationSupport.createProjectionValidationSql(snapshot));
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            return result;
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            result.add(String.format("SELECT %s FROM %s WHERE %s = 'sample'", snapshot.getRequest().getColumn(),
                    snapshot.getRequest().getTable(), snapshot.getRequest().getColumn()));
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            result.add(String.format("SELECT %s FROM %s WHERE %s LIKE 'sample%%'", snapshot.getRequest().getColumn(),
                    snapshot.getRequest().getTable(), snapshot.getRequest().getColumn()));
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findEncryptRule(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> encryptRules) {
        return encryptRules.stream().filter(each -> snapshot.getRequest().getColumn().equalsIgnoreCase(WorkflowRuleValueUtils.getRuleValue(each, "logic_column"))).findFirst();
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
        return expected.equalsIgnoreCase(actual);
    }
    
    private String formatFieldValue(final String fieldName, final String value) {
        return String.format("%s=%s", fieldName, value);
    }
    
    private String createExpectedDerivedColumnSummary(final EncryptWorkflowState workflowState) {
        return String.format("cipher=%s, assisted_query=%s, like_query=%s", workflowState.getDerivedColumnPlan().getCipherColumnName(),
                workflowState.getDerivedColumnPlan().getAssistedQueryColumnName(), workflowState.getDerivedColumnPlan().getLikeQueryColumnName());
    }
}
