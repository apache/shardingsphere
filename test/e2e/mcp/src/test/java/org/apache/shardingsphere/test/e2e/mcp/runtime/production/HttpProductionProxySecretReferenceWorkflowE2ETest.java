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

import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.fixture.MCPWorkflowSecretReferenceFixture;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class HttpProductionProxySecretReferenceWorkflowE2ETest extends AbstractProductionProxyWorkflowE2ETest {
    
    private static final String PLAN_TOOL_NAME = "database_gateway_plan_encrypt_rule";
    
    private static final String APPLY_TOOL_NAME = WorkflowToolDescriptors.APPLY_TOOL_NAME;
    
    private static final String RULES_RESOURCE_URI = "shardingsphere://features/encrypt/databases/%s/rules";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertSecretReferenceApplyRequiresManualExecutionThroughProxy() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> planResponse = planSecretReferencedEncryptRule(interactionClient);
            String planId = String.valueOf(planResponse.get("plan_id"));
            assertPlannedSecretReferencePayload(planResponse);
            Map<String, Object> previewResponse = previewWorkflow(interactionClient, planId);
            assertSecretReferencedPreview(previewResponse);
            Map<String, Object> applyResponse = interactionClient.call(APPLY_TOOL_NAME, createApplyArguments(planId, getApprovedSteps(previewResponse)));
            assertThat(String.valueOf(applyResponse.get("response_mode")), is("recovery"));
            assertThat(String.valueOf(applyResponse.get("status")), is("failed"));
            assertThat(String.valueOf(applyResponse.get("category")), is(MCPDiagnosticCategory.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED));
            assertThat(getMapList(applyResponse.get("step_results")).size(), is(0));
            assertThat(getStringList(applyResponse.get("executed_distsql")).size(), is(0));
            assertThat(getStringList(applyResponse.get("applied_artifacts")).size(), is(0));
            MCPWorkflowSecretReferenceFixture.assertSecretReferenceRedacted(applyResponse);
            assertTrue(getPayloadItems(interactionClient.readResource(String.format(RULES_RESOURCE_URI, getLogicalDatabaseName()))).isEmpty());
        }
    }
    
    private Map<String, Object> planSecretReferencedEncryptRule(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        return interactionClient.call(PLAN_TOOL_NAME,
                Map.of("database", getLogicalDatabaseName(), "table", "orders", "column", "status",
                        "natural_language_intent", "encrypt status with reversible encryption, no equality, no like", "algorithm_type", "AES",
                        "cipher_column_name", "status_cipher",
                        "primary_algorithm_properties", Map.of("aes-key-value", MCPWorkflowSecretReferenceFixture.createSecretReferenceInput())));
    }
    
    private void assertPlannedSecretReferencePayload(final Map<String, Object> planResponse) {
        assertThat(String.valueOf(planResponse.get("status")), is("planned"));
        Map<String, Object> secretReferenceSummary = getMap(planResponse.get("secret_reference_summary"));
        assertTrue((Boolean) secretReferenceSummary.get("required"));
        assertThat(secretReferenceSummary.get("reference_count"), is(1));
        Map<String, Object> secretReference = getMapList(secretReferenceSummary.get("references")).getFirst();
        assertThat(String.valueOf(secretReference.get("algorithm_role")), is("primary"));
        assertThat(String.valueOf(secretReference.get("property_key")), is("aes-key-value"));
        assertThat(String.valueOf(secretReference.get("label")), is("secret_placeholder:primary.aes-key-value"));
        assertThat(String.valueOf(secretReference.get("manual_placeholder")), is("<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>"));
        MCPWorkflowSecretReferenceFixture.assertSecretReferenceRedacted(planResponse);
    }
    
    private Map<String, Object> previewWorkflow(final MCPInteractionClient interactionClient, final String planId) throws IOException, InterruptedException {
        return interactionClient.call(APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "preview"));
    }
    
    private void assertSecretReferencedPreview(final Map<String, Object> previewResponse) {
        assertThat(String.valueOf(previewResponse.get("status")), is("preview"));
        List<Map<String, Object>> previewArtifacts = getMapList(previewResponse.get("preview_artifacts"));
        assertThat(previewArtifacts.size(), is(1));
        assertThat(String.valueOf(previewArtifacts.getFirst().get("sql")), containsString("'aes-key-value'='<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>'"));
        assertFalse(String.valueOf(previewArtifacts.getFirst().get("sql")).contains("secret_reference:primary.aes-key-value"));
        MCPWorkflowSecretReferenceFixture.assertSecretReferenceRedacted(previewResponse);
    }
    
    private List<String> getApprovedSteps(final Map<String, Object> previewResponse) {
        return getMapList(previewResponse.get("preview_artifacts")).stream().map(each -> String.valueOf(each.get("approval_step"))).distinct().toList();
    }
    
    private Map<String, Object> createApplyArguments(final String planId, final List<String> approvedSteps) {
        return Map.of("plan_id", planId, "execution_mode", "review-then-execute", "approved_steps", approvedSteps);
    }
}
