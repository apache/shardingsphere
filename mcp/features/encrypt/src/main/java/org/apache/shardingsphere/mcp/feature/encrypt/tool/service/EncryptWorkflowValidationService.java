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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
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
import java.util.regex.Pattern;

/**
 * Encrypt workflow validation service.
 */
public final class EncryptWorkflowValidationService implements MCPWorkflowRuntimeHandler, MCPWorkflowApplyArtifactValidator {
    
    private static final Pattern UNQUOTED_RESERVED_NAME_COLUMN_PATTERN = Pattern.compile("\\(\\s*NAME\\s*=\\s*name\\s*,\\s*CIPHER\\s*=", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern UNQUOTED_AES_TYPE_PATTERN = Pattern.compile("TYPE\\s*\\(\\s*NAME\\s*=\\s*AES\\b", Pattern.CASE_INSENSITIVE);
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    private final EncryptRuleInspectionService ruleInspectionService = new EncryptRuleInspectionService();
    
    private final WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport(
            WorkflowSynchronizationSupport.DEFAULT_SYNCHRONIZATION_WINDOW, WorkflowSynchronizationSupport.DEFAULT_POLL_INTERVAL);
    
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
            if (each.ruleDistSql()) {
                addRuleDistSQLIssues(result, snapshot, each.sql(), each.displaySql());
            }
        }
        return result;
    }
    
    @Override
    public void synchronize(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade,
                            final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade, final String sessionId) {
        workflowSynchronizationSupport.synchronize(() -> createValidationReport(snapshot, queryFacade));
    }
    
    private void addRuleDistSQLIssues(final List<Map<String, Object>> issues, final WorkflowContextSnapshot snapshot, final String sql, final String displaySql) {
        if (!isEncryptRuleDistSQL(sql)) {
            return;
        }
        if (UNQUOTED_RESERVED_NAME_COLUMN_PATTERN.matcher(sql).find()) {
            issues.add(createValidationIssue("Generated encrypt DistSQL uses reserved logical column identifier `name` without DistSQL quoting.", displaySql));
        }
        if (UNQUOTED_AES_TYPE_PATTERN.matcher(sql).find()) {
            issues.add(createValidationIssue("Generated encrypt DistSQL uses AES algorithm type without a string literal.", displaySql));
        }
        String actualSQL = sql.toLowerCase(Locale.ENGLISH);
        if (actualSQL.contains("encrypt_algorithm") && actualSQL.contains("'aes-key-value'") && !actualSQL.contains("'digest-algorithm-name'")) {
            issues.add(createValidationIssue("Generated AES encrypt DistSQL is missing `digest-algorithm-name`.", displaySql));
        }
        addAlgorithmIssues(issues, getWorkflowRequest(snapshot), displaySql);
    }
    
    private void addAlgorithmIssues(final List<Map<String, Object>> issues, final EncryptWorkflowRequest request, final String displaySql) {
        addEncryptAlgorithmIssue(issues, "encrypt", request.getAlgorithmType(), request.getPrimaryAlgorithmProperties(), displaySql);
        if (Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter())) {
            addEncryptAlgorithmIssue(issues, "assisted query", request.getOptions().getAssistedQueryAlgorithmType(), request.getOptions().getAssistedQueryAlgorithmProperties(), displaySql);
        }
        if (Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery())) {
            addEncryptAlgorithmIssue(issues, "like query", request.getOptions().getLikeQueryAlgorithmType(), request.getOptions().getLikeQueryAlgorithmProperties(), displaySql);
        }
    }
    
    private void addEncryptAlgorithmIssue(final List<Map<String, Object>> issues, final String role, final String algorithmType, final Map<String, String> properties, final String displaySql) {
        if (algorithmType.isEmpty()) {
            return;
        }
        if (!WorkflowAlgorithmUtils.isAlgorithmServiceAvailable(EncryptAlgorithm.class, algorithmType, properties)) {
            issues.add(createValidationIssue(String.format("Generated encrypt DistSQL references %s algorithm `%s`, but it cannot be loaded or initialized by EncryptAlgorithm SPI.",
                    role, algorithmType), displaySql));
        }
    }
    
    private boolean isEncryptRuleDistSQL(final String sql) {
        String actualSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return actualSQL.startsWith("CREATE ENCRYPT RULE");
    }
    
    private Map<String, Object> createValidationIssue(final String message, final String sql) {
        return new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", WorkflowLifecycle.STEP_REVIEW,
                message, "Regenerate the workflow artifact through the feature planner before approval.", true, Map.of("sql", sql)).toMap();
    }
    
    private ValidationReport createValidationReport(final WorkflowContextSnapshot snapshot, final MCPFeatureQueryFacade queryFacade) {
        ValidationReport result = new ValidationReport();
        EncryptWorkflowRequest request = getWorkflowRequest(snapshot);
        List<Map<String, Object>> encryptRules = ruleInspectionService.queryEncryptRules(queryFacade, request.getDatabase(), request.getTable());
        queryFacade.checkDatabaseCapability(request.getDatabase());
        result.setRuleValidation(validateRules(snapshot, request, encryptRules, result, queryFacade));
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
                                            final EncryptWorkflowRequest request, final List<Map<String, Object>> encryptRules, final ValidationReport validationReport,
                                            final MCPFeatureQueryFacade queryFacade) {
        Optional<List<Map<String, Object>>> expectedRules = getExpectedRules(snapshot);
        if (expectedRules.isPresent()) {
            return validateExpectedRules(snapshot, expectedRules.get(), encryptRules, validationReport, queryFacade);
        }
        Optional<Map<String, Object>> actualRule = findEncryptRule(snapshot, encryptRules, queryFacade);
        if (WorkflowLifecycleUtils.isDropWorkflow(snapshot)) {
            if (actualRule.isEmpty()) {
                return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, List.of(), "Encrypt rule has been removed.");
            }
            Map<String, Object> maskedActualRule = createMaskedRules(snapshot, List.of(actualRule.get())).getFirst();
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no encrypt rule",
                    String.valueOf(maskedActualRule),
                    "Encrypt rule still exists after drop.", "Drop the encrypt rule again or investigate the failure."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, maskedActualRule, "Encrypt rule still exists.");
        }
        if (actualRule.isEmpty()) {
            validationReport.getMismatches().add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", snapshot.getRequest().getColumn(), "",
                    "Encrypt rule is missing.", "Generate the encrypt rule artifact again and re-run validation."));
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, List.of(), "Encrypt rule is missing.");
        }
        Map<String, Object> actualRuleValue = actualRule.get();
        List<Map<String, Object>> mismatches = new LinkedList<>();
        addRuleValueMismatch(mismatches, "cipher_column", request.getOptions().getCipherColumnName(),
                WorkflowRuleValueUtils.getRuleValue(actualRuleValue, "cipher_column"), "Cipher column mapping does not match.");
        addRuleValueMismatch(mismatches, "assisted_query_column",
                Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRuleValue, "assisted_query_column"), "Assisted-query column mapping does not match.");
        addRuleValueMismatch(mismatches, "like_query_column",
                Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryColumnName() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRuleValue, "like_query_column"), "LIKE-query column mapping does not match.");
        addAlgorithmTypeMismatch(mismatches, "encryptor_type", request.getAlgorithmType(),
                WorkflowRuleValueUtils.getRuleValue(actualRuleValue, "encryptor_type"), "Encrypt algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "assisted_query_type",
                Boolean.TRUE.equals(request.getOptions().getRequiresEqualityFilter()) ? request.getOptions().getAssistedQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRuleValue, "assisted_query_type"), "Assisted-query algorithm type does not match.");
        addAlgorithmTypeMismatch(mismatches, "like_query_type",
                Boolean.TRUE.equals(request.getOptions().getRequiresLikeQuery()) ? request.getOptions().getLikeQueryAlgorithmType() : "",
                WorkflowRuleValueUtils.getRuleValue(actualRuleValue, "like_query_type"), "LIKE-query algorithm type does not match.");
        Map<String, Object> maskedActualRule = createMaskedRules(snapshot, List.of(actualRuleValue)).getFirst();
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, maskedActualRule, "Encrypt rule configuration does not match.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, maskedActualRule, createPassedRuleMessage(snapshot));
    }
    
    private Optional<List<Map<String, Object>>> getExpectedRules(final WorkflowContextSnapshot snapshot) {
        if (snapshot.getFeatureData() instanceof EncryptWorkflowState) {
            return Optional.of(((EncryptWorkflowState) snapshot.getFeatureData()).getExpectedRules());
        }
        return snapshot.getFeatureData() instanceof RuleWorkflowFeatureData ? Optional.of(((RuleWorkflowFeatureData) snapshot.getFeatureData()).getExpectedRules()) : Optional.empty();
    }
    
    private ValidationSection validateExpectedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules, final List<Map<String, Object>> actualRules,
                                                    final ValidationReport validationReport, final MCPFeatureQueryFacade queryFacade) {
        List<Map<String, Object>> mismatches = createExpectedRuleMismatches(snapshot, expectedRules, actualRules, queryFacade);
        if (!mismatches.isEmpty()) {
            validationReport.getMismatches().addAll(mismatches);
            return new ValidationSection(WorkflowLifecycle.STATUS_FAILED, createMaskedRules(snapshot, actualRules), "Encrypt table rule state does not match the planned state.");
        }
        return new ValidationSection(WorkflowLifecycle.STATUS_PASSED, createMaskedRules(snapshot, actualRules), createPassedRuleStateMessage(snapshot));
    }
    
    private String createPassedRuleMessage(final WorkflowContextSnapshot snapshot) {
        return WorkflowSecretReferenceUtils.hasSecretReferences(snapshot.getRequest())
                ? "Encrypt rule matches the planned non-sensitive columns and algorithms; sensitive properties are present and masked."
                : "Encrypt rule matches the planned columns and algorithms.";
    }
    
    private String createPassedRuleStateMessage(final WorkflowContextSnapshot snapshot) {
        return WorkflowSecretReferenceUtils.hasSecretReferences(snapshot.getRequest())
                ? "Encrypt table rule state matches the planned non-sensitive state; sensitive properties are present and masked."
                : "Encrypt table rule state matches the planned state.";
    }
    
    private List<Map<String, Object>> createExpectedRuleMismatches(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> expectedRules,
                                                                   final List<Map<String, Object>> actualRules, final MCPFeatureQueryFacade queryFacade) {
        List<Map<String, Object>> result = new LinkedList<>();
        String databaseName = snapshot.getRequest().getDatabase();
        for (Map<String, Object> each : expectedRules) {
            String expectedColumn = WorkflowRuleValueUtils.getRuleValue(each, "logic_column");
            Optional<Map<String, Object>> actualRule = findRuleByColumn(actualRules, queryFacade, databaseName, expectedColumn);
            if (actualRule.isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue("logic_column", expectedColumn), "",
                        "Expected encrypt rule column is missing.", "Re-apply the intended encrypt rule."));
                continue;
            }
            addExpectedRuleValueMismatches(result, snapshot, each, actualRule.get(), queryFacade);
        }
        for (Map<String, Object> each : actualRules) {
            String actualColumn = WorkflowRuleValueUtils.getRuleValue(each, "logic_column");
            if (findRuleByColumn(expectedRules, queryFacade, databaseName, actualColumn).isEmpty()) {
                result.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", "no extra encrypt rule column",
                        formatFieldValue("logic_column", actualColumn), "Unexpected encrypt rule column exists.", "Inspect concurrent rule changes before retrying validation."));
            }
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findRuleByColumn(final List<Map<String, Object>> rules, final MCPFeatureQueryFacade queryFacade,
                                                           final String databaseName, final String column) {
        return rules.stream()
                .filter(each -> queryFacade.isSameIdentifier(databaseName, IdentifierScope.COLUMN, column, WorkflowRuleValueUtils.getRuleValue(each, "logic_column"))).findFirst();
    }
    
    private void addExpectedRuleValueMismatches(final List<Map<String, Object>> mismatches, final WorkflowContextSnapshot snapshot, final Map<String, Object> expectedRule,
                                                final Map<String, Object> actualRule, final MCPFeatureQueryFacade queryFacade) {
        String databaseName = snapshot.getRequest().getDatabase();
        addIdentifierMismatch(mismatches, queryFacade, databaseName, "cipher_column", WorkflowRuleValueUtils.getRuleValue(expectedRule, "cipher_column"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "cipher_column"), "Cipher column mapping does not match.");
        addIdentifierMismatch(mismatches, queryFacade, databaseName, "assisted_query_column", WorkflowRuleValueUtils.getRuleValue(expectedRule, "assisted_query_column"),
                WorkflowRuleValueUtils.getRuleValue(actualRule, "assisted_query_column"), "Assisted-query column mapping does not match.");
        addIdentifierMismatch(mismatches, queryFacade, databaseName, "like_query_column", WorkflowRuleValueUtils.getRuleValue(expectedRule, "like_query_column"),
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
    
    private void addIdentifierMismatch(final List<Map<String, Object>> mismatches, final MCPFeatureQueryFacade queryFacade, final String databaseName,
                                       final String fieldName, final String expected, final String actual, final String impact) {
        if (queryFacade.isSameIdentifier(databaseName, IdentifierScope.COLUMN, expected, actual)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule", formatFieldValue(fieldName, expected), formatFieldValue(fieldName, actual), impact,
                "Re-apply the intended encrypt rule."));
    }
    
    private void addPropertyMismatch(final List<Map<String, Object>> mismatches, final WorkflowContextSnapshot snapshot, final String fieldName,
                                     final Object expected, final Object actual, final String impact) {
        Map<String, String> expectedProperties = WorkflowAlgorithmUtils.createPropertyMap(expected);
        Map<String, String> actualProperties = WorkflowAlgorithmUtils.createPropertyMap(actual);
        String algorithmRole = getAlgorithmRole(fieldName);
        if (WorkflowSecretReferenceUtils.matchesManualPlaceholderProperties(expectedProperties, actualProperties, snapshot.getRequest(), algorithmRole)) {
            return;
        }
        mismatches.add(validationSupport.createMismatch(WorkflowIssueCode.RULE_STATE_MISMATCH, "rule",
                formatFieldValue(fieldName, WorkflowArtifactMaskUtils.maskPropertyMap(expectedProperties, snapshot.getPropertyRequirements(), snapshot.getRequest(),
                        algorithmRole)),
                formatFieldValue(fieldName, WorkflowArtifactMaskUtils.maskPropertyMap(actualProperties, snapshot.getPropertyRequirements(), snapshot.getRequest(),
                        algorithmRole)),
                impact,
                "Re-apply the intended encrypt rule."));
    }
    
    private List<Map<String, Object>> createMaskedRules(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> rules) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rules) {
            Map<String, Object> rule = new LinkedHashMap<>(each);
            rule.put("encryptor_props", WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowAlgorithmUtils.createPropertyMap(each.get("encryptor_props")), snapshot.getPropertyRequirements(),
                    snapshot.getRequest(), EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY));
            rule.put("assisted_query_props",
                    WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowAlgorithmUtils.createPropertyMap(each.get("assisted_query_props")), snapshot.getPropertyRequirements(),
                            snapshot.getRequest(), EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY));
            rule.put("like_query_props", WorkflowArtifactMaskUtils.maskPropertyMap(WorkflowAlgorithmUtils.createPropertyMap(each.get("like_query_props")), snapshot.getPropertyRequirements(),
                    snapshot.getRequest(), EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY));
            result.add(rule);
        }
        return result;
    }
    
    private Optional<Map<String, Object>> findEncryptRule(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> encryptRules,
                                                          final MCPFeatureQueryFacade queryFacade) {
        return encryptRules.stream()
                .filter(each -> queryFacade.isSameIdentifier(snapshot.getRequest().getDatabase(), IdentifierScope.COLUMN, snapshot.getRequest().getColumn(),
                        WorkflowRuleValueUtils.getRuleValue(each, "logic_column")))
                .findFirst();
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
    
    private String getAlgorithmRole(final String fieldName) {
        if ("assisted_query_props".equals(fieldName)) {
            return EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY;
        }
        if ("like_query_props".equals(fieldName)) {
            return EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY;
        }
        return EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY;
    }
    
    private String formatFieldValue(final String fieldName, final Object value) {
        return String.format("%s=%s", fieldName, value);
    }
    
}
