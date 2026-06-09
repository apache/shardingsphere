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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@EnabledIf("isEnabled")
class HttpProductionProxyEncryptWorkflowE2ETest extends AbstractProductionProxyWorkflowE2ETest {
    
    private static final String PLAN_TOOL_NAME = "database_gateway_plan_encrypt_rule";
    
    private static final String PLAN_PROMPT_NAME = "plan_encrypt_rule";
    
    private static final String APPLY_TOOL_NAME = WorkflowToolDescriptors.APPLY_TOOL_NAME;
    
    private static final String VALIDATE_TOOL_NAME = WorkflowToolDescriptors.VALIDATE_TOOL_NAME;
    
    private static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/encrypt/algorithms";
    
    private static final String RULES_RESOURCE_URI = "shardingsphere://features/encrypt/databases/%s/rules";
    
    private static final String TABLE_RULES_RESOURCE_URI = "shardingsphere://features/encrypt/databases/%s/tables/%s/rules";
    
    private static final String WORKFLOW_RESOURCE_URI = "shardingsphere://workflows/%s";
    
    private static final String TEMPLATE_SECRET_VALUE = "mcp-template-secret-6f2d4a8b";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertCompleteEncryptAlgorithmThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.complete(Map.of("type", "ref/prompt", "name", PLAN_PROMPT_NAME), "algorithm_type", "AE", Map.of());
            assertThat(getStringList(getMap(actual.get("completion")).get("values")), hasItem("AES"));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateEncryptWorkflowThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> clarifyingResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("table", "orders", "column", "status", "natural_language_intent", "encrypt status with reversible encryption, no equality, no like"));
            assertThat(String.valueOf(clarifyingResponse.get("status")), is("clarifying"));
            assertThat(getClarificationMessages(clarifyingResponse), is(List.of("Please provide logical database first.")));
            String planId = String.valueOf(clarifyingResponse.get("plan_id"));
            Map<String, Object> plannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("plan_id", planId, "database", getLogicalDatabaseName(), "algorithm_type", "AES",
                            "cipher_column_name", "status_cipher", "primary_algorithm_properties", Map.of("aes-key-value", TEMPLATE_SECRET_VALUE)));
            assertThat(String.valueOf(plannedResponse.get("status")), is("planned"));
            assertSecretRedacted(plannedResponse, TEMPLATE_SECRET_VALUE);
            assertThat(String.valueOf(plannedResponse.get("current_step")), is("review"));
            assertThat(getStringList(plannedResponse.get("global_steps")).size(), is(8));
            assertFalse(plannedResponse.containsKey("derived_column_plan"));
            assertFalse(plannedResponse.containsKey("ddl_artifacts"));
            assertFalse(plannedResponse.containsKey("index_plan"));
            Map<String, Object> maskedPropertyPreview = getMap(plannedResponse.get("masked_property_preview"));
            assertThat(String.valueOf(getMap(maskedPropertyPreview.get("primary")).get("aes-key-value")), is("******"));
            List<Map<String, Object>> distSqlArtifacts = getMapList(plannedResponse.get("distsql_artifacts"));
            assertThat(distSqlArtifacts.size(), is(1));
            assertThat(String.valueOf(distSqlArtifacts.getFirst().get("sql")), containsString("'aes-key-value'='******'"));
            assertThat(String.valueOf(distSqlArtifacts.getFirst().get("sql")), containsString("CIPHER=status_cipher"));
            assertSecretRedacted(interactionClient.readResource(String.format(WORKFLOW_RESOURCE_URI, planId)), TEMPLATE_SECRET_VALUE);
            Map<String, Object> applyResponse = applyReviewedWorkflow(interactionClient, planId, TEMPLATE_SECRET_VALUE);
            assertApplyCompleted(applyResponse);
            assertThat(getMapList(applyResponse.get("step_results")).size(), is(1));
            assertThat(getStringList(applyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(applyResponse.get("executed_distsql")).size(), is(1));
            Map<String, Object> validationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(validationResponse);
            assertSecretRedacted(validationResponse, TEMPLATE_SECRET_VALUE);
            assertFalse(validationResponse.containsKey("ddl_validation"));
            assertFalse(validationResponse.containsKey("logical_metadata_validation"));
            assertFalse(validationResponse.containsKey("sql_executability_validation"));
            Map<String, Object> ruleValidation = getMap(validationResponse.get("rule_validation"));
            assertThat(String.valueOf(ruleValidation.get("status")), is("passed"));
            Map<String, Object> ruleEvidence = getMapList(ruleValidation.get("evidence")).getFirst();
            assertThat(String.valueOf(ruleEvidence.get("logic_column")), is("status"));
            assertThat(String.valueOf(ruleEvidence.get("cipher_column")), is("status_cipher"));
            assertThat(String.valueOf(ruleEvidence.get("encryptor_type")).toUpperCase(Locale.ENGLISH), is("AES"));
        }
    }
    
    @Test
    void assertPlanRecommendsAssistedQueryEncryptWorkflowThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualClarifyingResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, requires equality, no like"));
            assertThat(String.valueOf(actualClarifyingResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualClarifyingResponse), hasItem(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
            Map<String, Object> actualClarificationQuestion = getMapList(actualClarifyingResponse.get("clarification_questions")).getFirst();
            assertThat(String.valueOf(actualClarificationQuestion.get("field")), is("primary_algorithm_properties.aes-key-value"));
            assertThat(String.valueOf(actualClarificationQuestion.get("input_type")), is("secret"));
            assertTrue((Boolean) actualClarificationQuestion.get("secret"));
            assertThat(String.valueOf(actualClarificationQuestion.get("message")),
                    is("Sensitive input must be provided through configured secure channels before continuing the same planner."));
            assertFalse(actualClarificationQuestion.containsKey("display_message"));
            List<Map<String, Object>> actualRecommendations = getMapList(actualClarifyingResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(2));
            assertThat(String.valueOf(actualRecommendations.getFirst().get("algorithm_role")), is("primary"));
            assertThat(String.valueOf(actualRecommendations.getFirst().get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("AES"));
            assertThat(String.valueOf(actualRecommendations.get(1).get("algorithm_role")), is("assisted_query"));
            assertThat(String.valueOf(actualRecommendations.get(1).get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("MD5"));
            String planId = String.valueOf(actualClarifyingResponse.get("plan_id"));
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("plan_id", planId, "cipher_column_name", "status_cipher", "assisted_query_column_name", "status_assisted_query",
                            "assisted_query_algorithm_type", "MD5", "primary_algorithm_properties", Map.of("aes-key-value", "assisted-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            assertFalse(actualPlannedResponse.containsKey("derived_column_plan"));
            assertFalse(actualPlannedResponse.containsKey("ddl_artifacts"));
            assertFalse(actualPlannedResponse.containsKey("index_plan"));
            List<Map<String, Object>> actualDistSqlArtifacts = getMapList(actualPlannedResponse.get("distsql_artifacts"));
            assertThat(actualDistSqlArtifacts.size(), is(1));
            assertThat(String.valueOf(actualDistSqlArtifacts.getFirst().get("sql")), containsString("ASSISTED_QUERY_COLUMN=status_assisted_query"));
            Map<String, Object> actualApplyResponse = applyReviewedWorkflow(interactionClient, planId);
            assertApplyCompleted(actualApplyResponse);
            assertThat(getMapList(actualApplyResponse.get("step_results")).size(), is(1));
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertFalse(actualValidationResponse.containsKey("sql_executability_validation"));
        }
    }
    
    @Test
    void assertPlanReportsLikeQueryCapabilityConflictThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, requires equality and LIKE query"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("clarifying"));
            List<String> actualIssueCodes = getIssueCodes(actualPlanResponse);
            assertThat(actualIssueCodes, hasItem(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
            assertFalse(actualPlanResponse.containsKey("derived_column_plan"));
            List<Map<String, Object>> actualRecommendations = getMapList(actualPlanResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(2));
            assertFalse(actualRecommendations.stream().anyMatch(each -> "like_query".equals(each.get("algorithm_role"))));
        }
    }
    
    @Test
    void assertPlanRequiresExplicitCipherColumnThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "explicit-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("clarifying"));
            assertSecretRedacted(actualPlannedResponse, "explicit-secret");
            assertThat(getIssueCodes(actualPlannedResponse), hasItem(WorkflowIssueCode.RULE_INPUT_REQUIRED));
            assertThat(getStringList(actualPlannedResponse.get("missing_required_inputs")), hasItem("cipher_column_name"));
            assertFalse(actualPlannedResponse.containsKey("derived_column_plan"));
            assertFalse(actualPlannedResponse.containsKey("ddl_artifacts"));
        }
    }
    
    @Test
    void assertPlanRejectsUnsupportedSecondEncryptColumnThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualFirstPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "cipher_column_name", "status_cipher", "primary_algorithm_properties", Map.of("aes-key-value", "first-secret")));
            assertThat(String.valueOf(actualFirstPlanResponse.get("status")), is("planned"));
            String firstPlanId = String.valueOf(actualFirstPlanResponse.get("plan_id"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, firstPlanId));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", firstPlanId)));
            Map<String, Object> actualSecondPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "amount",
                            "natural_language_intent", "encrypt amount with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "second-secret")));
            assertThat(String.valueOf(actualSecondPlanResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualSecondPlanResponse), hasItem(WorkflowIssueCode.ENCRYPT_ALTER_SCOPE_LIMITED));
            assertFalse(getClarificationMessages(actualSecondPlanResponse).isEmpty());
            assertFalse(actualSecondPlanResponse.containsKey("ddl_artifacts"));
            assertThat(getMapList(actualSecondPlanResponse.get("distsql_artifacts")).size(), is(0));
            List<Map<String, Object>> actualEncryptRules = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualEncryptRules.size(), is(1));
            assertThat(String.valueOf(findItemByField(actualEncryptRules, "logic_column", "status").get("cipher_column")), is("status_cipher"));
        }
    }
    
    @Test
    void assertApplySupportsManualOnlyExecutionModeThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "cipher_column_name", "status_cipher", "primary_algorithm_properties", Map.of("aes-key-value", "manual-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            Map<String, Object> actualApplyResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "manual-only"));
            assertThat(String.valueOf(actualApplyResponse.get("status")), is("awaiting-manual-execution"));
            assertThat(getIssueCodes(actualApplyResponse), is(List.of(WorkflowIssueCode.MANUAL_EXECUTION_PENDING)));
            assertThat(getMapList(actualApplyResponse.get("step_results")).size(), is(0));
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(0));
            Map<String, Object> actualManualArtifactPackage = getMap(actualApplyResponse.get("manual_artifact_package"));
            assertFalse(actualManualArtifactPackage.containsKey("ddl_artifacts"));
            assertThat(getMapList(actualManualArtifactPackage.get("distsql_artifacts")).size(), is(1));
            assertThat(String.valueOf(getMapList(actualManualArtifactPackage.get("distsql_artifacts")).getFirst().get("sql")),
                    containsString("'aes-key-value'='******'"));
        }
    }
    
    @Test
    void assertApplySupportsRuleApprovedStepThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "cipher_column_name", "status_cipher", "primary_algorithm_properties", Map.of("aes-key-value", "approved-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            Map<String, Object> actualPreviewResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "preview"));
            assertThat(String.valueOf(actualPreviewResponse.get("status")), is("preview"));
            List<Map<String, Object>> actualPreviewArtifacts = getMapList(actualPreviewResponse.get("preview_artifacts"));
            assertThat(actualPreviewArtifacts.size(), is(1));
            assertThat(String.valueOf(actualPreviewArtifacts.getFirst().get("approval_step")), is("rule_distsql"));
            Map<String, Object> actualRuleApplyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId, List.of("rule_distsql")));
            assertThat(String.valueOf(actualRuleApplyResponse.get("status")), is("completed"));
            assertThat(getStringList(actualRuleApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualRuleApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(getStringList(actualRuleApplyResponse.get("skipped_artifacts")).size(), is(0));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
        }
    }
    
    @Test
    void assertPlanRejectsUnsupportedEncryptAlterExpansionThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createEncryptRuleWithoutEquality(interactionClient);
            Map<String, Object> actualAlterPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, update to require equality and no like", "operation_type", "alter",
                            "primary_algorithm_properties", Map.of("aes-key-value", "alter-secret")));
            assertThat(String.valueOf(actualAlterPlanResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualAlterPlanResponse), hasItem(WorkflowIssueCode.ENCRYPT_ALTER_SCOPE_LIMITED));
            assertFalse(getClarificationMessages(actualAlterPlanResponse).isEmpty());
            assertFalse(actualAlterPlanResponse.containsKey("ddl_artifacts"));
            assertThat(getMapList(actualAlterPlanResponse.get("distsql_artifacts")).size(), is(0));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateEncryptDropWorkflowThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createEncryptRuleWithoutEquality(interactionClient);
            Map<String, Object> actualDropPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status", "operation_type", "drop"));
            assertThat(String.valueOf(actualDropPlanResponse.get("status")), is("planned"));
            assertThat(getClarificationMessages(actualDropPlanResponse), is(List.of()));
            assertThat(getIssueCodes(actualDropPlanResponse), hasItem(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED));
            assertFalse(getIssueCodes(actualDropPlanResponse).contains(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED));
            assertFalse(actualDropPlanResponse.containsKey("ddl_artifacts"));
            assertThat(String.valueOf(getMapList(actualDropPlanResponse.get("distsql_artifacts")).getFirst().get("sql")), is("DROP ENCRYPT RULE orders"));
            String planId = String.valueOf(actualDropPlanResponse.get("plan_id"));
            Map<String, Object> actualApplyResponse = applyReviewedWorkflow(interactionClient, planId);
            assertApplyCompleted(actualApplyResponse);
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertFalse(actualValidationResponse.containsKey("ddl_validation"));
            assertThat(String.valueOf(getMap(actualValidationResponse.get("rule_validation")).get("status")), is("passed"));
        }
    }
    
    @Test
    void assertPlanApplyValidateAndReadEncryptResourcesWithCustomAlgorithmThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "MCP_CUSTOM_REVERSIBLE",
                            "cipher_column_name", "status_cipher"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            assertApplyCompleted(applyReviewedWorkflow(interactionClient, planId));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
            List<Map<String, Object>> actualEncryptPluginItems = getPayloadItems(interactionClient.readResource(ALGORITHMS_RESOURCE_URI));
            Map<String, Object> actualCustomAlgorithm = findItemByField(actualEncryptPluginItems, "type", "MCP_CUSTOM_REVERSIBLE");
            assertThat(String.valueOf(actualCustomAlgorithm.get("type")), is("MCP_CUSTOM_REVERSIBLE"));
            assertFalse((Boolean) actualCustomAlgorithm.get("capability_confirmed"));
            List<Map<String, Object>> actualEncryptRules = getPayloadItems(
                    interactionClient.readResource(String.format(RULES_RESOURCE_URI, getLogicalDatabaseName())));
            Map<String, Object> actualEncryptRule = findItemByField(actualEncryptRules, "logic_column", "status");
            assertThat(String.valueOf(actualEncryptRule.get("encryptor_type")).toUpperCase(Locale.ENGLISH), is("MCP_CUSTOM_REVERSIBLE"));
            List<Map<String, Object>> actualSingleRuleItems = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualSingleRuleItems.size(), is(1));
            assertThat(String.valueOf(actualSingleRuleItems.getFirst().get("logic_column")), is("status"));
        }
    }
    
    private void createEncryptRuleWithoutEquality(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        createEncryptRuleWithoutEquality(interactionClient, "status", "base-secret");
    }
    
    private void createEncryptRuleWithoutEquality(final MCPInteractionClient interactionClient, final String columnName, final String secret) throws IOException, InterruptedException {
        Map<String, Object> actualCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", columnName,
                        "natural_language_intent", String.format("encrypt %s with reversible encryption, no equality, no like", columnName), "algorithm_type", "AES",
                        "cipher_column_name", columnName + "_cipher", "primary_algorithm_properties", Map.of("aes-key-value", secret)));
        assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
        String planId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
        assertApplyCompleted(applyReviewedWorkflow(interactionClient, planId));
        assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
    }
    
    private Map<String, Object> applyReviewedWorkflow(final MCPInteractionClient interactionClient, final String planId) throws IOException, InterruptedException {
        return applyReviewedWorkflow(interactionClient, planId, "");
    }
    
    private Map<String, Object> applyReviewedWorkflow(final MCPInteractionClient interactionClient, final String planId, final String secretValue) throws IOException, InterruptedException {
        Map<String, Object> previewResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "preview"));
        assertThat(String.valueOf(previewResponse.get("status")), is("preview"));
        assertSecretRedacted(previewResponse, secretValue);
        List<String> approvedSteps = getMapList(previewResponse.get("preview_artifacts")).stream().map(each -> String.valueOf(each.get("approval_step"))).distinct().toList();
        Map<String, Object> result = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId, approvedSteps));
        assertSecretRedacted(result, secretValue);
        return result;
    }
    
    private Map<String, Object> createApplyArguments(final String planId, final List<String> approvedSteps) {
        return Map.of("plan_id", planId, "execution_mode", "review-then-execute", "approved_steps", approvedSteps);
    }
    
    private Map<String, Object> findItemByField(final List<Map<String, Object>> items, final String fieldName, final String expectedValue) {
        return items.stream().filter(each -> expectedValue.equalsIgnoreCase(String.valueOf(each.get(fieldName)))).findFirst()
                .orElseThrow(() -> new AssertionError(String.format("Failed to find item by %s=%s in %s", fieldName, expectedValue, items)));
    }
    
    private void assertSecretRedacted(final Map<String, Object> actual, final String secretValue) {
        if (!secretValue.isEmpty()) {
            assertFalse(String.valueOf(actual).contains(secretValue));
        }
    }
}
