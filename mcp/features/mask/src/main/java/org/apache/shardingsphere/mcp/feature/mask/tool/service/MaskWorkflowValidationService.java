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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationReport;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationSection;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowValidationUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mask workflow validation service.
 */
public final class MaskWorkflowValidationService {
    
    private final WorkflowContextStore contextStore;
    
    private final MaskRuleInspectionService ruleInspectionService;
    
    public MaskWorkflowValidationService() {
        this(WorkflowContextStore.getInstance(), new MaskRuleInspectionService());
    }
    
    MaskWorkflowValidationService(final WorkflowContextStore contextStore, final MaskRuleInspectionService ruleInspectionService) {
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
        WorkflowContextSnapshot snapshot = contextStore.getRequired(planId);
        Map<String, Object> rejectedResponse = WorkflowValidationUtils.checkValidatePreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        ValidationReport validationReport = new ValidationReport();
        snapshot.setValidationReport(validationReport);
        List<Map<String, Object>> maskRules = ruleInspectionService.queryMaskRules(requestContext, snapshot.getRequest().getDatabase(), snapshot.getRequest().getTable());
        MCPMetadataQueryFacade metadataQueryService = requestContext.getMetadataQueryFacade();
        validationReport.setDdlValidation(new ValidationSection("skipped", List.of(), "Mask workflows do not require physical DDL validation."));
        validationReport.setRuleValidation(validateRules(snapshot, maskRules, validationReport));
        validationReport.setLogicalMetadataValidation(validateLogicalMetadata(snapshot, metadataQueryService, validationReport));
        validationReport.setSqlExecutabilityValidation(validateSqlExecutability(requestContext, sessionId, snapshot, validationReport));
        validationReport.setOverallStatus(WorkflowValidationUtils.resolveOverallStatus(validationReport.getRuleValidation(),
                validationReport.getLogicalMetadataValidation(), validationReport.getSqlExecutabilityValidation()));
        String validationStatus = WorkflowValidationUtils.resolveValidationStatus(validationReport);
        snapshot.setValidationReport(validationReport);
        if (null != snapshot.getInteractionPlan()) {
            snapshot.getInteractionPlan().setCurrentStep("validated");
        }
        snapshot.setStatus(validationStatus);
        contextStore.save(snapshot);
        List<Map<String, Object>> issues = WorkflowValidationUtils.createValidationIssues(validationReport);
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("status", validationStatus);
        result.put("issues", issues);
        result.putAll(validationReport.toMap());
        return result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> maskRules, final ValidationReport validationReport) {
        Optional<Map<String, Object>> actualRule = maskRules.stream()
                .filter(each -> snapshot.getRequest().getColumn().equalsIgnoreCase(findRuleValue(each, "column", "logic_column"))).findFirst();
        if (isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection("passed", List.of(), "Mask rule has been removed.");
            }
            validationReport.getMismatches().add(WorkflowValidationUtils.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no mask rule", String.valueOf(actualRule.get()),
                    "Mask rule still exists after drop.", "Drop the mask rule again or investigate the failure."));
            return new ValidationSection("failed", actualRule.get(), "Mask rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(WorkflowValidationUtils.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Mask rule is missing.", "Create or alter the mask rule again."));
            return new ValidationSection("failed", List.of(), "Mask rule is missing.");
        }
        String actualAlgorithmType = findRuleValue(actualRule.get(), "algorithm_type", "mask_algorithm");
        if (!WorkflowSqlUtils.trimToEmpty(snapshot.getRequest().getAlgorithmType()).equalsIgnoreCase(actualAlgorithmType)) {
            validationReport.getMismatches().add(WorkflowValidationUtils.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getAlgorithmType(), actualAlgorithmType,
                    "Mask algorithm type does not match.", "Re-apply the intended mask rule."));
            return new ValidationSection("failed", actualRule.get(), "Mask algorithm type does not match.");
        }
        return new ValidationSection("passed", actualRule.get(), "Mask rule matches the planned algorithm.");
    }
    
    private ValidationSection validateLogicalMetadata(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryService,
                                                      final ValidationReport validationReport) {
        if (metadataQueryService.queryTableColumn(
                snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), snapshot.getRequest().getTable(), snapshot.getRequest().getColumn()).isPresent()) {
            return new ValidationSection("passed", Map.of("table", snapshot.getRequest().getTable(), "column", snapshot.getRequest().getColumn()),
                    "Logical table and column are still visible from Proxy metadata.");
        }
        validationReport.getMismatches().add(WorkflowValidationUtils.createMismatch(WorkflowIssueCode.LOGICAL_METADATA_MISMATCH, "logical_metadata", snapshot.getRequest().getColumn(), "",
                "Logical column is not visible from Proxy metadata.", "Refresh metadata or investigate the logical schema."));
        return new ValidationSection("failed", List.of(), "Logical column is not visible from Proxy metadata.");
    }
    
    private ValidationSection validateSqlExecutability(final MCPFeatureContext requestContext, final String sessionId,
                                                       final WorkflowContextSnapshot snapshot, final ValidationReport validationReport) {
        String validationSql = createValidationSql(snapshot);
        try {
            requestContext.getExecutionFacade().execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), validationSql, 1, 0));
            return new ValidationSection("passed", List.of(validationSql), "Validation SQL is executable from the logical view.");
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            validationReport.getMismatches().add(WorkflowValidationUtils.createMismatch(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "sql_executability", validationSql, ex.getMessage(),
                    "Validation SQL cannot be executed from the logical view.", "Inspect rule state and logical metadata."));
            return new ValidationSection("failed", validationSql, ex.getMessage());
        }
    }
    
    private String createValidationSql(final WorkflowContextSnapshot snapshot) {
        WorkflowSqlUtils.checkSafeIdentifier("table", snapshot.getRequest().getTable());
        WorkflowSqlUtils.checkSafeIdentifier("column", snapshot.getRequest().getColumn());
        return String.format("SELECT %s FROM %s", snapshot.getRequest().getColumn(), snapshot.getRequest().getTable());
    }
    
    private boolean isDropWorkflow(final WorkflowContextSnapshot snapshot) {
        return "drop".equalsIgnoreCase(resolveOperationType(snapshot));
    }
    
    private String resolveOperationType(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getClarifiedIntent() ? "" : WorkflowSqlUtils.trimToEmpty(snapshot.getClarifiedIntent().getOperationType());
    }
    
    private String findRuleValue(final Map<String, Object> rule, final String... keys) {
        for (String each : keys) {
            Object value = rule.get(each);
            String actualValue = null == value ? "" : WorkflowSqlUtils.trimToEmpty(String.valueOf(value));
            if (!actualValue.isEmpty()) {
                return actualValue;
            }
        }
        return "";
    }
    
}
