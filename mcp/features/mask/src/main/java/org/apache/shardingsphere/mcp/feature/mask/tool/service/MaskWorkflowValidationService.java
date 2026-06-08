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

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleWorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactMaskUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mask workflow validation service.
 */
public final class MaskWorkflowValidationService implements MCPWorkflowRuntimeHandler {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final MaskRuleInspectionService ruleInspectionService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public MaskWorkflowValidationService() {
        ruleInspectionService = new MaskRuleInspectionService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport();
    }
    
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        Map<String, Object> rejectedResponse = validationSupport.checkValidatePreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        ValidationReport validationReport = createValidationReport(snapshot, queryFacade);
        snapshot.setValidationReport(validationReport);
        return validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        String databaseType = queryFacade.getDatabaseType(snapshot.getRequest().getDatabase());
        List<Map<String, Object>> maskRules = ruleInspectionService.queryMaskRules(queryFacade, snapshot.getRequest().getDatabase(), snapshot.getRequest().getTable());
        result.setRuleValidation(validateRules(snapshot, maskRules, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> maskRules,
                                            final ValidationReport validationReport, final String databaseType) {
        Optional<RuleWorkflowFeatureData> ruleFeatureData = getRuleFeatureData(snapshot);
        if (ruleFeatureData.isPresent()) {
            return validateExpectedRules(snapshot, ruleFeatureData.get().getExpectedRules(), maskRules, validationReport, databaseType);
        }
        Optional<Map<String, Object>> actualRule = maskRules.stream()
                .filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, snapshot.getRequest().getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "column"))).findFirst();
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, List.of(), "Mask rule has been removed.");
            }
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no mask rule",
                    String.valueOf(createMaskedRules(snapshot, List.of(actualRule.get())).get(0)),
                    "Mask rule still exists after drop.", "Drop the mask rule again or investigate the failure."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, List.of(actualRule.get())).get(0), "Mask rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Mask rule is missing.", "Create or alter the mask rule again."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Mask rule is missing.");
        }
        String actualAlgorithmType = WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "algorithm_type");
        if (!snapshot.getRequest().getAlgorithmType().equalsIgnoreCase(actualAlgorithmType)) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getAlgorithmType(), actualAlgorithmType,
                    "Mask algorithm type does not match.", "Re-apply the intended mask rule."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, List.of(actualRule.get())).get(0), "Mask algorithm type does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, createMaskedRules(snapshot, List.of(actualRule.get())).get(0), "Mask rule matches the planned algorithm.");
    }
    
    private Optional<RuleWorkflowFeatureData> getRuleFeatureData(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof RuleWorkflowFeatureData ? Optional.of((RuleWorkflowFeatureData) snapshot.getFeatureData()) : Optional.empty();
    }
    
    private ValidationSection validateExpectedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules, final List<Map<String, Object>> actualRules,
                                                    final ValidationReport validationReport, final String databaseType) {
        List<Map<String, Object>> mismatches = createExpectedRuleMismatches(snapshot, expectedRules, actualRules, databaseType);
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, actualRules), "Mask table rule state does not match the planned state.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, createMaskedRules(snapshot, actualRules), "Mask table rule state matches the planned state.");
    }
    
    private List<Map<String, Object>> createExpectedRuleMismatches(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules,
                                                                   final List<Map<String, Object>> actualRules, final String databaseType) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : expectedRules) {
            String expectedColumn = WorkflowRuleValueUtils.getRuleValue(each, "column");
            Optional<Map<String, Object>> actualRule = findRuleByColumn(actualRules, databaseType, expectedColumn);
            if (actualRule.isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue("column", expectedColumn), "",
                        "Expected mask rule column is missing.", "Re-apply the intended mask rule."));
                continue;
            }
            addExpectedRuleValueMismatches(result, snapshot, each, actualRule.get());
        }
        for (Map<String, Object> each : actualRules) {
            String actualColumn = WorkflowRuleValueUtils.getRuleValue(each, "column");
            if (findRuleByColumn(expectedRules, databaseType, actualColumn).isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no extra mask rule column", formatFieldValue("column", actualColumn),
                        "Unexpected mask rule column exists.", "Inspect concurrent rule changes before retrying validation."));
            }
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findRuleByColumn(final List<Map<String, Object>> rules, final String databaseType, final String column) {
        return rules.stream()
                .filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, column, WorkflowRuleValueUtils.getRuleValue(each, "column"))).findFirst();
    }
    
    private void addExpectedRuleValueMismatches(final List<Map<String, Object>> mismatches, final WorkflowContextSnapshot snapshot, final Map<String, Object> expectedRule,
                                                final Map<String, Object> actualRule) {
        String expectedAlgorithmType = WorkflowRuleValueUtils.getRuleValue(expectedRule, "algorithm_type");
        String actualAlgorithmType = WorkflowRuleValueUtils.getRuleValue(actualRule, "algorithm_type");
        if (!expectedAlgorithmType.equalsIgnoreCase(actualAlgorithmType)) {
            mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                    formatFieldValue("algorithm_type", expectedAlgorithmType), formatFieldValue("algorithm_type", actualAlgorithmType),
                    "Mask algorithm type does not match.", "Re-apply the intended mask rule."));
        }
        addPropertyMismatch(mismatches, snapshot, expectedRule.get("algorithm_props"), actualRule.get("algorithm_props"));
    }
    
    private void addPropertyMismatch(final List<Map<String, Object>> mismatches, final WorkflowContextSnapshot snapshot, final Object expected, final Object actual) {
        Map<String, String> expectedProperties = WorkflowSQLUtils.createPropertyMap(expected);
        Map<String, String> actualProperties = WorkflowSQLUtils.createPropertyMap(actual);
        if (expectedProperties.equals(actualProperties)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                formatFieldValue("algorithm_props", WorkflowArtifactMaskUtils.maskPropertyMap(expectedProperties, snapshot.getPropertyRequirements())),
                formatFieldValue("algorithm_props", WorkflowArtifactMaskUtils.maskPropertyMap(actualProperties, snapshot.getPropertyRequirements())),
                "Mask algorithm properties do not match.", "Re-apply the intended mask rule."));
    }
    
    private List<Map<String, Object>> createMaskedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> rules) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rules) {
            Map<String, Object> rule = new LinkedHashMap<>(each);
            rule.put("algorithm_props", WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowSQLUtils.createPropertyMap(each.get("algorithm_props")), snapshot.getPropertyRequirements()));
            result.add(rule);
        }
        return result;
    }
    
    private String formatFieldValue(final String fieldName, final Object value) {
        return String.format("%s=%s", fieldName, value);
    }
}
