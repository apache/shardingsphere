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
    
    private static final String PLAN_TOOL_NAME = "database_gateway_plan_encrypt_rule";
    
    private static final String PLAN_PROMPT_NAME = "plan_encrypt_rule";
    
    private static final String APPLY_TOOL_NAME = WorkflowToolDescriptors.APPLY_TOOL_NAME;
    
    private static final String VALIDATE_TOOL_NAME = WorkflowToolDescriptors.VALIDATE_TOOL_NAME;
    
    private static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/encrypt/algorithms";
    
    private static final String RULES_RESOURCE_URI = "shardingsphere://features/encrypt/databases/%s/rules";
    
    private static final String TABLE_RULES_RESOURCE_URI = "shardingsphere://features/encrypt/databases/%s/tables/%s/rules";
    
    @Test
    void assertCompleteEncryptAlgorithmThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.complete(Map.of("type", "ref/prompt", "name", PLAN_PROMPT_NAME), "algorithm_type", "AE", Map.of());
            assertThat(getStringList(getMap(actual.get("completion")).get("values")), hasItem("AES"));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateEncryptWorkflowThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> clarifyingResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("table", "orders", "column", "status", "natural_language_intent", "encrypt status with reversible encryption, no equality, no like"));
            assertThat(String.valueOf(clarifyingResponse.get("status")), is("clarifying"));
            assertThat(getClarificationMessages(clarifyingResponse), is(List.of("Please provide logical database first.")));
            String planId = String.valueOf(clarifyingResponse.get("plan_id"));
            Map<String, Object> plannedResponse = interactionClient.call(PLAN_TOOL_NAME,
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
            Map<String, Object> applyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId));
            assertApplyCompleted(applyResponse);
            assertThat(getMapList(applyResponse.get("step_results")).size(), is(2));
            assertThat(getStringList(applyResponse.get("executed_ddl")).size(), is(1));
            assertThat(getStringList(applyResponse.get("executed_distsql")).size(), is(1));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_assisted_query"), is(0));
            Map<String, Object> validationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
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
            Map<String, Object> actualClarifyingResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, requires equality, no like"));
            assertThat(String.valueOf(actualClarifyingResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualClarifyingResponse), hasItem(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
            assertThat(getClarificationMessages(actualClarifyingResponse), is(List.of("Please provide property `aes-key-value`.")));
            List<Map<String, Object>> actualRecommendations = getMapList(actualClarifyingResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(2));
            assertThat(String.valueOf(actualRecommendations.get(0).get("algorithm_role")), is("primary"));
            assertThat(String.valueOf(actualRecommendations.get(0).get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("AES"));
            assertThat(String.valueOf(actualRecommendations.get(1).get("algorithm_role")), is("assisted_query"));
            assertThat(String.valueOf(actualRecommendations.get(1).get("algorithm_type")).toUpperCase(Locale.ENGLISH), is("MD5"));
            String planId = String.valueOf(actualClarifyingResponse.get("plan_id"));
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
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
            Map<String, Object> actualApplyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId));
            assertApplyCompleted(actualApplyResponse);
            assertThat(getMapList(actualApplyResponse.get("step_results")).size(), is(3));
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(2));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_assisted_query"), is(1));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(String.valueOf(getMap(actualValidationResponse.get("sql_executability_validation")).get("status")), is("passed"));
        }
    }
    
    @Test
    void assertPlanReportsLikeQueryCapabilityConflictThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, requires equality and LIKE query"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("clarifying"));
            List<String> actualIssueCodes = getIssueCodes(actualPlanResponse);
            assertThat(actualIssueCodes, hasItem(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
            assertThat(getMap(actualPlanResponse.get("derived_column_plan")).size(), is(0));
            List<Map<String, Object>> actualRecommendations = getMapList(actualPlanResponse.get("algorithm_recommendations"));
            assertThat(actualRecommendations.size(), is(2));
            assertFalse(actualRecommendations.stream().anyMatch(each -> "like_query".equals(each.get("algorithm_role"))));
        }
    }
    
    @Test
    void assertPlanAutoRenamesDerivedColumnWhenCipherColumnConflictsThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update",
                    Map.of("database", getLogicalDatabaseName(), "schema", "public",
                            "sql", "ALTER TABLE orders ADD COLUMN status_cipher VARCHAR(32), ADD COLUMN status_cipher_1 VARCHAR(32)",
                            "execution_mode", "execute"));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_cipher_1"), is(1));
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "renamed-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            assertThat(getIssueCodes(actualPlannedResponse), hasItem(WorkflowIssueCode.AUTO_RENAMED_DUE_TO_CONFLICT));
            Map<String, Object> actualDerivedColumnPlan = getMap(actualPlannedResponse.get("derived_column_plan"));
            assertThat(String.valueOf(actualDerivedColumnPlan.get("cipher_column_name")), is("status_cipher_2"));
            assertThat(String.valueOf(getMapList(actualPlannedResponse.get("ddl_artifacts")).get(0).get("sql")), containsString("ADD COLUMN status_cipher_2"));
            assertThat(String.valueOf(getMapList(actualPlannedResponse.get("distsql_artifacts")).get(0).get("sql")), containsString("CIPHER=status_cipher_2"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            assertApplyCompleted(interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId)));
            assertThat(countPhysicalColumn("status_cipher_2"), is(1));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
        }
    }
    
    @Test
    void assertPlanRejectsUnsupportedSecondEncryptColumnThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualFirstPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "first-secret")));
            assertThat(String.valueOf(actualFirstPlanResponse.get("status")), is("planned"));
            String firstPlanId = String.valueOf(actualFirstPlanResponse.get("plan_id"));
            assertApplyCompleted(interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(firstPlanId)));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", firstPlanId)));
            Map<String, Object> actualSecondPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "amount",
                            "natural_language_intent", "encrypt amount with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "second-secret")));
            assertThat(String.valueOf(actualSecondPlanResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualSecondPlanResponse), hasItem(WorkflowIssueCode.ENCRYPT_ALTER_SCOPE_LIMITED));
            assertFalse(getClarificationMessages(actualSecondPlanResponse).isEmpty());
            assertThat(getMapList(actualSecondPlanResponse.get("ddl_artifacts")).size(), is(0));
            assertThat(getMapList(actualSecondPlanResponse.get("distsql_artifacts")).size(), is(0));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("amount_cipher"), is(0));
            List<Map<String, Object>> actualEncryptRules = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualEncryptRules.size(), is(1));
            assertThat(String.valueOf(findItemByField(actualEncryptRules, "logic_column", "status").get("cipher_column")), is("status_cipher"));
        }
    }
    
    @Test
    void assertApplySupportsManualOnlyExecutionModeThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "manual-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            Map<String, Object> actualApplyResponse = interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "manual-only"));
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
            Map<String, Object> actualPlannedResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                            "primary_algorithm_properties", Map.of("aes-key-value", "approved-secret")));
            assertThat(String.valueOf(actualPlannedResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlannedResponse.get("plan_id"));
            Map<String, Object> actualDdlOnlyApplyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId, List.of("ddl")));
            assertThat(String.valueOf(actualDdlOnlyApplyResponse.get("status")), is("completed"));
            assertThat(getStringList(actualDdlOnlyApplyResponse.get("executed_ddl")).size(), is(1));
            assertThat(getStringList(actualDdlOnlyApplyResponse.get("executed_distsql")).size(), is(0));
            assertThat(getStringList(actualDdlOnlyApplyResponse.get("skipped_artifacts")).size(), is(1));
            assertThat(String.valueOf(getMapList(actualDdlOnlyApplyResponse.get("step_results")).get(0).get("status")), is("passed"));
            assertThat(String.valueOf(getMapList(actualDdlOnlyApplyResponse.get("step_results")).get(1).get("status")), is("skipped"));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            Map<String, Object> actualFailedValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationFailed(actualFailedValidationResponse);
            assertThat(String.valueOf(getMap(actualFailedValidationResponse.get("rule_validation")).get("status")), is("failed"));
            Map<String, Object> actualRuleOnlyApplyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId, List.of("rule_distsql")));
            assertThat(String.valueOf(actualRuleOnlyApplyResponse.get("status")), is("completed"));
            assertThat(getStringList(actualRuleOnlyApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualRuleOnlyApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(getStringList(actualRuleOnlyApplyResponse.get("skipped_artifacts")).size(), is(1));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
        }
    }
    
    @Test
    void assertPlanRejectsUnsupportedEncryptAlterExpansionThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createEncryptRuleWithoutEquality(interactionClient);
            Map<String, Object> actualAlterPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, update to require equality and no like", "operation_type", "alter",
                            "primary_algorithm_properties", Map.of("aes-key-value", "alter-secret")));
            assertThat(String.valueOf(actualAlterPlanResponse.get("status")), is("clarifying"));
            assertThat(getIssueCodes(actualAlterPlanResponse), hasItem(WorkflowIssueCode.ENCRYPT_ALTER_SCOPE_LIMITED));
            assertFalse(getClarificationMessages(actualAlterPlanResponse).isEmpty());
            assertThat(getMapList(actualAlterPlanResponse.get("ddl_artifacts")).size(), is(0));
            assertThat(getMapList(actualAlterPlanResponse.get("distsql_artifacts")).size(), is(0));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            assertThat(countPhysicalColumn("status_assisted_query"), is(0));
        }
    }
    
    @Test
    void assertPlanApplyAndValidateEncryptDropWorkflowThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            createEncryptRuleWithoutEquality(interactionClient);
            Map<String, Object> actualDropPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status", "operation_type", "drop"));
            assertThat(String.valueOf(actualDropPlanResponse.get("status")), is("planned"));
            assertThat(getClarificationMessages(actualDropPlanResponse), is(List.of()));
            assertThat(getIssueCodes(actualDropPlanResponse), hasItem(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED));
            assertThat(getIssueCodes(actualDropPlanResponse), hasItem(WorkflowIssueCode.PHYSICAL_CLEANUP_REQUIRED));
            assertThat(getMapList(actualDropPlanResponse.get("ddl_artifacts")).size(), is(0));
            assertThat(String.valueOf(getMapList(actualDropPlanResponse.get("distsql_artifacts")).get(0).get("sql")), is("DROP ENCRYPT RULE orders"));
            String planId = String.valueOf(actualDropPlanResponse.get("plan_id"));
            Map<String, Object> actualApplyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId));
            assertApplyCompleted(actualApplyResponse);
            assertThat(getStringList(actualApplyResponse.get("executed_ddl")).size(), is(0));
            assertThat(getStringList(actualApplyResponse.get("executed_distsql")).size(), is(1));
            assertThat(countPhysicalColumn("status_cipher"), is(1));
            Map<String, Object> actualValidationResponse = interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId));
            assertValidationPassed(actualValidationResponse);
            assertThat(String.valueOf(getMap(actualValidationResponse.get("ddl_validation")).get("status")), is("skipped"));
            assertThat(String.valueOf(getMap(actualValidationResponse.get("rule_validation")).get("status")), is("passed"));
        }
    }
    
    @Test
    void assertPlanApplyValidateAndReadEncryptResourcesWithCustomAlgorithmThroughProxy() throws Exception {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualPlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                    Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                            "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "MCP_CUSTOM_REVERSIBLE"));
            assertThat(String.valueOf(actualPlanResponse.get("status")), is("planned"));
            String planId = String.valueOf(actualPlanResponse.get("plan_id"));
            assertApplyCompleted(interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId)));
            assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
            List<Map<String, Object>> actualEncryptPluginItems = getPayloadItems(interactionClient.readResource(ALGORITHMS_RESOURCE_URI));
            assertThat(String.valueOf(findItemByField(actualEncryptPluginItems, "type", "MCP_CUSTOM_REVERSIBLE").get("type")), is("MCP_CUSTOM_REVERSIBLE"));
            List<Map<String, Object>> actualEncryptRules = getPayloadItems(
                    interactionClient.readResource(String.format(RULES_RESOURCE_URI, getLogicalDatabaseName())));
            Map<String, Object> actualEncryptRule = findItemByField(actualEncryptRules, "logic_column", "status");
            assertThat(String.valueOf(actualEncryptRule.get("encryptor_type")).toUpperCase(Locale.ENGLISH), is("MCP_CUSTOM_REVERSIBLE"));
            List<Map<String, Object>> actualSingleRuleItems = getPayloadItems(
                    interactionClient.readResource(String.format(TABLE_RULES_RESOURCE_URI, getLogicalDatabaseName(), "orders")));
            assertThat(actualSingleRuleItems.size(), is(1));
            assertThat(String.valueOf(actualSingleRuleItems.get(0).get("logic_column")), is("status"));
        }
    }
    
    private void createEncryptRuleWithoutEquality(final MCPInteractionClient interactionClient) throws Exception {
        createEncryptRuleWithoutEquality(interactionClient, "status", "base-secret");
    }
    
    private void createEncryptRuleWithoutEquality(final MCPInteractionClient interactionClient, final String columnName, final String secret) throws Exception {
        Map<String, Object> actualCreatePlanResponse = interactionClient.call(PLAN_TOOL_NAME,
                Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", columnName,
                        "natural_language_intent", String.format("encrypt %s with reversible encryption, no equality, no like", columnName), "algorithm_type", "AES",
                        "primary_algorithm_properties", Map.of("aes-key-value", secret)));
        assertThat(String.valueOf(actualCreatePlanResponse.get("status")), is("planned"));
        String planId = String.valueOf(actualCreatePlanResponse.get("plan_id"));
        assertApplyCompleted(interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId)));
        assertValidationPassed(interactionClient.call(VALIDATE_TOOL_NAME, Map.of("plan_id", planId)));
    }
    
    private Map<String, Object> createApplyArguments(final String planId) {
        return Map.of("plan_id", planId, "execution_mode", "review-then-execute");
    }
    
    private Map<String, Object> createApplyArguments(final String planId, final List<String> approvedSteps) {
        return Map.of("plan_id", planId, "execution_mode", "review-then-execute", "approved_steps", approvedSteps);
    }
    
    private Map<String, Object> findItemByField(final List<Map<String, Object>> items, final String fieldName, final String expectedValue) {
        return items.stream().filter(each -> expectedValue.equalsIgnoreCase(String.valueOf(each.get(fieldName)))).findFirst()
                .orElseThrow(() -> new AssertionError(String.format("Failed to find item by %s=%s in %s", fieldName, expectedValue, items)));
    }
}
