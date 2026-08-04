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

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.mockito.MockedStatic;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

abstract class AbstractMCPToolSpecificationFactoryTest {
    
    protected McpSyncServerExchange createElicitationExchange(final McpSchema.ElicitResult elicitationResult) {
        return createElicitationExchange(elicitationResult, McpSchema.ClientCapabilities.builder().elicitation().build());
    }
    
    protected McpSyncServerExchange createElicitationExchange(final McpSchema.ElicitResult elicitationResult, final McpSchema.ClientCapabilities clientCapabilities) {
        McpSyncServerExchange result = mock(McpSyncServerExchange.class);
        when(result.sessionId()).thenReturn("session-id");
        when(result.getClientCapabilities()).thenReturn(clientCapabilities);
        when(result.createElicitation(any())).thenReturn(elicitationResult);
        return result;
    }
    
    protected McpSchema.ClientCapabilities createFormOnlyClientCapabilities() {
        return new McpSchema.ClientCapabilities(
                Collections.emptyMap(), null, null,
                new McpSchema.ClientCapabilities.Elicitation(new McpSchema.ClientCapabilities.Elicitation.Form(), null));
    }
    
    protected McpSchema.ClientCapabilities createFormAndUrlClientCapabilities() {
        return new McpSchema.ClientCapabilities(
                Collections.emptyMap(), null, null,
                new McpSchema.ClientCapabilities.Elicitation(new McpSchema.ClientCapabilities.Elicitation.Form(), new McpSchema.ClientCapabilities.Elicitation.Url()));
    }
    
    protected McpSyncServerExchange createUrlOnlyElicitationExchange() {
        McpSyncServerExchange result = mock(McpSyncServerExchange.class);
        when(result.sessionId()).thenReturn("session-id");
        when(result.getClientCapabilities()).thenReturn(new McpSchema.ClientCapabilities(
                Collections.emptyMap(), null, null,
                new McpSchema.ClientCapabilities.Elicitation(null, new McpSchema.ClientCapabilities.Elicitation.Url())));
        return result;
    }
    
    protected McpSyncServerExchange createThrowingElicitationExchange() {
        McpSyncServerExchange result = mock(McpSyncServerExchange.class);
        when(result.sessionId()).thenReturn("session-id");
        when(result.getClientCapabilities()).thenReturn(McpSchema.ClientCapabilities.builder().elicitation().build());
        when(result.createElicitation(any())).thenThrow(new IllegalStateException("unsupported"));
        return result;
    }
    
    protected SyncToolSpecification findToolSpecification(final List<SyncToolSpecification> specifications, final String toolName) {
        return specifications.stream().filter(each -> toolName.equals(each.tool().name())).findFirst().orElseThrow();
    }
    
    protected MCPToolDefinition mockSupportedTool(final MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry, final MCPToolDescriptor toolDescriptor) {
        MCPToolDefinition result = new MCPToolDefinition(toolDescriptor, mock(MCPToolHandler.class));
        mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(toolDescriptor));
        mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.getToolDefinition(toolDescriptor.getName())).thenReturn(result);
        return result;
    }
    
    protected void mockToolDispatch(final MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry, final MCPToolDefinition toolDefinition,
                                    final Map<String, Object> arguments, final MCPSuccessPayload response) {
        mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(arguments)))
                .thenReturn(response);
    }
    
    protected MCPRuntimeContext createRuntimeContext(final MCPTransportType activeTransport) {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-id", "", "", Map.of()));
        return new MCPRuntimeContext(sessionManager, mock(MCPDatabaseCapabilityProvider.class), activeTransport);
    }
    
    protected SyncToolSpecification createToolSpecification(final MCPTransportType activeTransport) {
        return createToolSpecification(createRuntimeContext(activeTransport));
    }
    
    protected SyncToolSpecification createToolSpecification(final MCPRuntimeContext runtimeContext) {
        return new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().getFirst();
    }
    
    protected McpSyncServerExchange createExchange() {
        McpSyncServerExchange result = mock(McpSyncServerExchange.class);
        when(result.sessionId()).thenReturn("session-id");
        return result;
    }
    
    protected CallToolResult callTool(final SyncToolSpecification toolSpecification, final McpSyncServerExchange exchange, final String toolName,
                                      final Map<String, Object> arguments) {
        return toolSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, arguments));
    }
    
    protected void assertStructuredFallback(final CallToolResult actual, final String expectedReason, final boolean expectedFormMode, final boolean expectedUrlMode,
                                            final String expectedInteraction) {
        Map<String, Object> actualPayload = getStructuredContent(actual);
        assertThat(actualPayload.get("fallback_reason"), is(expectedReason));
        Map<?, ?> actualSupport = (Map<?, ?>) actualPayload.get("elicitation_support");
        assertThat(actualSupport.get("form_mode"), is(expectedFormMode));
        assertThat(actualSupport.get("url_mode"), is(expectedUrlMode));
        assertThat(actualSupport.get("selected_interaction"), is(expectedInteraction));
        assertFalse(actual.isError());
    }
    
    protected void assertSanitizedSensitiveFallback(final CallToolResult actual) {
        Map<String, Object> actualPayload = getStructuredContent(actual);
        List<?> actualQuestions = (List<?>) actualPayload.get("clarification_questions");
        assertFalse(actualQuestions.isEmpty());
        Map<?, ?> actualQuestion = (Map<?, ?>) actualQuestions.get(0);
        assertThat(actualQuestion.get("input_type"), is("secret"));
        assertTrue(Boolean.TRUE.equals(actualQuestion.get("secret")));
        assertTrue(actualQuestion.containsKey("message"));
        assertFalse(actualQuestion.containsKey("display_message"));
        List<?> actualNextActions = (List<?>) actualPayload.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("type"), is("terminal"));
        assertFalse(String.valueOf(actualPayload).contains("Provide access token."));
        assertFalse(String.valueOf(actualPayload).contains("Provide display name."));
        assertFalse(String.valueOf(actualPayload).contains("Provide props."));
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getStructuredContent(final CallToolResult actual) {
        return (Map<String, Object>) actual.structuredContent();
    }
    
    protected Map<String, Object> getTextContentPayload(final CallToolResult actual) {
        return JsonUtils.fromJsonString(((TextContent) actual.content().getFirst()).text(), new TypeReference<>() {
        });
    }
    
    protected CallToolResult createCallToolResult(final String toolName, final MCPSuccessPayload response) {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptorWithoutOutputSchema(toolName));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            return callTool(createToolSpecification(createRuntimeContext(MCPTransportType.HTTP)), createExchange(), toolName, Map.of());
        }
    }
    
    protected Map<String, Object> createClarifyingPayload() {
        return Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of(
                        createClarifyingQuestion("custom_properties.display-name", "string", false, "Provide display name."),
                        createClarifyingQuestion("requires_review", "boolean", false, "Require review?")));
    }
    
    protected Map<String, Object> createClarifyingPayload(final Map<String, Object> question) {
        return Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of(question));
    }
    
    protected Map<String, Object> createClarifyingPayloadWithoutPlanId() {
        return Map.of(
                "status", "clarifying",
                "clarification_questions", List.of(createClarifyingQuestion("custom_properties.display-name", "string", false, "Provide display name.")));
    }
    
    protected Map<String, Object> createClarifyingQuestion(final String field, final String inputType, final boolean secret, final String displayMessage) {
        return Map.of("field", field, "input_type", inputType, "secret", secret, "display_message", displayMessage);
    }
    
    protected Map<String, Object> createClarifyingQuestion(final String field, final String inputType, final boolean secret,
                                                           final String displayMessage, final List<String> allowedValues) {
        return Map.of("field", field, "input_type", inputType, "secret", secret, "display_message", displayMessage, "allowed_values", allowedValues);
    }
    
    protected Map<String, Object> createElicitedArguments() {
        return Map.of(
                "plan_id", "plan-1",
                "custom_properties", Map.of("display-name", "foo_display"),
                "intent", Map.of("requires_review", true));
    }
    
    protected Map<String, Object> createExpectedElicitRequestedSchema() {
        Map<String, Object> properties = new LinkedHashMap<>(2, 1F);
        properties.put("field_1", Map.of("type", "string", "description", "Provide display name."));
        properties.put("field_2", Map.of("type", "boolean", "description", "Require review?"));
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("required", List.of("field_1", "field_2"));
        result.put("additionalProperties", false);
        return result;
    }
    
    protected MCPToolDescriptor createToolDescriptor(final String toolName) {
        Map<String, Object> properties = new LinkedHashMap<>(2, 1F);
        properties.put("query", Map.of("type", "string", "description", "Search query."));
        properties.put("object_types", Map.of("type", "array", "description", "Optional object-type filter.",
                "items", Map.of("type", "string", "description", "Object type.", "enum", List.of("TABLE", "VIEW"))));
        return new MCPToolDescriptor(toolName, "Search Metadata", "Search database metadata.", createInputSchema(properties, List.of("query")),
                Map.of("type", "object"), MCPToolAnnotations.builder()
                        .title("Search Metadata").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(),
                Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://databases")));
    }
    
    protected MCPToolDescriptor createToolDescriptor(final String toolName, final MCPToolAnnotations annotations) {
        return new MCPToolDescriptor(toolName, "Fixture Tool", "Run a fixture tool.", createInputSchema(Map.of(), List.of()), Map.of("type", "object"), annotations, Collections.emptyMap());
    }
    
    protected MCPToolDescriptor createToolDescriptorWithoutOutputSchema(final String toolName) {
        return new MCPToolDescriptor(toolName, "Fixture Tool", "Run a fixture tool.", createInputSchema(Map.of(), List.of()), Collections.emptyMap(),
                MCPToolAnnotations.builder()
                        .title("Fixture Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(),
                Collections.emptyMap());
    }
    
    protected MCPToolDescriptor createStrictToolDescriptor(final String toolName) {
        return new MCPToolDescriptor(toolName, "Search Metadata", "Search database metadata.", createInputSchema(Map.of(), List.of()),
                Map.of("type", "object", "properties", Map.of("status", Map.of("type", "string")), "required", List.of("status")),
                MCPToolAnnotations.builder()
                        .title("Search Metadata").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(),
                Collections.emptyMap());
    }
    
    protected MCPToolDescriptor createPlanningToolDescriptor(final String toolName) {
        return createPlanningToolDescriptor(toolName, Collections.emptyMap());
    }
    
    protected MCPToolDescriptor createPlanningToolDescriptor(final String toolName, final Map<String, Object> additionalProperties) {
        Map<String, Object> properties = new LinkedHashMap<>(2, 1F);
        properties.put("custom_properties", Map.of("type", "object", "description", "Custom properties.", "additionalProperties", true));
        properties.put("intent", Map.of("type", "object", "description", "Intent.", "properties",
                Map.of("requires_review", Map.of("type", "boolean", "description", "Requires review.")), "required", List.of(), "additionalProperties", false));
        properties.putAll(additionalProperties);
        return new MCPToolDescriptor(toolName, "Plan Custom Rule", "Plan a custom rule.", createInputSchema(properties, List.of()),
                Map.of("type", "object"), MCPToolAnnotations.builder()
                        .title("Plan Custom Rule").readOnlyHint(false).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(),
                Collections.emptyMap());
    }
    
    protected MCPToolDescriptor createAmbiguousPlanningToolDescriptor(final String toolName) {
        Map<String, Object> objectProperty = Map.of(
                "type", "object",
                "description", "Intent.",
                "properties", Map.of("requires_review", Map.of("type", "boolean", "description", "Requires review.")),
                "required", List.of(),
                "additionalProperties", false);
        Map<String, Object> properties = new LinkedHashMap<>(2, 1F);
        properties.put("intent", objectProperty);
        properties.put("review_policy", objectProperty);
        return new MCPToolDescriptor(toolName, "Plan Custom Rule", "Plan a custom rule.", createInputSchema(properties, List.of()),
                Map.of("type", "object"), MCPToolAnnotations.builder()
                        .title("Plan Custom Rule").readOnlyHint(false).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(),
                Collections.emptyMap());
    }
    
    protected Map<String, Object> createInputSchema(final Map<String, Object> properties, final List<String> required) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("required", required);
        result.put("additionalProperties", false);
        return result;
    }
    
    protected List<Map<String, Object>> createResourceHints(final String uriPrefix, final String sourceField, final int count) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            result.add(MCPResourceHintUtils.create(uriPrefix + i, "logical-database", "inspect_detail", "Read resource.", sourceField));
        }
        return result;
    }
    
    protected static final class MutableClock extends Clock {
        
        private Instant instant = Instant.parse("2026-05-22T00:00:00Z");
        
        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }
        
        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }
        
        @Override
        public Instant instant() {
            return instant;
        }
        
        protected void advance(final Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
