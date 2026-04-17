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

import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

class HttpProductionProxyEncryptWorkflowE2ETest extends AbstractProductionProxyWorkflowE2ETest {
    
    @Test
    void assertPlanApplyAndValidateEncryptWorkflowThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> clarifyingResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("table", "orders", "column", "status", "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊"));
            assertThat(String.valueOf(clarifyingResponse.get("status")), is("clarifying"));
            assertThat(getStringList(clarifyingResponse.get("pending_questions")), is(List.of("请先提供 logical database。")));
            String planId = String.valueOf(clarifyingResponse.get("plan_id"));
            Map<String, Object> plannedResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("plan_id", planId, "database", getLogicalDatabaseName(), "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "123456abc")));
            assertThat(String.valueOf(plannedResponse.get("status")), is("planned"));
            assertThat(String.valueOf(plannedResponse.get("current_step")), is("review"));
            assertThat(getStringList(plannedResponse.get("global_steps")).size(), is(8));
            Map<String, Object> derivedColumnPlan = getMap(plannedResponse.get("derived_column_plan"));
            assertThat(String.valueOf(derivedColumnPlan.get("cipher_column_name")), is("status_cipher"));
            assertThat(String.valueOf(derivedColumnPlan.get("assisted_query_column_required")), is("false"));
            assertThat(String.valueOf(derivedColumnPlan.get("like_query_column_required")), is("false"));
            List<Map<String, Object>> ddlArtifacts = getMapList(plannedResponse.get("ddl_artifacts"));
            assertThat(ddlArtifacts.size(), is(1));
            assertThat(String.valueOf(ddlArtifacts.get(0).get("sql")), containsString("ADD COLUMN status_cipher"));
            Map<String, Object> maskedPropertyPreview = getMap(plannedResponse.get("masked_property_preview"));
            assertThat(String.valueOf(getMap(maskedPropertyPreview.get("primary")).get("aes-key-value")), is("******"));
            List<Map<String, Object>> distSqlArtifacts = getMapList(plannedResponse.get("distsql_artifacts"));
            assertThat(distSqlArtifacts.size(), is(1));
            assertThat(String.valueOf(distSqlArtifacts.get(0).get("sql")), containsString("'aes-key-value'='******'"));
            Map<String, Object> applyResponse = interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId));
            assertThat(String.valueOf(applyResponse.get("status")), is("completed"));
            assertThat(getMapList(applyResponse.get("step_results")).size(), is(2));
            assertThat(getStringList(applyResponse.get("executed_ddl")).size(), is(1));
            assertThat(getStringList(applyResponse.get("executed_distsql")).size(), is(1));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_assisted_query"), is(0));
            Map<String, Object> validationResponse = interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId));
            assertValidationPassed(validationResponse);
            Map<String, Object> ddlValidation = getMap(validationResponse.get("ddl_validation"));
            Map<String, Object> ruleValidation = getMap(validationResponse.get("rule_validation"));
            Map<String, Object> logicalMetadataValidation = getMap(validationResponse.get("logical_metadata_validation"));
            assertThat(String.valueOf(ddlValidation.get("status")), is("passed"));
            assertThat(String.valueOf(ruleValidation.get("status")), is("passed"));
            assertThat(String.valueOf(logicalMetadataValidation.get("status")), is("passed"));
            assertThat(String.valueOf(getMap(validationResponse.get("sql_executability_validation")).get("status")), is("passed"));
            Map<String, Object> ddlEvidence = getMap(ddlValidation.get("evidence"));
            Map<String, Object> ruleEvidence = getMap(ruleValidation.get("evidence"));
            assertThat(String.valueOf(ddlEvidence.get("cipher_column")), is("status_cipher"));
            assertThat(String.valueOf(ruleEvidence.get("logic_column")), is("status"));
            assertThat(String.valueOf(ruleEvidence.get("cipher_column")), is("status_cipher"));
            assertThat(String.valueOf(ruleEvidence.get("encryptor_type")).toUpperCase(Locale.ENGLISH), is("AES"));
        }
    }
    
    @Test
    void assertPlanRecommendsAssistedQueryEncryptWorkflowThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualClarifyingResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，需要等值，不需要模糊"));
            assertThat(String.valueOf(actualClarifyingResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualClarifyingResponse), hasItem(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
            assertThat(getStringList(actualClarifyingResponse.get("pending_questions")), is(List.of("请提供属性 `aes-key-value`。")));
            List<Map<String, Object>> actualRecommendations = getMapList(actualClarifyingResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(2));
            assertThat(String.valueOf(actualRecommendations.get(0).get("algorithm_role")), is("primary"));
            assertThat(String.valueOf(actualRecommendations.get(0).get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("AES"));
            assertThat(String.valueOf(actualRecommendations.get(1).get("algorithm_role")), is("assisted_query"));
            assertThat(String.valueOf(actualRecommendations.get(1).get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("MD5"));
            String planId = String.valueOf(actualClarifyingResponse.get("plan_id"));
            Map<String, Object> actualPlannedResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("plan_id", planId, "primary_algorithm_properties", Map.of("aes-key-value", "assisted-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            Map<String, Object> actualDerivedColumnPlan = getMap(actualPlannedResponse.get("derived_column_plan"));
            assertThat(String.valueOf(actualDerivedColumnPlan.get("cipher_column_name")), is("status_cipher"));
            assertThat(String.valueOf(actualDerivedColumnPlan.get("assisted_query_column_name")), is("status_assisted_query"));
            assertThat(String.valueOf(actualDerivedColumnPlan.get("assisted_query_column_required")), is("true"));
            assertThat(String.valueOf(actualDerivedColumnPlan.get("like_query_column_required")), is("false"));
            List<Map<String, Object>> actualDdlArtifacts = getMapList(actualPlannedResponse.get("ddl_artifacts"));
            assertThat(actualDdlArtifacts.size(), is(1));
            assertThat(String.valueOf(actualDdlArtifacts.get(0).get("sql")), containsString("ADD COLUMN status_assisted_query"));
            List<Map<String, Object>> actualIndexPlan = getMapList(actualPlannedResponse.get("index_plan"));
            assertThat(actualIndexPlan.size(), is(1));
            assertThat(String.valueOf(actualIndexPlan.get(0).get("sql")), containsString("status_assisted_query"));
            List<Map<String, Object>> actualDistSqlArtifacts = getMapList(actualPlannedResponse.get("distsql_artifacts"));
            assertThat(actualDistSqlArtifacts.size(), is(1));
            assertThat(String.valueOf(actualDistSqlArtifacts.get(0).get("sql")), containsString("ASSISTED_QUERY_COLUMN=status_assisted_query"));
            Map<String, Object> actualApplyResponse = interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId));
            assertThat(String.valueOf(actualApplyResponse.get("status")), is("completed"));
            assertThat(getMapList(actualApplyResponse.get("step_results")).size(), is(3));
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(2));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_assisted_query"), is(1));
            Map<String, Object> actualValidationResponse = interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(String.valueOf(getMap(actualValidationResponse.get("sql_executability_validation")).get("status")), is("passed"));
        }
    }
    
    @Test
    void assertPlanReportsLikeQueryCapabilityConflictThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，需要等值，需要模糊"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("clarifying"));
            List<String> actualIssueCodes = getIssueCodes(actualPlanResponse);
            assertThat(actualIssueCodes, hasItem(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
            assertThat(actualIssueCodes, hasItem(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
            assertThat(getMap(actualPlanResponse.get("derived_column_plan")).size(), is(0));
            List<Map<String, Object>> actualRecommendations = getMapList(actualPlanResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(2));
            assertFalse(actualRecommendations.stream().anyMatch(each -> "like_query".equals(each.get("algorithm_role"))));
        }
    }
    
    @Test
    void assertPlanAutoRenamesDerivedColumnWhenCipherColumnConflictsThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query",
                    Map.of("database", getLogicalDatabaseName(), "schema", "public",
                            "sql", "ALTER TABLE orders ADD COLUMN status_cipher VARCHAR(32), ADD COLUMN status_cipher_1 VARCHAR(32)"));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_cipher_1"), is(1));
            Map<String, Object> actualPlannedResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "renamed-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            assertThat(getIssueCodes(actualPlannedResponse), hasItem(WorkflowIssueCode.AUTO_RENAMED_DUE_TO_CONFLICT));
            Map<String, Object> actualDerivedColumnPlan = getMap(actualPlannedResponse.get("derived_column_plan"));
            assertThat(String.valueOf(actualDerivedColumnPlan.get("cipher_column_name")), is("status_cipher_2"));
            assertThat(String.valueOf(getMapList(actualPlannedResponse.get("ddl_artifacts")).get(0).get("sql")), containsString("ADD COLUMN status_cipher_2"));
            assertThat(String.valueOf(getMapList(actualPlannedResponse.get("distsql_artifacts")).get(0).get("sql")), containsString("CIPHER=status_cipher_2"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId)).get("status")), is("completed"));
            assertThat(countPhysicalColumn("status_cipher_2"), is(1));
            assertValidationPassed(interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId)));
        }
    }
    
    @Test
    void assertPlanKeepsSiblingEncryptRulesWhenCreatingSecondColumnThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualFirstPlanResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "first-secret")));
            assertThat(String.valueOf(actualFirstPlanResponse.get("status")), is("planned"));
            String firstPlanId = String.valueOf(actualFirstPlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", firstPlanId)).get("status")), is("completed"));
            assertValidationPassed(interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", firstPlanId)));
            Map<String, Object> actualSecondPlanResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "amount",
                            "natural_language_intent", "给 amount 做可逆加密，不需要等值，不需要模糊", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "second-secret")));
            assertThat(String.valueOf(actualSecondPlanResponse.get("status")), is("planned"));
            assertThat(String.valueOf(getMapList(actualSecondPlanResponse.get("distsql_artifacts")).get(0).get("sql")), containsString("ALTER ENCRYPT RULE orders"));
            String secondPlanId = String.valueOf(actualSecondPlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", secondPlanId)).get("status")), is("completed"));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("amount_cipher"), is(1));
            assertValidationEventuallyPassed(interactionClient, secondPlanId);
            List<Map<String, Object>> actualEncryptRules = getPayloadItems(
                    interactionClient.readResource(String.format("shardingsphere://databases/%s/encrypt-rules/orders", getLogicalDatabaseName())));
            assertThat(actualEncryptRules.size(), is(2));
            assertThat(String.valueOf(findItemByField(actualEncryptRules, "logic_column", "status").get("cipher_column")), is("status_cipher"));
            assertThat(String.valueOf(findItemByField(actualEncryptRules, "logic_column", "amount").get("cipher_column")), is("amount_cipher"));
        }
    }
    
    @Test
    void assertApplySupportsManualOnlyExecutionModeThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlannedResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "manual-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            Map<String, Object> actualApplyResponse = interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId, "execution_mode", "manual-only"));
            assertThat(String.valueOf(actualApplyResponse.get("status")), is("awaiting-manual-execution"));
            assertThat(getIssueCodes(actualApplyResponse), is(List.of(WorkflowIssueCode.MANUAL_EXECUTION_PENDING)));
            assertThat(getMapList(actualApplyResponse.get("step_results")).size(), is(0));
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(0));
            assertThat(countPhysicalColumn("status_cipher"), is(0));
            Map<String, Object> actualManualArtifactPackage = getMap(actualApplyResponse.get("manual_artifact_package"));
            assertThat(getMapList(actualManualArtifactPackage.get("ddl_artifacts")).size(), is(1));
            assertThat(getMapList(actualManualArtifactPackage.get("distsql_artifacts")).size(), is(1));
            assertThat(String.valueOf(getMapList(actualManualArtifactPackage.get("distsql_artifacts")).get(0).get("sql")),
                    containsString("'aes-key-value'='******'"));
        }
    }
    
    @Test
    void assertApplySupportsApprovedStepsThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlannedResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "approved-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            Map<String, Object> actualDdlOnlyApplyResponse = interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId, "approved_steps", List.of("ddl")));
            assertThat(String.valueOf(actualDdlOnlyApplyResponse.get("status")), is("completed"));
            assertThat(getStringList(actualDdlOnlyApplyResponse.get("executed_ddl")).size(), is(1));
            assertThat(getStringList(actualDdlOnlyApplyResponse.get("executed_distsql")).size(), is(0));
            assertThat(getStringList(actualDdlOnlyApplyResponse.get("skipped_artifacts")).size(), is(1));
            assertThat(String.valueOf(getMapList(actualDdlOnlyApplyResponse.get("step_results")).get(0).get("status")), is("passed"));
            assertThat(String.valueOf(getMapList(actualDdlOnlyApplyResponse.get("step_results")).get(1).get("status")), is("skipped"));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            Map<String, Object> actualFailedValidationResponse = interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId));
            assertValidationFailed(actualFailedValidationResponse);
            assertThat(String.valueOf(getMap(actualFailedValidationResponse.get("rule_validation")).get("status")), is("failed"));
            Map<String, Object> actualRuleOnlyApplyResponse = interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId, "approved_steps", List.of("rule_distsql")));
            assertThat(String.valueOf(actualRuleOnlyApplyResponse.get("status")), is("completed"));
            assertThat(getStringList(actualRuleOnlyApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualRuleOnlyApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(getStringList(actualRuleOnlyApplyResponse.get("skipped_artifacts")).size(), is(1));
            assertValidationPassed(interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId)));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateEncryptAlterWorkflowThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createEncryptRuleWithoutEquality(interactionClient);
            Map<String, Object> actualAlterPlanResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，修改成需要等值，不需要模糊", "operation_type", "alter",
                            "primary_algorithm_properties", Map.of("aes-key-value", "alter-secret")));
            assertThat(String.valueOf(actualAlterPlanResponse.get("status")), is("planned"));
            List<Map<String, Object>> actualDdlArtifacts = getMapList(actualAlterPlanResponse.get("ddl_artifacts"));
            assertThat(actualDdlArtifacts.size(), is(1));
            assertThat(String.valueOf(actualDdlArtifacts.get(0).get("sql")), containsString("ADD COLUMN status_assisted_query"));
            List<Map<String, Object>> actualDistSqlArtifacts = getMapList(actualAlterPlanResponse.get("distsql_artifacts"));
            assertThat(actualDistSqlArtifacts.size(), is(1));
            assertThat(String.valueOf(actualDistSqlArtifacts.get(0).get("sql")), containsString("ALTER ENCRYPT RULE orders"));
            String planId = String.valueOf(actualAlterPlanResponse.get("plan_id"));
            Map<String, Object> actualApplyResponse = interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId));
            assertThat(String.valueOf(actualApplyResponse.get("status")), is("completed"));
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(2));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_assisted_query"), is(1));
            assertValidationEventuallyPassed(interactionClient, planId);
        }
    }
    
    @Test
    void assertPlanApplyValidateAndReadEncryptResourcesWithCustomSpiAlgorithmThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call("plan_encrypt_mask_rule",
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊", "algorithm_type", "MCP_CUSTOM_REVERSIBLE"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            assertThat(getIssueCodes(actualPlanResponse), hasItem(WorkflowIssueCode.CUSTOM_ALGORITHM_CAPABILITY_UNCONFIRMED));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            assertThat(String.valueOf(interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId)).get("status")), is("completed"));
            assertValidationPassed(interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId)));
            List<Map<String, Object>> actualEncryptPluginItems = getPayloadItems(interactionClient.readResource("shardingsphere://plugins/encrypt-algorithms"));
            Map<String, Object> actualEncryptPlugin = findItemByField(actualEncryptPluginItems, "type", "MCP_CUSTOM_REVERSIBLE");
            assertThat(String.valueOf(actualEncryptPlugin.get("source")), is("custom-spi"));
            List<Map<String, Object>> actualEncryptRules = getPayloadItems(
                    interactionClient.readResource(String.format("shardingsphere://databases/%s/encrypt-rules", getLogicalDatabaseName())));
            Map<String, Object> actualEncryptRule = findItemByField(actualEncryptRules, "logic_column", "status");
            assertThat(String.valueOf(actualEncryptRule.get("encryptor_type")).toUpperCase(Locale.ENGLISH), is("MCP_CUSTOM_REVERSIBLE"));
            List<Map<String, Object>> actualSingleRuleItems = getPayloadItems(
                    interactionClient.readResource(String.format("shardingsphere://databases/%s/encrypt-rules/orders", getLogicalDatabaseName())));
            assertThat(actualSingleRuleItems.size(), is(1));
            assertThat(String.valueOf(actualSingleRuleItems.get(0).get("logic_column")), is("status"));
        }
    }
    
    private void createEncryptRuleWithoutEquality(final MCPInteractionClient interactionClient) throws Exception {
        Map<String, Object> actualCreatePlanResponse = interactionClient.call("plan_encrypt_mask_rule",
                Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                        "natural_language_intent", "给 status 做可逆加密，不需要等值，不需要模糊", "algorithm_type", "AES",
                        "primary_algorithm_properties", Map.of("aes-key-value", "base-secret")));
        assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
        String planId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
        assertThat(String.valueOf(interactionClient.call("apply_encrypt_mask_rule", Map.of("plan_id", planId)).get("status")), is("completed"));
        assertValidationPassed(interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId)));
    }
    
    private Map<String, Object> findItemByField(final List<Map<String, Object>> items, final String fieldName, final String expectedValue) {
        return items.stream().filter(each -> expectedValue.equalsIgnoreCase(String.valueOf(each.get(fieldName)))).findFirst()
                .orElseThrow(() -> new AssertionError(String.format("Failed to find item by %s=%s in %s", fieldName, expectedValue, items)));
    }
}
