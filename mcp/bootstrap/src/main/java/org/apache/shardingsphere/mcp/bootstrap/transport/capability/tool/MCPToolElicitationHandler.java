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
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.MCPToolController;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * MCP tool elicitation handler.
 */
@RequiredArgsConstructor
final class MCPToolElicitationHandler {
    
    private static final String STDIO_TRANSPORT = "stdio";
    
    private static final Duration FORM_CONTINUATION_TTL = Duration.ofMinutes(10L);
    
    private static final String ELICITATION_SUPPORT_FIELD = "elicitation_support";
    
    private static final String FALLBACK_REASON_FIELD = "fallback_reason";
    
    private static final String FORM_MODE_FIELD = "form_mode";
    
    private static final String URL_MODE_FIELD = "url_mode";
    
    private static final String SELECTED_INTERACTION_FIELD = "selected_interaction";
    
    private static final String STRUCTURED_FALLBACK_INTERACTION = "structured_fallback";
    
    private static final String URL_FALLBACK_INTERACTION = "url_fallback";
    
    private static final String CLIENT_UNSUPPORTED_REASON = "client_unsupported";
    
    private static final String REMOTE_IDENTITY_REQUIRED_REASON = "remote_identity_required";
    
    private static final String MISSING_PLAN_ID_REASON = "missing_plan_id";
    
    private static final String SENSITIVE_FORM_BLOCKED_REASON = "sensitive_form_blocked";
    
    private static final String URL_MODE_NOT_IMPLEMENTED_REASON = "url_mode_not_implemented";
    
    private static final String AMBIGUOUS_FIELD_BINDING_REASON = "ambiguous_field_binding";
    
    private static final String ELICITATION_FAILED_REASON = "elicitation_failed";
    
    private static final String MALFORMED_ELICITATION_RESULT_REASON = "malformed_elicitation_result";
    
    private static final String INVALID_ELICITED_CONTENT_REASON = "invalid_elicited_content";
    
    private static final String STALE_ELICITATION_REASON = "stale_elicitation";
    
    private final MCPToolController toolController;
    
    private final String activeTransport;
    
    private final Clock clock;
    
    private final MCPToolClarificationPolicy clarificationPolicy = new MCPToolClarificationPolicy();
    
    boolean shouldHandle(final MCPToolDescriptor toolDescriptor, final Map<String, Object> payload) {
        return clarificationPolicy.requiresPlanningClarification(toolDescriptor, payload);
    }
    
    MCPResponse handle(final McpSyncServerExchange exchange, final MCPToolDefinition toolDefinition, final Map<String, Object> arguments,
                       final MCPResponse fallbackResponse, final Map<String, Object> payload) {
        MCPToolDescriptor toolDescriptor = toolDefinition.getDescriptor();
        ClientElicitationSupport clientSupport = getClientElicitationSupport(exchange);
        Optional<MCPToolClarificationPolicy.ClarificationForm> clarificationForm = clarificationPolicy.createClarificationForm(payload, toolDescriptor);
        if (clarificationForm.isEmpty()) {
            return createFallbackResponse(payload, determineUnavailableFormReason(payload, clientSupport), clientSupport);
        }
        if (!clientSupport.supportsFormMode()) {
            return createFallbackResponse(payload, CLIENT_UNSUPPORTED_REASON, clientSupport);
        }
        if (!STDIO_TRANSPORT.equals(activeTransport)) {
            return createFallbackResponse(payload, REMOTE_IDENTITY_REQUIRED_REASON, clientSupport);
        }
        FormContinuationContext continuationContext = createContinuationContext(exchange, toolDescriptor, arguments, clarificationForm.get());
        McpSchema.ElicitResult elicitedResult;
        try {
            elicitedResult = exchange.createElicitation(createElicitRequest(toolDescriptor.getName(), clarificationForm.get(), continuationContext.formRequestId()));
        } catch (final McpError | IllegalStateException | UnsupportedOperationException ignored) {
            return createFallbackResponse(payload, ELICITATION_FAILED_REASON, clientSupport);
        }
        return continueOrFallback(exchange, toolDefinition, arguments, fallbackResponse, payload, clarificationForm.get(), continuationContext, elicitedResult, clientSupport);
    }
    
    private MCPResponse continueOrFallback(final McpSyncServerExchange exchange, final MCPToolDefinition toolDefinition, final Map<String, Object> arguments,
                                           final MCPResponse fallbackResponse, final Map<String, Object> payload,
                                           final MCPToolClarificationPolicy.ClarificationForm clarificationForm,
                                           final FormContinuationContext continuationContext, final McpSchema.ElicitResult elicitedResult,
                                           final ClientElicitationSupport clientSupport) {
        if (null == elicitedResult || null == elicitedResult.action()) {
            return createFallbackResponse(payload, MALFORMED_ELICITATION_RESULT_REASON, clientSupport);
        }
        if (McpSchema.ElicitResult.Action.ACCEPT != elicitedResult.action()) {
            return fallbackResponse;
        }
        if (null == elicitedResult.content()) {
            return createFallbackResponse(payload, MALFORMED_ELICITATION_RESULT_REASON, clientSupport);
        }
        if (!isActiveContinuation(exchange, toolDefinition.getDescriptor(), arguments, clarificationForm, continuationContext)) {
            return createFallbackResponse(payload, STALE_ELICITATION_REASON, clientSupport);
        }
        if (!clarificationPolicy.isValidElicitedContent(clarificationForm, elicitedResult.content())) {
            return createFallbackResponse(payload, INVALID_ELICITED_CONTENT_REASON, clientSupport);
        }
        return toolController.handle(exchange.sessionId(), toolDefinition, clarificationPolicy.mergeArguments(arguments, clarificationForm, elicitedResult.content()));
    }
    
    private ClientElicitationSupport getClientElicitationSupport(final McpSyncServerExchange exchange) {
        McpSchema.ClientCapabilities clientCapabilities = exchange.getClientCapabilities();
        if (null == clientCapabilities || null == clientCapabilities.elicitation()) {
            return new ClientElicitationSupport(false, false);
        }
        McpSchema.ClientCapabilities.Elicitation elicitation = clientCapabilities.elicitation();
        return new ClientElicitationSupport(null != elicitation.form() || null == elicitation.url(), null != elicitation.url());
    }
    
    private String determineUnavailableFormReason(final Map<String, Object> payload, final ClientElicitationSupport clientSupport) {
        if (clarificationPolicy.getPlanId(payload).trim().isEmpty()) {
            return MISSING_PLAN_ID_REASON;
        }
        if (!clarificationPolicy.hasSensitiveClarificationQuestions(payload)) {
            return AMBIGUOUS_FIELD_BINDING_REASON;
        }
        return clientSupport.supportsUrlMode() ? URL_MODE_NOT_IMPLEMENTED_REASON : SENSITIVE_FORM_BLOCKED_REASON;
    }
    
    private MCPResponse createFallbackResponse(final Map<String, Object> payload, final String fallbackReason, final ClientElicitationSupport clientSupport) {
        Map<String, Object> result = new LinkedHashMap<>(payload);
        if (clarificationPolicy.hasSensitiveClarificationQuestions(payload)) {
            result.put(MCPPayloadFieldNames.CLARIFICATION_QUESTIONS, createSanitizedClarificationQuestions(payload));
            result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createSensitiveNextActions());
        }
        result.put(ELICITATION_SUPPORT_FIELD, createElicitationSupportPayload(clientSupport, selectFallbackInteraction(fallbackReason)));
        result.put(FALLBACK_REASON_FIELD, fallbackReason);
        return new MCPMapResponse(result);
    }
    
    private List<Map<String, Object>> createSanitizedClarificationQuestions(final Map<String, Object> payload) {
        Object clarificationQuestions = payload.get(MCPPayloadFieldNames.CLARIFICATION_QUESTIONS);
        if (!(clarificationQuestions instanceof List<?> questions)) {
            return List.of();
        }
        List<Map<String, Object>> result = new LinkedList<>();
        for (Object each : questions) {
            if (each instanceof Map<?, ?> question) {
                result.add(createSanitizedClarificationQuestion(question));
            }
        }
        return result;
    }
    
    private Map<String, Object> createSanitizedClarificationQuestion(final Map<?, ?> question) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put(MCPPayloadFieldNames.FIELD, Objects.toString(question.get(MCPPayloadFieldNames.FIELD), ""));
        result.put(MCPPayloadFieldNames.INPUT_TYPE, "secret");
        result.put(MCPPayloadFieldNames.SECRET, true);
        result.put(MCPPayloadFieldNames.MESSAGE, "Sensitive input must be provided through configured secure channels before continuing the same planner.");
        return result;
    }
    
    private List<Map<String, Object>> createSensitiveNextActions() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("order", 1);
        result.put("type", "terminal");
        result.put("title", "Collect sensitive inputs through configured secure channels.");
        result.put(MCPPayloadFieldNames.REASON, "MCP form elicitation is limited to non-sensitive STDIO continuations; URL mode is not implemented in this release.");
        return List.of(result);
    }
    
    private Map<String, Object> createElicitationSupportPayload(final ClientElicitationSupport clientSupport, final String selectedInteraction) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put(FORM_MODE_FIELD, clientSupport.supportsFormMode());
        result.put(URL_MODE_FIELD, clientSupport.supportsUrlMode());
        result.put(SELECTED_INTERACTION_FIELD, selectedInteraction);
        return result;
    }
    
    private String selectFallbackInteraction(final String fallbackReason) {
        return URL_MODE_NOT_IMPLEMENTED_REASON.equals(fallbackReason) || SENSITIVE_FORM_BLOCKED_REASON.equals(fallbackReason) ? URL_FALLBACK_INTERACTION : STRUCTURED_FALLBACK_INTERACTION;
    }
    
    private FormContinuationContext createContinuationContext(final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor, final Map<String, Object> arguments,
                                                              final MCPToolClarificationPolicy.ClarificationForm clarificationForm) {
        return new FormContinuationContext(toolDescriptor.getName(), exchange.sessionId(), clarificationForm.planId(), arguments.hashCode(), clock.instant().plus(FORM_CONTINUATION_TTL),
                UUID.randomUUID().toString());
    }
    
    private boolean isActiveContinuation(final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor, final Map<String, Object> arguments,
                                         final MCPToolClarificationPolicy.ClarificationForm clarificationForm, final FormContinuationContext continuationContext) {
        return STDIO_TRANSPORT.equals(activeTransport) && continuationContext.sessionId().equals(exchange.sessionId())
                && continuationContext.toolName().equals(toolDescriptor.getName()) && continuationContext.planId().equals(clarificationForm.planId())
                && continuationContext.argumentsHashCode() == arguments.hashCode() && clock.instant().isBefore(continuationContext.expiresAt());
    }
    
    private McpSchema.ElicitRequest createElicitRequest(final String toolName, final MCPToolClarificationPolicy.ClarificationForm clarificationForm, final String formRequestId) {
        return McpSchema.ElicitRequest.builder()
                .message(String.format("Provide missing ShardingSphere workflow inputs for `%s`.", toolName))
                .requestedSchema(clarificationForm.requestedSchema())
                .meta(createElicitMeta(toolName, clarificationForm, formRequestId))
                .build();
    }
    
    private Map<String, Object> createElicitMeta(final String toolName, final MCPToolClarificationPolicy.ClarificationForm clarificationForm, final String formRequestId) {
        return Map.of(
                MCPShardingSphereMetadataKeys.TOOL, toolName,
                MCPShardingSphereMetadataKeys.PLAN_ID, clarificationForm.planId(),
                MCPShardingSphereMetadataKeys.FORM_REQUEST_ID, formRequestId);
    }
    
    private record ClientElicitationSupport(boolean supportsFormMode, boolean supportsUrlMode) {
        
    }
    
    private record FormContinuationContext(String toolName, String sessionId, String planId, int argumentsHashCode, Instant expiresAt, String formRequestId) {
    }
}
