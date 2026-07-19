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

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPModelContractAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ProxyWorkflowRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ProxyWorkflowRuntimeTestSupport.ProxyWorkflowRuntimeFixture;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractProductionProxyWorkflowE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private ProxyWorkflowRuntimeFixture runtimeFixture;
    
    private ProxyWorkflowRuntimeFixture sharedRuntimeFixture;
    
    private boolean sharedRuntimeFixtureSelected;
    
    private boolean clusterRuntimeFixtureSelected;
    
    @AfterEach
    void tearDownFixture() {
        if (sharedRuntimeFixtureSelected) {
            runtimeFixture = null;
            sharedRuntimeFixtureSelected = false;
            return;
        }
        if (null != runtimeFixture) {
            runtimeFixture.close();
            runtimeFixture = null;
        }
        clusterRuntimeFixtureSelected = false;
    }
    
    @AfterAll
    void tearDownSharedFixture() {
        if (null != sharedRuntimeFixture) {
            sharedRuntimeFixture.close();
            sharedRuntimeFixture = null;
        }
    }
    
    @Override
    protected final RuntimeTransport getTransport() {
        return RuntimeTransport.HTTP;
    }
    
    @Override
    protected final void prepareRuntimeFixture() throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(),
                () -> MySQLRuntimeTestSupport.createDockerRequiredMessage("Docker is required for the Proxy-backed workflow E2E tests."));
        try {
            if (sharedRuntimeFixtureSelected) {
                prepareSharedRuntimeFixture();
                return;
            }
            runtimeFixture = clusterRuntimeFixtureSelected ? ProxyWorkflowRuntimeTestSupport.createClusterFixture() : ProxyWorkflowRuntimeTestSupport.createFixture();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    private void prepareSharedRuntimeFixture() throws SQLException {
        if (null == sharedRuntimeFixture) {
            sharedRuntimeFixture = ProxyWorkflowRuntimeTestSupport.createFixture();
        }
        runtimeFixture = sharedRuntimeFixture;
    }
    
    @Override
    protected final Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeFixture.getRuntimeDatabases();
    }
    
    protected final String getLogicalDatabaseName() {
        return runtimeFixture.getLogicalDatabaseName();
    }
    
    protected final void useSharedReadOnlyRuntimeFixture() {
        sharedRuntimeFixtureSelected = true;
    }
    
    protected final void useClusterRuntimeFixture() {
        clusterRuntimeFixtureSelected = true;
    }
    
    protected final void assertValidationPassed(final Map<String, Object> actualValidationResponse) {
        assertThat(actualValidationResponse.toString(), String.valueOf(actualValidationResponse.get("status")), is("validated"));
        assertThat(actualValidationResponse.toString(), String.valueOf(actualValidationResponse.get("overall_status")), is("passed"));
        assertThat(actualValidationResponse.toString(), getObjectListOrEmpty(actualValidationResponse.get("issues")).size(), is(0));
        assertThat(actualValidationResponse.toString(), getObjectListOrEmpty(actualValidationResponse.get("mismatches")).size(), is(0));
        assertModelFacingPayloadContract(actualValidationResponse);
    }
    
    protected final void assertApplyCompleted(final Map<String, Object> actualApplyResponse) {
        assertThat(actualApplyResponse.toString(), String.valueOf(actualApplyResponse.get("status")), is("completed"));
        assertModelFacingPayloadContract(actualApplyResponse);
    }
    
    protected final Map<String, Object> applyReviewedWorkflow(final MCPInteractionClient interactionClient, final String planId) throws IOException, InterruptedException {
        return interactionClient.call(WorkflowToolDescriptors.APPLY_TOOL_NAME, createReviewThenExecuteArguments(planId, getApprovedSteps(previewWorkflow(interactionClient, planId))));
    }
    
    protected final Map<String, Object> previewWorkflow(final MCPInteractionClient interactionClient, final String planId) throws IOException, InterruptedException {
        Map<String, Object> result = interactionClient.call(WorkflowToolDescriptors.APPLY_TOOL_NAME, Map.of("plan_id", planId, "execution_mode", "preview"));
        assertThat(String.valueOf(result.get("status")), is("preview"));
        assertModelFacingPayloadContract(result);
        return result;
    }
    
    protected final List<String> getApprovedSteps(final Map<String, Object> previewResponse) {
        return getObjectListOrEmpty(previewResponse.get("preview_artifacts")).stream().map(each -> String.valueOf(each.get("approval_step"))).distinct().toList();
    }
    
    protected final Map<String, Object> createReviewThenExecuteArguments(final String planId, final List<String> approvedSteps) {
        return Map.of("plan_id", planId, "execution_mode", "review-then-execute", "approved_steps", approvedSteps);
    }
    
    protected final List<String> getIssueCodes(final Map<String, Object> payload) {
        return getObjectListOrEmpty(payload.get("issues")).stream().map(each -> String.valueOf(each.get("code"))).toList();
    }
    
    protected final List<String> getClarificationMessages(final Map<String, Object> payload) {
        return getObjectListOrEmpty(payload.get("clarification_questions")).stream().map(each -> String.valueOf(each.get("display_message"))).toList();
    }
    
    protected final void assertModelFacingPayloadContract(final Map<String, Object> payload) {
        MCPModelContractAssertions.assertCanonicalNextActionLists(payload);
    }
    
    protected final Map<String, Object> findItemByField(final List<Map<String, Object>> items, final String fieldName, final String expectedValue) {
        return items.stream().filter(each -> expectedValue.equalsIgnoreCase(String.valueOf(each.get(fieldName)))).findFirst()
                .orElseThrow(() -> new AssertionError(String.format("Failed to find item by %s=%s in %s", fieldName, expectedValue, items)));
    }
    
    protected final List<String> getStringListOrEmpty(final Object value) {
        return null == value ? List.of() : ((List<?>) value).stream().map(String::valueOf).toList();
    }
    
    protected final List<Map<String, Object>> getObjectListOrEmpty(final Object value) {
        return null == value ? List.of() : MCPInteractionPayloads.getRequiredObjectList(value, "payload");
    }
    
    protected final Map<String, Object> getObjectOrEmpty(final Object value) {
        return null == value ? Map.of() : MCPInteractionPayloads.getRequiredObjectValue(value, "payload");
    }
    
    protected final Map<String, Object> getValidationSection(final Map<String, Object> payload, final String layer) {
        return getObjectListOrEmpty(payload.get("sections")).stream().filter(each -> layer.equals(each.get("layer"))).findFirst().orElse(Map.of());
    }
}
