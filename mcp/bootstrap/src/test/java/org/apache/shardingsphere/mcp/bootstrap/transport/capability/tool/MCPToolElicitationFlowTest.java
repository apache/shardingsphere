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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPToolElicitationFlowTest extends AbstractMCPToolSpecificationFactoryTest {
    
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
    
    @Test
    void assertCreateToolSpecificationsHandleContinuationError() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), any()))
                    .thenReturn(new MCPMapPayload(createClarifyingPayload()))
                    .thenThrow(new DatabaseCapabilityNotFoundException());
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT,
                    Map.of("field_1", "foo_display", "field_2", true)));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertTrue(actual.isError());
            assertNull(actual.structuredContent());
            assertThat(getTextContentPayload(actual).get("message"), is("Database capability does not exist."));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsElicitKeyGeneratorField() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName,
                    Map.of("key_generator", Map.of("type", "string", "description", "Key generator name."))));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), any()))
                    .thenReturn(new MCPMapPayload(createClarifyingPayload(createClarifyingQuestion("key_generator", "string", false, "Provide key generator name."))),
                            new MCPMapPayload(Map.of("status", "planned")));
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "snowflake_generator")));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertThat(actual.structuredContent(), is(Map.of("status", "planned")));
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition),
                    eq(Map.of("plan_id", "plan-1", "key_generator", "snowflake_generator"))));
        }
    }
    
    private void assertInteractiveElicitation(final McpSchema.ClientCapabilities clientCapabilities) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPSuccessPayload clarifyingResponse = new MCPMapPayload(createClarifyingPayload());
            MCPSuccessPayload plannedResponse = new MCPMapPayload(Map.of("status", "planned"));
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), any()))
                    .thenReturn(clarifyingResponse, plannedResponse);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT,
                    Map.of("field_1", "foo_display", "field_2", true)), clientCapabilities);
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertThat(actual.structuredContent(), is(Map.of("status", "planned")));
            ArgumentCaptor<McpSchema.ElicitRequest> requestCaptor = ArgumentCaptor.forClass(McpSchema.ElicitRequest.class);
            verify(exchange).createElicitation(requestCaptor.capture());
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.TOOL), is(toolName));
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.PLAN_ID), is("plan-1"));
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.FORM_REQUEST_ID), isA(String.class));
            assertThat(requestCaptor.getValue().requestedSchema(), is(createExpectedElicitRequestedSchema()));
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(createElicitedArguments())));
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
    void assertCreateToolSpecificationsFallbackWithUrlModeForSensitiveQuestion() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPSuccessPayload response = new MCPMapPayload(createClarifyingPayload(
                    createClarifyingQuestion("primary_algorithm_properties.access-token", "string", true, "Provide access token.")));
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()), createFormAndUrlClientCapabilities());
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "url_mode_not_implemented", true, true, "structured_fallback");
            assertSanitizedSensitiveFallback(actual);
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithoutPlanId() {
        assertCreateToolSpecificationsSkipUnsafeElicitationWithPayload(createClarifyingPayloadWithoutPlanId(), "missing_plan_id", "structured_fallback");
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithoutSecretMetadata() {
        assertCreateToolSpecificationsSkipUnsafeElicitationWithPayload(Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of(Map.of(
                        "field", "primary_algorithm_properties.props",
                        "input_type", "string",
                        "display_message", "Provide props."))),
                "sensitive_form_blocked", "structured_fallback");
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithAmbiguousFieldBinding() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            Map<String, Object> expectedPayload = createClarifyingPayload(createClarifyingQuestion("requires_review", "boolean", false, "Require review?"));
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createAmbiguousPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", true)));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "ambiguous_field_binding", true, false, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationForUrlOnlyClient() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createUrlOnlyElicitationExchange();
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "client_unsupported", false, true, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackForStreamableHttp() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPSuccessPayload response = new MCPMapPayload(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display")));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.HTTP), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "remote_identity_required", true, false, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    private void assertCreateToolSpecificationsSkipUnsafeElicitation(final Map<String, Object> question) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            Map<String, Object> expectedPayload = createClarifyingPayload(question);
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("custom_properties.display-name", "foo_display")));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertStructuredFallback(actual, "sensitive_form_blocked", true, false, "structured_fallback");
            assertSanitizedSensitiveFallback(actual);
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    private void assertCreateToolSpecificationsSkipUnsafeElicitationWithPayload(final Map<String, Object> expectedPayload, final String expectedReason,
                                                                                final String expectedInteraction) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display")));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, toolName, Map.of());
            assertStructuredFallback(actual, expectedReason, true, false, expectedInteraction);
            if ("sensitive_form_blocked".equals(expectedReason) || "url_mode_not_implemented".equals(expectedReason)) {
                assertSanitizedSensitiveFallback(actual);
            }
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationForNonPlanningTool() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = callTool(createToolSpecification(createRuntimeContext(MCPTransportType.HTTP)), exchange, "database_gateway_search_metadata", Map.of());
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithoutRuntimeDescriptor() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("fixture_ping"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = callTool(createToolSpecification(createRuntimeContext(MCPTransportType.HTTP)), exchange, "fixture_ping", Map.of());
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWithoutElicitation() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPSuccessPayload response = new MCPMapPayload(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createExchange();
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "client_unsupported", false, false, "structured_fallback");
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWithoutElicitationCapabilities() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPSuccessPayload response = new MCPMapPayload(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSchema.ClientCapabilities clientCapabilities = new McpSchema.ClientCapabilities(Collections.emptyMap(), null, null, null);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()), clientCapabilities);
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, "database_gateway_plan_encrypt_rule", Map.of());
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
            MCPSuccessPayload response = new MCPMapPayload(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createThrowingElicitationExchange();
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, "database_gateway_plan_encrypt_rule", Map.of());
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
            MCPSuccessPayload response = new MCPMapPayload(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display", "field_2", true)));
            when(exchange.createElicitation(any())).thenAnswer(invocation -> {
                clock.advance(Duration.ofMinutes(11L));
                return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display", "field_2", true));
            });
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "stale_elicitation", true, false, "structured_fallback");
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(createElicitedArguments())), never());
        }
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(final Map<String, Object> elicitedContent) {
        assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(createClarifyingPayload(), elicitedContent);
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(final Map<String, Object> expectedPayload, final Map<String, Object> elicitedContent) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, elicitedContent));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, "invalid_elicited_content", true, false, "structured_fallback");
            verify(exchange).createElicitation(any());
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(createElicitedArguments())), never());
        }
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitedContentInvalid(final String expectedReason, final McpSchema.ElicitResult elicitedResult) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPSuccessPayload response = new MCPMapPayload(createClarifyingPayload());
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(elicitedResult);
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertStructuredFallback(actual, expectedReason, true, false, "structured_fallback");
            verify(exchange).createElicitation(any());
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(createElicitedArguments())), never());
        }
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitationAction(final McpSchema.ElicitResult.Action action) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPSuccessPayload response = new MCPMapPayload(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(action, Map.of()));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), exchange, "database_gateway_plan_encrypt_rule", Map.of());
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange).createElicitation(any());
        }
    }
}
