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
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.core.workflow.InMemoryWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.handler.PlanEncryptRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowPlanningService;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.tool.handler.PlanMaskRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPToolSpecificationFactoryTest extends AbstractMCPToolSpecificationFactoryTest {
    
    @Test
    void assertCreateToolSpecifications() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS));
            List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().tool().name(), is("database_gateway_search_metadata"));
            assertThat(actual.getFirst().tool().title(), is("Search Metadata"));
            assertThat(actual.getFirst().tool().description(), is("Search database metadata."));
            assertThat(actual.getFirst().tool().inputSchema().type(), is("object"));
            assertThat(actual.getFirst().tool().inputSchema().required(), is(List.of("query")));
            assertFalse(actual.getFirst().tool().inputSchema().additionalProperties());
            assertThat(actual.getFirst().tool().inputSchema().properties().get("query"), is(Map.of("type", "string", "description", "Search query.")));
            assertThat(actual.getFirst().tool().inputSchema().properties().get("object_types"), is(Map.of(
                    "type", "array",
                    "description", "Optional object-type filter.",
                    "items", Map.of("type", "string", "description", "Object type.", "enum", List.of("TABLE", "VIEW")))));
            assertThat(actual.getFirst().tool().outputSchema(), is(Map.of("type", "object")));
            assertTrue(actual.getFirst().tool().annotations().readOnlyHint());
            assertNull(actual.getFirst().tool().annotations().returnDirect());
            assertThat(actual.getFirst().tool().meta(), is(Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://databases"))));
            assertNotNull(actual.getFirst().callHandler());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsMapAnnotationPresence() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor(
                    "fixture_declared_defaults", new MCPToolAnnotations(null, false, true, false, true))));
            MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS));
            List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
            assertNotNull(actual.getFirst().tool().annotations());
            assertFalse(actual.getFirst().tool().annotations().readOnlyHint());
            assertTrue(actual.getFirst().tool().annotations().destructiveHint());
            assertFalse(actual.getFirst().tool().annotations().idempotentHint());
            assertTrue(actual.getFirst().tool().annotations().openWorldHint());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleNullArguments() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPResponse response = new MCPMapResponse(Map.of("status", "ok"));
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            SyncToolSpecification actualSpecification = createToolSpecification("stdio");
            McpSyncServerExchange exchange = createExchange();
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_search_metadata", null));
            assertThat(actual.structuredContent(), is(Map.of("status", "ok")));
            assertThat(((TextContent) actual.content().getFirst()).text(), is("{\"status\":\"ok\"}"));
            assertFalse(actual.isError());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleErrorResponse() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of("query", "foo_query"), new MCPErrorResponse(""));
            CallToolResult actual = callTool(createToolSpecification("stdio"), createExchange(), "database_gateway_search_metadata", Map.of("query", "foo_query"));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualPayload = (Map<String, Object>) actual.structuredContent();
            assertThat(actualPayload.get("response_mode"), is("recovery"));
            assertThat(actualPayload.get("message"), is(""));
            assertTrue(actual.isError());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandlePlainPayload() {
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(Map.of("message", "invalid_request")));
        assertFalse(actual.isError());
    }
    
    @Test
    void assertCreateWithMaskWorkflowPlanSchema() throws ReflectiveOperationException {
        CallToolResult actual = createRealDescriptorCallToolResult(MaskFeatureDefinition.PLAN_TOOL_NAME, createMaskPlanResponse());
        assertFalse(actual.isError(), () -> String.valueOf(actual.structuredContent()));
        assertThat(getStructuredContent(actual).get("status"), is("planned"));
    }
    
    @Test
    void assertCreateWithEncryptWorkflowPlanSchema() throws ReflectiveOperationException {
        CallToolResult actual = createRealDescriptorCallToolResult(EncryptFeatureDefinition.PLAN_TOOL_NAME, createEncryptPlanResponse());
        assertFalse(actual.isError(), () -> String.valueOf(actual.structuredContent()));
        assertThat(getStructuredContent(actual).get("status"), is("planned"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleResourceLinks() {
        Map<String, Object> payload = Map.of("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "read_first", "Read logical database.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        ResourceLink actualLink = (ResourceLink) actual.content().get(1);
        assertThat(actualLink.uri(), is("shardingsphere://databases/logic_db"));
        assertThat(actualLink.title(), is("logical-database"));
        assertThat(actualLink.mimeType(), is("application/json"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleItemResourceLinks() {
        Map<String, Object> payload = Map.of("items", List.of(Map.of(
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db/tables/t_order", "table", "inspect_detail", "Read table.", "resource"),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_parent", "Read database.", "parent_resource"),
                "next_resources", List.of(MCPResourceHintUtils.create(
                        "shardingsphere://databases/logic_db/tables/t_order/columns", "column-list", "inspect_children", "Read columns.", "next_resources")))));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
        assertThat(actual.content().size(), is(4));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://databases/logic_db/tables/t_order"));
        assertThat(((ResourceLink) actual.content().get(2)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((ResourceLink) actual.content().get(3)).uri(), is("shardingsphere://databases/logic_db/tables/t_order/columns"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("resource"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleRecoveryResourceLinks() {
        Map<String, Object> recovery = Map.of("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read capabilities.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPErrorResponse("", recovery));
        assertTrue(actual.isError());
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleBoundedResourceLinks() {
        Map<String, Object> payload = Map.of(
                "next_resources", createResourceHints("shardingsphere://databases/next_", "next_resources", 30),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "inspect_parent", "Read parent.", "parent_resource"),
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read detail.", "resource"),
                "resources_to_read", List.of(MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read capabilities.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
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
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(Map.of("resource_uri", "shardingsphere://databases/logic_db")));
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertCreateToolSpecificationsIgnoreArbitraryNestedResourceHint() {
        Map<String, Object> payload = Map.of("debug", Map.of("resource", MCPResourceHintUtils.create(
                "shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read logical database.", "resource")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleToolDefinitionDescriptor() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("fixture_ping"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), new MCPMapResponse(Map.of("status", "ok")));
            CallToolResult actual = callTool(createToolSpecification("stdio"), createExchange(), "fixture_ping", Map.of());
            assertThat(actual.structuredContent(), is(Map.of("status", "ok")));
            assertThat(((TextContent) actual.content().getFirst()).text(), is("{\"status\":\"ok\"}"));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleUnsupportedToolAsProtocolError() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata")).thenThrow(UnsupportedToolException.class);
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = createToolSpecification(runtimeContext);
            McpSyncServerExchange exchange = createExchange();
            McpError actual = assertThrows(McpError.class, () -> callTool(actualSpecification, exchange, "database_gateway_search_metadata", Map.of()));
            assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
            assertThat(actual.getJsonRpcError().message(), is("Unsupported tool `database_gateway_search_metadata`."));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
            assertThat(actualData.get("message"), is("Unsupported tool `database_gateway_search_metadata`."));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsRejectInvalidInputSchema() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
        when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
        SyncToolSpecification actualSpecification = findToolSpecification(new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications(), "database_gateway_search_metadata");
        CallToolResult actual = callTool(actualSpecification, createExchange(), "database_gateway_search_metadata", Map.of("query", "order", "object_types", List.of("TABLE")));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualPayload = (Map<String, Object>) actual.structuredContent();
        Map<?, ?> actualRecovery = (Map<?, ?>) actualPayload.get("recovery");
        assertThat(actualPayload.get("message"), is("object_types[0] must be one of [database, schema, table, view, column, index, storage_unit, sequence]."));
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("field"), is("object_types[0]"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("database", "schema", "table", "view", "column", "index", "storage_unit", "sequence")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("query", "order")));
        assertTrue(actual.isError());
    }
    
    @Test
    void assertCreateToolSpecificationsValidateStructuredOutput() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createStrictToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), new MCPMapResponse(Map.of("count", 1)));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            CallToolResult actual = callTool(createToolSpecification(runtimeContext), createExchange(), "database_gateway_search_metadata", Map.of());
            @SuppressWarnings("unchecked")
            Map<String, Object> actualPayload = (Map<String, Object>) actual.structuredContent();
            assertTrue(String.valueOf(actualPayload.get("message")).contains("database_gateway_search_metadata"));
            assertTrue(actual.isError());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleInteractiveElicitation() {
        assertInteractiveElicitation(McpSchema.ClientCapabilities.builder().elicitation().build());
    }
    
    @Test
    void assertCreateToolSpecificationsHandleInteractiveElicitationWithFormOnlyClient() {
        assertInteractiveElicitation(createFormOnlyClientCapabilities());
    }
    
    @Test
    void assertCreateToolSpecificationsHandleInteractiveElicitationWithFormAndUrlClient() {
        assertInteractiveElicitation(createFormAndUrlClientCapabilities());
    }
    
    private void assertInteractiveElicitation(final McpSchema.ClientCapabilities clientCapabilities) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPResponse clarifyingResponse = new MCPMapResponse(createClarifyingPayload());
            MCPResponse plannedResponse = new MCPMapResponse(Map.of("status", "planned"));
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-id"), any()))
                    .thenReturn(clarifyingResponse, plannedResponse);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT,
                    Map.of("field_1", "foo_display", "field_2", true)), clientCapabilities);
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, toolName, Map.of());
            assertThat(actual.structuredContent(), is(Map.of("status", "planned")));
            ArgumentCaptor<McpSchema.ElicitRequest> requestCaptor = ArgumentCaptor.forClass(McpSchema.ElicitRequest.class);
            verify(exchange).createElicitation(requestCaptor.capture());
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.TOOL), is(toolName));
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.PLAN_ID), is("plan-1"));
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.FORM_REQUEST_ID), isA(String.class));
            assertThat(requestCaptor.getValue().requestedSchema(), is(createExpectedElicitRequestedSchema()));
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-id"), eq(createElicitedArguments())));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithSecretQuestion() {
        assertCreateToolSpecificationsSkipUnsafeElicitation(createClarifyingQuestion("custom_properties.display-name", "string", true, "Provide display name."));
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithSecretInputType() {
        assertCreateToolSpecificationsSkipUnsafeElicitation(createClarifyingQuestion("custom_properties.display-name", "secret", false, "Provide display name."));
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithSensitiveFieldName() {
        assertCreateToolSpecificationsSkipUnsafeElicitation(createClarifyingQuestion("primary_algorithm_properties.access-token", "string", false, "Provide access token."));
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWithUrlModeForSensitiveQuestion() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPResponse response = new MCPMapResponse(createClarifyingPayload(
                    createClarifyingQuestion("primary_algorithm_properties.access-token", "string", false, "Provide access token.")));
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()), createFormAndUrlClientCapabilities());
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "url_mode_not_implemented", true, true, "url_fallback");
            assertSanitizedSensitiveFallback(actual);
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithoutPlanId() {
        assertCreateToolSpecificationsSkipUnsafeElicitationWithPayload(createClarifyingPayloadWithoutPlanId(), "missing_plan_id", "structured_fallback");
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithUnknownAlgorithmSecretFlag() {
        assertCreateToolSpecificationsSkipUnsafeElicitationWithPayload(Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of(Map.of(
                        "field", "primary_algorithm_properties.props",
                        "input_type", "string",
                        "display_message", "Provide props."))),
                "sensitive_form_blocked", "url_fallback");
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithAmbiguousFieldBinding() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            Map<String, Object> expectedPayload = createClarifyingPayload(createClarifyingQuestion("requires_review", "boolean", false, "Require review?"));
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createAmbiguousPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", true)));
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "ambiguous_field_binding", true, false, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationForUrlOnlyClient() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createUrlOnlyElicitationExchange();
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "client_unsupported", false, true, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackForStreamableHttp() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPResponse response = new MCPMapResponse(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display")));
            CallToolResult actual = callTool(createToolSpecification("http"), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "remote_identity_required", true, false, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    private void assertCreateToolSpecificationsSkipUnsafeElicitation(final Map<String, Object> question) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            Map<String, Object> expectedPayload = createClarifyingPayload(question);
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("custom_properties.display-name", "foo_display")));
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "sensitive_form_blocked", true, false, "url_fallback");
            assertSanitizedSensitiveFallback(actual);
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    private void assertCreateToolSpecificationsSkipUnsafeElicitationWithPayload(final Map<String, Object> expectedPayload, final String expectedReason,
                                                                                final String expectedInteraction) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display")));
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, toolName, Map.of());
            assertStructuredFallback(actual, expectedReason, true, false, expectedInteraction);
            if ("url_fallback".equals(expectedInteraction)) {
                assertSanitizedSensitiveFallback(actual);
            }
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationForNonPlanningTool() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = callTool(createToolSpecification(runtimeContext), exchange, "database_gateway_search_metadata", Map.of());
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithoutRuntimeDescriptor() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("fixture_ping"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = callTool(createToolSpecification(runtimeContext), exchange, "fixture_ping", Map.of());
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWithoutElicitation() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPResponse response = new MCPMapResponse(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createExchange();
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "client_unsupported", false, false, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWithoutElicitationCapabilities() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPResponse response = new MCPMapResponse(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSchema.ClientCapabilities clientCapabilities = new McpSchema.ClientCapabilities(Collections.emptyMap(), null, null, null);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()), clientCapabilities);
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "client_unsupported", false, false, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitationDeclined() {
        assertCreateToolSpecificationsFallbackWhenElicitationAction(McpSchema.ElicitResult.Action.DECLINE);
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitationCancelled() {
        assertCreateToolSpecificationsFallbackWhenElicitationAction(McpSchema.ElicitResult.Action.CANCEL);
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitedContentHasExtraField() {
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(Map.of("field_1", "foo_display", "field_2", true, "field_3", "unexpected"));
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitedContentMissesField() {
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(Map.of("field_1", "foo_display"));
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitedContentHasBlankRequiredValue() {
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(Map.of("field_1", " ", "field_2", true));
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitedContentTypeMismatches() {
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(Map.of("field_1", 1, "field_2", true));
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitedContentViolatesAllowedValues() {
        Map<String, Object> expectedPayload = createClarifyingPayload(createClarifyingQuestion(
                "custom_properties.display-name", "string", false, "Provide display name.", List.of("foo_display")));
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(expectedPayload, Map.of("field_1", "bar_display"));
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitationFails() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPResponse response = new MCPMapResponse(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createThrowingElicitationExchange();
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "elicitation_failed", true, false, "structured_fallback");
            verify(exchange).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitationResultMalformed() {
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid("malformed_elicitation_result", new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, null));
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitationExpires() {
        try (
                MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class);
                MockedStatic<Clock> mockedClock = mockStatic(Clock.class)) {
            MutableClock clock = new MutableClock();
            mockedClock.when(Clock::systemUTC).thenReturn(clock);
            MCPResponse response = new MCPMapResponse(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display", "field_2", true)));
            when(exchange.createElicitation(any())).thenAnswer(invocation -> {
                clock.advance(Duration.ofMinutes(11L));
                return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display", "field_2", true));
            });
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "stale_elicitation", true, false, "structured_fallback");
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-id"), eq(createElicitedArguments())), never());
        }
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(final Map<String, Object> elicitedContent) {
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(createClarifyingPayload(), elicitedContent);
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(final Map<String, Object> expectedPayload, final Map<String, Object> elicitedContent) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, elicitedContent));
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "invalid_elicited_content", true, false, "structured_fallback");
            verify(exchange).createElicitation(any());
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-id"), eq(createElicitedArguments())), never());
        }
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(final String expectedReason, final McpSchema.ElicitResult elicitedResult) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPResponse response = new MCPMapResponse(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(elicitedResult);
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, expectedReason, true, false, "structured_fallback");
            verify(exchange).createElicitation(any());
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-id"), eq(createElicitedArguments())), never());
        }
    }
    
    private CallToolResult createRealDescriptorCallToolResult(final String toolName, final MCPResponse response) {
        MCPToolDescriptor descriptor = ToolDefinitionRegistry.getToolDefinition(toolName).getDescriptor();
        return new MCPCallToolResultFactory().create(descriptor, response);
    }
    
    private MCPResponse createMaskPlanResponse() throws ReflectiveOperationException {
        PlanMaskRuleToolHandler handler = new PlanMaskRuleToolHandler();
        MaskWorkflowPlanningService planningService = mock(MaskWorkflowPlanningService.class);
        when(planningService.plan(any(), any(), any(), any(), any())).thenReturn(createMaskSnapshot());
        setField(handler, "planningService", planningService);
        return handler.handle(createWorkflowContext(), new MCPToolCall("session-id", Map.of("database", "logic_db", "table", "orders", "column", "phone")));
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
    
    private MCPResponse createEncryptPlanResponse() throws ReflectiveOperationException {
        PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
        EncryptWorkflowPlanningService planningService = mock(EncryptWorkflowPlanningService.class);
        when(planningService.plan(any(), any(), any(), any(), any())).thenReturn(createEncryptSnapshot());
        setField(handler, "planningService", planningService);
        return handler.handle(createWorkflowContext(), new MCPToolCall("session-id", Map.of("database", "logic_db", "table", "orders", "column", "phone")));
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
    
    private MCPWorkflowHandlerContext createWorkflowContext() {
        MCPWorkflowHandlerContext result = mock(MCPWorkflowHandlerContext.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        when(result.getDatabaseContext()).thenReturn(databaseContext);
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(databaseContext.getMetadataQueryFacade()).thenReturn(mock(MCPMetadataQueryFacade.class));
        when(databaseContext.getQueryFacade()).thenReturn(mock(MCPFeatureQueryFacade.class));
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitationAction(final McpSchema.ElicitResult.Action action) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(action, Map.of()));
            CallToolResult actual = callTool(createToolSpecification("stdio"), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange).createElicitation(any());
        }
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
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOutputSchemaExamples(final MCPToolDescriptor toolDescriptor) {
        return toolDescriptor.getOutputSchema().containsKey("examples") ? (List<Map<String, Object>>) toolDescriptor.getOutputSchema().get("examples") : List.of();
    }
    
}
