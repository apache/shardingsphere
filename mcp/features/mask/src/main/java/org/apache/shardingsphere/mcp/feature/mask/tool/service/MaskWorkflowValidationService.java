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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleWorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactMaskUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSecretReferenceUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Mask workflow validation service.
 */
public final class MaskWorkflowValidationService implements MCPWorkflowRuntimeHandler, MCPWorkflowApplyArtifactValidator {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final MaskRuleInspectionService ruleInspectionService;
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport;
    
    public MaskWorkflowValidationService() {
        ruleInspectionService = new MaskRuleInspectionService();
        workflowSynchronizationSupport = new WorkflowSynchronizationSupport(
                WorkflowSynchronizationSupport.DEFAULT_SYNCHRONIZATION_WINDOW, WorkflowSynchronizationSupport.DEFAULT_POLL_INTERVAL);
    }
    
    @Override
    public Map<String, Object> validate(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                        final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId,
                                        final WorkflowContextSnapshot snapshot) {
        return validationSupport.validateAndFinalize(workflowSessionContext, sessionId, snapshot, () -> createValidationReport(snapshot, queryFacade));
    }
    
    @Override
    public List<Map<String, Object>> validate(final WorkflowContextSnapshot snapshot, final Collection<ExecutableWorkflowArtifact> artifacts) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (ExecutableWorkflowArtifact each : artifacts) {
            addRuleDistSQLIssues(result, snapshot, each.sql(), each.displaySql());
        }
        return result;
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private void addRuleDistSQLIssues(final List<Map<String, Object>> issues, final WorkflowContextSnapshot snapshot, final String sql, final String displaySql) {
        if (!isMaskRuleDistSQL(sql)) {
            return;
        }
        WorkflowRequest request = null == snapshot.getRequest() ? new WorkflowRequest() : snapshot.getRequest();
        if (request.getAlgorithmType().isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(MaskAlgorithm.class, request.getAlgorithmType(), request.getPrimaryAlgorithmProperties())) {
            issues.add(createValidationIssue(String.format("Generated mask DistSQL references algorithm `%s`, but it cannot be loaded or initialized by MaskAlgorithm SPI.",
                    request.getAlgorithmType()), displaySql));
        }
    }
    
    private boolean isMaskRuleDistSQL(final String sql) {
        String actualSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return actualSQL.startsWith("CREATE MASK RULE");
    }
    
    private Map<String, Object> createValidationIssue(final String message, final String sql) {
        return new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", WorkflowLifecycle.STEP_REVIEW,
                message, "Regenerate the workflow artifact through the feature planner before approval.", true, Map.of("sql", sql)).toMap();
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        queryFacade.checkDatabaseCapability(snapshot.getRequest().getDatabase());
        List<Map<String, Object>> maskRules = ruleInspectionService.queryMaskRules(queryFacade, snapshot.getRequest().getDatabase(), snapshot.getRequest().getTable());
        result.setRuleValidation(validateRules(snapshot, maskRules, result, queryFacade));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> maskRules,
                                            final ValidationReport validationReport, final MCPFeatureQueryFacade queryFacade) {
        Optional<RuleWorkflowFeatureData> ruleFeatureData = getRuleFeatureData(snapshot);
        if (ruleFeatureData.isPresent()) {
            return validateExpectedRules(snapshot, ruleFeatureData.get().getExpectedRules(), maskRules, validationReport, queryFacade);
        }
        Optional<Map<String, Object>> actualRule = maskRules.stream()
                .filter(each -> queryFacade.isSameIdentifier(snapshot.getRequest().getDatabase(), IdentifierScope.COLUMN, snapshot.getRequest().getColumn(),
                        WorkflowRuleValueUtils.getRuleValue(each, "column")))
                .findFirst();
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, List.of(), "Mask rule has been removed.");
            }
            Map<String, Object> maskedActualRule = createMaskedRules(snapshot, List.of(actualRule.get())).getFirst();
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no mask rule",
                    String.valueOf(maskedActualRule),
                    "Mask rule still exists after drop.", "Drop the mask rule again or investigate the failure."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, maskedActualRule, "Mask rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Mask rule is missing.", "Generate the mask rule artifact again and re-run validation."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Mask rule is missing.");
        }
        Map<String, Object> actualRuleValue = actualRule.get();
        Map<String, Object> maskedActualRule = createMaskedRules(snapshot, List.of(actualRuleValue)).getFirst();
        String actualAlgorithmType = WorkflowRuleValueUtils.getRuleValue(actualRuleValue, "algorithm_type");
        if (!snapshot.getRequest().getAlgorithmType().equalsIgnoreCase(actualAlgorithmType)) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getAlgorithmType(), actualAlgorithmType,
                    "Mask algorithm type does not match.", "Re-apply the intended mask rule."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, maskedActualRule, "Mask algorithm type does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, maskedActualRule, createPassedRuleMessage(snapshot));
    }
    
    private Optional<RuleWorkflowFeatureData> getRuleFeatureData(final WorkflowContextSnapshot snapshot) {
        return snapshot.getFeatureData() instanceof RuleWorkflowFeatureData ? Optional.of((RuleWorkflowFeatureData) snapshot.getFeatureData()) : Optional.empty();
    }
    
    private ValidationSection validateExpectedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules, final List<Map<String, Object>> actualRules,
                                                    final ValidationReport validationReport, final MCPFeatureQueryFacade queryFacade) {
        List<Map<String, Object>> mismatches = createExpectedRuleMismatches(snapshot, expectedRules, actualRules, queryFacade);
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, actualRules), "Mask table rule state does not match the planned state.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, createMaskedRules(snapshot, actualRules), createPassedRuleStateMessage(snapshot));
    }
    
    private String createPassedRuleMessage(final WorkflowContextSnapshot snapshot) {
        return WorkflowSecretReferenceUtils.hasSecretReferences(snapshot.getRequest())
                ? "Mask rule matches the planned non-sensitive algorithm state; sensitive properties are present and masked."
                : "Mask rule matches the planned algorithm.";
    }
    
    private String createPassedRuleStateMessage(final WorkflowContextSnapshot snapshot) {
        return WorkflowSecretReferenceUtils.hasSecretReferences(snapshot.getRequest())
                ? "Mask table rule state matches the planned non-sensitive state; sensitive properties are present and masked."
                : "Mask table rule state matches the planned state.";
    }
    
    private List<Map<String, Object>> createExpectedRuleMismatches(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules,
                                                                   final List<Map<String, Object>> actualRules, final MCPFeatureQueryFacade queryFacade) {
        List<Map<String, Object>> result = new LinkedList<>();
        String databaseName = snapshot.getRequest().getDatabase();
        for (Map<String, Object> each : expectedRules) {
            String expectedColumn = WorkflowRuleValueUtils.getRuleValue(each, "column");
            Optional<Map<String, Object>> actualRule = findRuleByColumn(actualRules, queryFacade, databaseName, expectedColumn);
            if (actualRule.isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue("column", expectedColumn), "",
                        "Expected mask rule column is missing.", "Re-apply the intended mask rule."));
                continue;
            }
            addExpectedRuleValueMismatches(result, snapshot, each, actualRule.get());
        }
        for (Map<String, Object> each : actualRules) {
            String actualColumn = WorkflowRuleValueUtils.getRuleValue(each, "column");
            if (findRuleByColumn(expectedRules, queryFacade, databaseName, actualColumn).isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no extra mask rule column", formatFieldValue("column", actualColumn),
                        "Unexpected mask rule column exists.", "Inspect concurrent rule changes before retrying validation."));
            }
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findRuleByColumn(final List<Map<String, Object>> rules, final MCPFeatureQueryFacade queryFacade,
                                                           final String databaseName, final String column) {
        return rules.stream()
                .filter(each -> queryFacade.isSameIdentifier(databaseName, IdentifierScope.COLUMN, column, WorkflowRuleValueUtils.getRuleValue(each, "column"))).findFirst();
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
        Map<String, String> expectedProperties = WorkflowAlgorithmUtils.createPropertyMap(expected);
        Map<String, String> actualProperties = WorkflowAlgorithmUtils.createPropertyMap(actual);
        if (WorkflowSecretReferenceUtils.matchesManualPlaceholderProperties(expectedProperties, actualProperties, snapshot.getRequest(), "primary")) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                formatFieldValue("algorithm_props", WorkflowArtifactMaskUtils.maskPropertyMap(expectedProperties, snapshot.getPropertyRequirements(), snapshot.getRequest(), "primary")),
                formatFieldValue("algorithm_props", WorkflowArtifactMaskUtils.maskPropertyMap(actualProperties, snapshot.getPropertyRequirements(), snapshot.getRequest(), "primary")),
                "Mask algorithm properties do not match.", "Re-apply the intended mask rule."));
    }
    
    private List<Map<String, Object>> createMaskedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> rules) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rules) {
            Map<String, Object> rule = new LinkedHashMap<>(each);
            rule.put("algorithm_props", WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowAlgorithmUtils.createPropertyMap(each.get("algorithm_props")), snapshot.getPropertyRequirements(),
                    snapshot.getRequest(), "primary"));
            result.add(rule);
        }
        return result;
    }
    
    private String formatFieldValue(final String fieldName, final Object value) {
        return String.format("%s=%s", fieldName, value);
    }
}
