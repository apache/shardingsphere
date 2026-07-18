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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator.ValidationResponse;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorPayload;
import org.apache.shardingsphere.mcp.core.tool.handler.metadata.ValidateRuntimeDatabaseToolHandler;
import org.apache.shardingsphere.mcp.core.tool.payload.RuntimeDatabaseValidationPayload;
import org.apache.shardingsphere.mcp.core.tool.payload.SQLExecutionPayload;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.tool.result.RuntimeDatabaseValidationCheckResult;
import org.apache.shardingsphere.mcp.support.database.tool.result.RuntimeDatabaseValidationResult;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class MCPCallToolResultFactoryTest extends AbstractMCPToolSpecificationFactoryTest {
    
    @Test
    void assertCreateToolSpecificationsHandleNullArguments() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPSuccessPayload response = new MCPMapPayload(Map.of("status", "ok"));
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            CallToolResult actual = createToolSpecification(MCPTransportType.STDIO).callHandler().apply(createExchange(), new CallToolRequest(
                    "database_gateway_search_metadata", null));
            assertThat(actual.structuredContent(), is(Map.of("status", "ok")));
            assertThat(((TextContent) actual.content().getFirst()).text(), is("{\"status\":\"ok\"}"));
            assertFalse(actual.isError());
        }
    }
    
    @Test
    void assertCreateError() {
        CallToolResult actual = new MCPCallToolResultFactory().create(new MCPErrorPayload(""));
        Map<String, Object> actualPayload = getTextContentPayload(actual);
        assertThat(actualPayload.get("response_mode"), is("recovery"));
        assertThat(actualPayload.get("summary"), is("Recovery guidance is available."));
        assertFalse(actualPayload.containsKey("message"));
        assertNull(actual.structuredContent());
        assertTrue(actual.isError());
    }
    
    @Test
    void assertCreateToolSpecificationsHandlePlainPayload() {
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapPayload(Map.of("message", "invalid_request")));
        assertFalse(actual.isError());
    }
    
    @Test
    void assertCreateWithRuntimeValidationDiagnostic() {
        MCPSuccessPayload response = RuntimeDatabaseValidationPayload.from(RuntimeDatabaseValidationResult.failed("",
                List.of(RuntimeDatabaseValidationCheckResult.failed("configuration", RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION,
                        "The requested database is not configured for this MCP runtime.")),
                RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION));
        CallToolResult actual = createRealDescriptorCallToolResult(ValidateRuntimeDatabaseToolHandler.TOOL_NAME, response);
        Map<String, Object> actualPayload = getStructuredContent(actual);
        assertFalse(actual.isError());
        assertThat(actualPayload.get("status"), is("failed"));
        assertFalse(((Map<?, ?>) actualPayload.get("recovery")).containsKey("database"));
        assertFalse(((Map<?, ?>) actualPayload.get("recovery")).containsKey("next_actions"));
        assertThat(((TextContent) actual.content().getFirst()).text(), is(JsonUtils.toJsonString(actualPayload)));
    }
    
    @Test
    void assertCreateErrorWithRuntimeValidationRecovery() {
        MCPErrorPayload errorPayload = MCPErrorConverter.convert(RuntimeDatabaseConnectionException.invalidConfiguration(
                "logic_db", new IllegalStateException("Invalid runtime database configuration.")));
        CallToolResult actual = new MCPCallToolResultFactory().create(errorPayload);
        assertTrue(actual.isError());
        assertNull(actual.structuredContent());
        assertThat(getTextContentPayload(actual).get("response_mode"), is("recovery"));
    }
    
    @Test
    void assertCreateWithExecutedResultSet() {
        SQLExecutionColumnDefinition column = new SQLExecutionColumnDefinition("order_id", "BIGINT", "BIGINT", false);
        MCPSuccessPayload response = SQLExecutionPayload.executed(SQLExecutionResult.resultSet(SupportedMCPStatement.DML, "SELECT",
                List.of(column), List.of(List.of(1L)), true, 1, 0, "WITH changed AS (...) SELECT order_id FROM changed"));
        CallToolResult actual = createRealDescriptorCallToolResult("database_gateway_execute_update", response);
        Map<String, Object> actualPayload = getStructuredContent(actual);
        assertFalse(actual.isError(), () -> String.valueOf(actualPayload));
        assertThat(actualPayload.get("columns"), is(List.of(column)));
        assertThat(actualPayload.get("rows"), is(List.of(List.of(1L))));
        assertThat(((Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst()).get("reason"), is(
                "The side-effecting statement already executed and returned truncated rows; do not replay it automatically. "
                        + "Use a separate read-only query if more data is needed."));
    }
    
    @Test
    void assertCreateWithMaskWorkflowPlanSchema() {
        CallToolResult actual = createRealDescriptorCallToolResult(MaskFeatureDefinition.PLAN_TOOL_NAME, createMaskPlanResponse());
        assertFalse(actual.isError(), () -> String.valueOf(actual.structuredContent()));
        assertThat(getStructuredContent(actual).get("status"), is("planned"));
    }
    
    @Test
    void assertCreateWithEncryptWorkflowPlanSchema() {
        CallToolResult actual = createRealDescriptorCallToolResult(EncryptFeatureDefinition.PLAN_TOOL_NAME, createEncryptPlanResponse());
        assertFalse(actual.isError(), () -> String.valueOf(actual.structuredContent()));
        assertThat(getStructuredContent(actual).get("status"), is("planned"));
        String actualText = ((TextContent) actual.content().getFirst()).text();
        assertFalse(actualText.contains("123456"));
        assertTrue(actualText.contains("******"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleResourceLinks() {
        Map<String, Object> payload = Map.of("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "read_first", "Read logical database.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapPayload(payload));
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        ResourceLink actualLink = (ResourceLink) actual.content().get(1);
        assertThat(actualLink.uri(), is("shardingsphere://databases/logic_db"));
        assertThat(actualLink.title(), is("logical-database"));
        assertThat(actualLink.mimeType(), is("application/json"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleSelfResourceLink() {
        Map<String, Object> payload = Map.of("self_resource",
                MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_self", "Read logical database.", "self_resource"));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapPayload(payload));
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("self_resource"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleItemResourceLinks() {
        Map<String, Object> payload = Map.of("items", List.of(Map.of(
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db/tables/t_order", "table", "inspect_detail", "Read table.", "resource"),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_parent", "Read database.", "parent_resource"),
                "next_resources", List.of(MCPResourceHintUtils.create(
                        "shardingsphere://databases/logic_db/tables/t_order/columns", "column-list", "inspect_children", "Read columns.", "next_resources")))));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapPayload(payload));
        assertThat(actual.content().size(), is(4));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://databases/logic_db/tables/t_order"));
        assertThat(((ResourceLink) actual.content().get(2)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((ResourceLink) actual.content().get(3)).uri(), is("shardingsphere://databases/logic_db/tables/t_order/columns"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("resource"));
    }
    
    @Test
    void assertCreateErrorWithRecoveryResourceLinks() {
        Map<String, Object> recovery = Map.of("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read capabilities.", "resources_to_read")));
        CallToolResult actual = new MCPCallToolResultFactory().create(new MCPErrorPayload("", recovery));
        Map<String, Object> actualPayload = getTextContentPayload(actual);
        assertTrue(actual.isError());
        assertNull(actual.structuredContent());
        assertThat(actualPayload.get("error_id"), isA(String.class));
        assertFalse(((Map<?, ?>) actualPayload.get("recovery")).containsKey("error_id"));
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://capabilities"));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.RESOURCE_LINKS_EMITTED), is(1));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleBoundedResourceLinks() {
        Map<String, Object> payload = Map.of(
                "next_resources", createResourceHints("shardingsphere://databases/next_", "next_resources", 30),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "inspect_parent", "Read parent.", "parent_resource"),
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read detail.", "resource"),
                "resources_to_read", List.of(MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read capabilities.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapPayload(payload));
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().size(), is(25));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.RESOURCE_LINKS_EMITTED), is(24));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.RESOURCE_LINKS_OMITTED), is(9));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://capabilities"));
        assertThat(((ResourceLink) actual.content().get(2)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((ResourceLink) actual.content().get(3)).uri(), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("resources_to_read"));
    }
    
    @Test
    void assertCreateToolSpecificationsIgnoreRawUriLink() {
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapPayload(Map.of("resource_uri", "shardingsphere://databases/logic_db")));
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertCreateToolSpecificationsIgnoreArbitraryNestedResourceHint() {
        Map<String, Object> payload = Map.of("debug", Map.of("resource", MCPResourceHintUtils.create(
                "shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read logical database.", "resource")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapPayload(payload));
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertToolOutputSchemaExamplesMatchSchemas() {
        JsonSchemaValidator validator = new DefaultJsonSchemaValidator();
        for (MCPToolDescriptor each : ToolDefinitionRegistry.getSupportedToolDescriptors()) {
            for (Map<String, Object> example : getOutputSchemaExamples(each)) {
                ValidationResponse actual = validator.validate(each.getOutputSchema(), example);
                assertTrue(actual.valid(), () -> String.format("Invalid outputSchema example for `%s`: %s", each.getName(), actual.errorMessage()));
            }
        }
    }
    
    private CallToolResult createRealDescriptorCallToolResult(final String toolName, final MCPSuccessPayload response) {
        MCPToolDescriptor descriptor = ToolDefinitionRegistry.getToolDefinition(toolName).getDescriptor();
        return new MCPCallToolResultFactory().create(descriptor, response);
    }
    
    private MCPSuccessPayload createMaskPlanResponse() {
        WorkflowContextSnapshot snapshot = createMaskSnapshot();
        Map<String, Object> payload = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest());
        payload.put("masked_property_preview", Map.of("primary",
                new MaskAlgorithmPropertyTemplateService().maskProperties(snapshot.getPropertyRequirements(), snapshot.getRequest().getPrimaryAlgorithmProperties())));
        return new MCPMapPayload(payload);
    }
    
    private WorkflowContextSnapshot createMaskSnapshot() {
        WorkflowRequest request = createWorkflowRequest();
        request.getPrimaryAlgorithmProperties().put("first-n", "3");
        request.getPrimaryAlgorithmProperties().put("last-m", "2");
        request.getPrimaryAlgorithmProperties().put("replace-char", "*");
        WorkflowContextSnapshot result = createWorkflowContextSnapshot(MaskFeatureDefinition.WORKFLOW_KIND, request);
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "first-n", true, false, "from", ""));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders (TYPE(NAME='keep_first_n_last_m'))"));
        return result;
    }
    
    private MCPSuccessPayload createEncryptPlanResponse() {
        WorkflowContextSnapshot snapshot = createEncryptSnapshot();
        Map<String, Object> payload = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest());
        payload.put("masked_property_preview", Map.of("primary",
                new EncryptAlgorithmPropertyTemplateService().maskProperties(snapshot.getPropertyRequirements(), snapshot.getRequest().getPrimaryAlgorithmProperties())));
        return new MCPMapPayload(payload);
    }
    
    private WorkflowContextSnapshot createEncryptSnapshot() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        WorkflowContextSnapshot result = createWorkflowContextSnapshot(EncryptFeatureDefinition.WORKFLOW_KIND, request);
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", ""));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        return result;
    }
    
    private WorkflowRequest createWorkflowRequest() {
        WorkflowRequest result = new WorkflowRequest();
        result.setDatabase("logic_db");
        result.setSchema("public");
        result.setTable("orders");
        return result;
    }
    
    private WorkflowContextSnapshot createWorkflowContextSnapshot(final WorkflowKind workflowKind, final WorkflowRequest request) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(workflowKind);
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        result.setClarifiedIntent(new ClarifiedIntent());
        result.setInteractionPlan(createInteractionPlan());
        return result;
    }
    
    private InteractionPlan createInteractionPlan() {
        InteractionPlan result = new InteractionPlan();
        result.setCurrentStep("review");
        result.setDeliveryMode("interactive");
        result.setExecutionMode("review-then-execute");
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOutputSchemaExamples(final MCPToolDescriptor toolDescriptor) {
        return toolDescriptor.getOutputSchema().containsKey("examples") ? (List<Map<String, Object>>) toolDescriptor.getOutputSchema().get("examples") : List.of();
    }
}
