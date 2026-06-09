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
import org.apache.shardingsphere.mcp.support.workflow.model.RuleWorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactMaskUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowValidationSupport;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowRuntimeHandler;

import java.util.LinkedHashMap;
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
        EncryptWorkflowRequest request = getWorkflowRequest(snapshot);
        List<Map<String, Object>> encryptRules = ruleInspectionService.queryEncryptRules(queryFacade, request.getDatabase(), request.getTable());
        String databaseType = queryFacade.getDatabaseType(request.getDatabase());
        result.setRuleValidation(validateRules(snapshot, request, encryptRules, result, databaseType));
        result.setOverallStatus(validationSupport.resolveOverallStatus(result.getRuleValidation()));
        return result;
    }
    
    private EncryptWorkflowRequest getWorkflowRequest(final WorkflowContextSnapshot snapshot) {
        if (snapshot.getRequest() instanceof EncryptWorkflowRequest) {
            return (EncryptWorkflowRequest) snapshot.getRequest();
        }
        EncryptWorkflowRequest result = EncryptWorkflowRequest.merge(snapshot.getRequest(), null);
        return null == result ? new EncryptWorkflowRequest() : result;
    }
    
    private ValidationSection validateRules(final WorkflowContextSnapshot snapshot,
                                            final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules, final ValidationReport validationReport, final String databaseType) {
        Optional<List<Map<String, Object>>> expectedRules = getExpectedRules(snapshot);
        if (expectedRules.isPresent()) {
            return validateExpectedRules(snapshot, expectedRules.get(), encryptRules, validationReport, databaseType);
        }
        Optional<Map<String, Object>> actualRule = findEncryptRule(snapshot, encryptRules, databaseType);
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, List.of(), "Encrypt rule has been removed.");
            }
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no encrypt rule",
                    String.valueOf(createMaskedRules(snapshot, List.of(actualRule.get())).getFirst()),
                    "Encrypt rule still exists after drop.", "Drop the encrypt rule again or investigate the failure."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, List.of(actualRule.get())).getFirst(), "Encrypt rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Encrypt rule is missing.", "Create or alter the encrypt rule again."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Encrypt rule is missing.");
        }
        List<Map<String, Object>> mismatches = new LinkedList<>();
        addRuleValueMismatch(mismatches, "cipher_column", request.getOptions().getCipherColumnName(),
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "cipher_column"), "Cipher column mapping does not match.");
        addRuleValueMismatch(mismatches, "assisted_query_column",
                Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "assisted_query_column"), "Assisted-query column mapping does not match.");
        addRuleValueMismatch(mismatches, "like_query_column",
                Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRule.get(), "like_query_column"), "LIKE-query column mapping does not match.");
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
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, List.of(actualRule.get())).getFirst(), "Encrypt rule configuration does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, createMaskedRules(snapshot, List.of(actualRule.get())).getFirst(), "Encrypt rule matches the planned columns and algorithms.");
    }
    
    private Optional<List<Map<String, Object>>> getExpectedRules(final WorkflowContextSnapshot snapshot) {
        if (snapshot.getFeatureData() instanceof EncryptWorkflowState) {
            return Optional.of(((EncryptWorkflowState) snapshot.getFeatureData()).getExpectedRules());
        }
        return snapshot.getFeatureData() instanceof RuleWorkflowFeatureData ? Optional.of(((RuleWorkflowFeatureData) snapshot.getFeatureData()).getExpectedRules()) : Optional.empty();
    }
    
    private ValidationSection validateExpectedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules, final List<Map<String, Object>> actualRules,
                                                    final ValidationReport validationReport, final String databaseType) {
        List<Map<String, Object>> mismatches = createExpectedRuleMismatches(snapshot, expectedRules, actualRules, databaseType);
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, actualRules), "Encrypt table rule state does not match the planned state.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, createMaskedRules(snapshot, actualRules), "Encrypt table rule state matches the planned state.");
    }
    
    private List<Map<String, Object>> createExpectedRuleMismatches(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules,
                                                                   final List<Map<String, Object>> actualRules, final String databaseType) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : expectedRules) {
            String expectedColumn = WorkflowRuleValueUtils.getRuleValue(each, "logic_column");
            Optional<Map<String, Object>> actualRule = findRuleByColumn(actualRules, databaseType, expectedColumn);
            if (actualRule.isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue("logic_column", expectedColumn), "",
                        "Expected encrypt rule column is missing.", "Re-apply the intended encrypt rule."));
                continue;
            }
            addExpectedRuleValueMismatches(result, snapshot, each, actualRule.get(), databaseType);
        }
        for (Map<String, Object> each : actualRules) {
            String actualColumn = WorkflowRuleValueUtils.getRuleValue(each, "logic_column");
            if (findRuleByColumn(expectedRules, databaseType, actualColumn).isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no extra encrypt rule column",
                        formatFieldValue("logic_column", actualColumn), "Unexpected encrypt rule column exists.", "Inspect concurrent rule changes before retrying validation."));
            }
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findRuleByColumn(final List<Map<String, Object>> rules, final String databaseType, final String column) {
        return rules.stream()
                .filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, column, WorkflowRuleValueUtils.getRuleValue(each, "logic_column"))).findFirst();
    }
    
    private void addExpectedRuleValueMismatches(final List<Map<String, Object>> mismatches, final WorkflowContextSnapshot snapshot, final Map<String, Object> expectedRule,
                                                final Map<String, Object> actualRule, final String databaseType) {
        addIdentifierMismatch(mismatches, databaseType, "cipher_column", WorkflowRuleValueUtils.getRuleValue(expectedRule, "cipher_column"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "cipher_column"), "Cipher column mapping does not match.");
        addIdentifierMismatch(mismatches, databaseType, "assisted_query_column", WorkflowRuleValueUtils.getRuleValue(expectedRule, "assisted_query_column"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "assisted_query_column"), "Assisted-query column mapping does not match.");
        addIdentifierMismatch(mismatches, databaseType, "like_query_column", WorkflowRuleValueUtils.getRuleValue(expectedRule, "like_query_column"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "like_query_column"), "LIKE-query column mapping does not match.");
        addAlgorithmTypeMismatch(mismatches, "encryptor_type", WorkflowRuleValueUtils.getRuleValue(expectedRule, "encryptor_type"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "encryptor_type"), "Encrypt algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "assisted_query_type", WorkflowRuleValueUtils.getRuleValue(expectedRule, "assisted_query_type"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "assisted_query_type"), "Assisted-query algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "like_query_type", WorkflowRuleValueUtils.getRuleValue(expectedRule, "like_query_type"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "like_query_type"), "LIKE-query algorithm type does not match.");
        addPropertyMismatch(mismatches, snapshot, "encryptor_props", expectedRule.get("encryptor_props"), actualRule.get("encryptor_props"), "Encrypt algorithm properties do not match.");
        addPropertyMismatch(mismatches, snapshot, "assisted_query_props", expectedRule.get("assisted_query_props"), actualRule.get("assisted_query_props"),
                "Assisted-query algorithm properties do not match.");
        addPropertyMismatch(mismatches, snapshot, "like_query_props", expectedRule.get("like_query_props"), actualRule.get("like_query_props"),
                "LIKE-query algorithm properties do not match.");
    }
    
    private void addIdentifierMismatch(final List<Map<String, Object>> mismatches, final String databaseType, final String fieldName, final String expected,
                                       final String actual, final String impact) {
        if (WorkflowSQLUtils.isSameIdentifier(databaseType, expected, actual)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue(fieldName, expected), formatFieldValue(fieldName, actual), impact,
                "Re-apply the intended encrypt rule."));
    }
    
    private void addPropertyMismatch(final List<Map<String, Object>> mismatches, final WorkflowContextSnapshot snapshot, final String fieldName,
                                     final Object expected, final Object actual, final String impact) {
        Map<String, String> expectedProperties = WorkflowSQLUtils.createPropertyMap(expected);
        Map<String, String> actualProperties = WorkflowSQLUtils.createPropertyMap(actual);
        if (expectedProperties.equals(actualProperties)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                formatFieldValue(fieldName, WorkflowArtifactMaskUtils.maskPropertyMap(expectedProperties, snapshot.getPropertyRequirements())),
                formatFieldValue(fieldName, WorkflowArtifactMaskUtils.maskPropertyMap(actualProperties, snapshot.getPropertyRequirements())), impact,
                "Re-apply the intended encrypt rule."));
    }
    
    private List<Map<String, Object>> createMaskedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> rules) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rules) {
            Map<String, Object> rule = new LinkedHashMap<>(each);
            rule.put("encryptor_props", WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowSQLUtils.createPropertyMap(each.get("encryptor_props")), snapshot.getPropertyRequirements()));
            rule.put("assisted_query_props",
                    WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowSQLUtils.createPropertyMap(each.get("assisted_query_props")), snapshot.getPropertyRequirements()));
            rule.put("like_query_props", WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowSQLUtils.createPropertyMap(each.get("like_query_props")), snapshot.getPropertyRequirements()));
            result.add(rule);
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findEncryptRule(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> encryptRules, final String databaseType) {
        return encryptRules.stream()
                .filter(each -> WorkflowSQLUtils.isSameIdentifier(databaseType, snapshot.getRequest().getColumn(), WorkflowRuleValueUtils.getRuleValue(each, "logic_column"))).findFirst();
    }
    
    private void addRuleValueMismatch(final List<Map<String, Object>> mismatches, final String fieldName, final String expected, final String actual, final String impact) {
        if (expected.equals(actual)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue(fieldName, expected), formatFieldValue(fieldName, actual), impact,
                "Re-apply the intended encrypt rule."));
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
    
    private String formatFieldValue(final String fieldName, final Object value) {
        return String.format("%s=%s", fieldName, value);
    }
    
}
