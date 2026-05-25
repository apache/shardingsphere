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

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPToolSpecificationElicitationFactoryTest extends AbstractMCPToolSpecificationFactoryTest {
    
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
                    .thenReturn(clarifyingResponse,
                            plannedResponse);
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT,
                    Map.of("field_1", "foo_display", "field_2", true)), clientCapabilities);
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()), createFormAndUrlClientCapabilities());
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", true)));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createUrlOnlyElicitationExchange();
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("http")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display")));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("custom_properties.display-name", "foo_display")));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display")));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_search_metadata", Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("fixture_ping", Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createThrowingElicitationExchange();
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display", "field_2", true)));
            when(exchange.createElicitation(any())).thenAnswer(invocation -> {
                clock.advance(Duration.ofMinutes(11L));
                return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("field_1", "foo_display", "field_2", true));
            });
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, elicitedContent));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
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
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(elicitedResult);
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
            assertStructuredFallback(actual, expectedReason, true, false, "structured_fallback");
            verify(exchange).createElicitation(any());
            mockedToolDefinitionRegistry.verify(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-id"), eq(createElicitedArguments())), never());
        }
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitationAction(final McpSchema.ElicitResult.Action action) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createPlanningToolDescriptor("database_gateway_plan_encrypt_rule"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(action, Map.of()));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange).createElicitation(any());
        }
    }
}
