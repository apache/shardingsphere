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
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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
    
    private final MCPToolController toolController;
    
    private final String activeTransport;
    
    private final Clock clock;
    
    private final MCPToolClarificationPolicy clarificationPolicy = new MCPToolClarificationPolicy();
    
    private final MCPToolElicitationFallbackResponseFactory fallbackResponseFactory = new MCPToolElicitationFallbackResponseFactory();
    
    boolean shouldHandle(final MCPToolDescriptor toolDescriptor, final Map<String, Object> payload) {
        return clarificationPolicy.requiresPlanningClarification(toolDescriptor, payload);
    }
    
    Optional<MCPResponse> handle(final McpSyncServerExchange exchange, final MCPToolDefinition toolDefinition, final Map<String, Object> arguments, final Map<String, Object> payload) {
        MCPToolDescriptor toolDescriptor = toolDefinition.getDescriptor();
        MCPClientElicitationCapabilities clientCapabilities = MCPClientElicitationCapabilities.from(exchange);
        Optional<MCPToolClarificationPolicy.ClarificationForm> clarificationForm = clarificationPolicy.createClarificationForm(payload, toolDescriptor);
        if (clarificationForm.isEmpty()) {
            return Optional.of(createFallbackResponse(payload, getUnavailableFormFallbackReason(payload, clientCapabilities), clientCapabilities));
        }
        if (!clientCapabilities.isFormModeSupported()) {
            return Optional.of(createFallbackResponse(payload, MCPToolElicitationFallbackReason.CLIENT_UNSUPPORTED, clientCapabilities));
        }
        if (!STDIO_TRANSPORT.equals(activeTransport)) {
            return Optional.of(createFallbackResponse(payload, MCPToolElicitationFallbackReason.REMOTE_IDENTITY_REQUIRED, clientCapabilities));
        }
        FormContinuationContext continuationContext = createContinuationContext(exchange, toolDescriptor, arguments, clarificationForm.get());
        McpSchema.ElicitResult elicitedResult;
        try {
            elicitedResult = exchange.createElicitation(createElicitRequest(toolDescriptor.getName(), clarificationForm.get(), continuationContext.formRequestId()));
        } catch (final McpError | IllegalStateException | UnsupportedOperationException ignored) {
            return Optional.of(createFallbackResponse(payload, MCPToolElicitationFallbackReason.ELICITATION_FAILED, clientCapabilities));
        }
        return continueOrFallback(exchange, toolDefinition, arguments, payload, clarificationForm.get(), continuationContext, elicitedResult, clientCapabilities);
    }
    
    private Optional<MCPResponse> continueOrFallback(final McpSyncServerExchange exchange, final MCPToolDefinition toolDefinition, final Map<String, Object> arguments,
                                           final Map<String, Object> payload,
                                           final MCPToolClarificationPolicy.ClarificationForm clarificationForm,
                                           final FormContinuationContext continuationContext, final McpSchema.ElicitResult elicitedResult,
                                           final MCPClientElicitationCapabilities clientCapabilities) {
        if (null == elicitedResult || null == elicitedResult.action()) {
            return Optional.of(createFallbackResponse(payload, MCPToolElicitationFallbackReason.MALFORMED_ELICITATION_RESULT, clientCapabilities));
        }
        if (McpSchema.ElicitResult.Action.ACCEPT != elicitedResult.action()) {
            return Optional.empty();
        }
        if (null == elicitedResult.content()) {
            return Optional.of(createFallbackResponse(payload, MCPToolElicitationFallbackReason.MALFORMED_ELICITATION_RESULT, clientCapabilities));
        }
        if (!continuationContext.isActive(activeTransport, clock, exchange, toolDefinition.getDescriptor(), arguments, clarificationForm)) {
            return Optional.of(createFallbackResponse(payload, MCPToolElicitationFallbackReason.STALE_ELICITATION, clientCapabilities));
        }
        if (!clarificationPolicy.isValidElicitedContent(clarificationForm, elicitedResult.content())) {
            return Optional.of(createFallbackResponse(payload, MCPToolElicitationFallbackReason.INVALID_ELICITED_CONTENT, clientCapabilities));
        }
        return Optional.of(toolController.handle(exchange.sessionId(), toolDefinition, clarificationPolicy.mergeArguments(arguments, clarificationForm, elicitedResult.content())));
    }
    
    private MCPResponse createFallbackResponse(final Map<String, Object> payload, final MCPToolElicitationFallbackReason fallbackReason,
                                               final MCPClientElicitationCapabilities clientCapabilities) {
        return fallbackResponseFactory.create(payload, fallbackReason, clientCapabilities);
    }
    
    private MCPToolElicitationFallbackReason getUnavailableFormFallbackReason(final Map<String, Object> payload, final MCPClientElicitationCapabilities clientCapabilities) {
        if (Objects.toString(payload.get(WorkflowFieldNames.PLAN_ID), "").trim().isEmpty()) {
            return MCPToolElicitationFallbackReason.MISSING_PLAN_ID;
        }
        return clarificationPolicy.hasSensitiveClarificationQuestions(payload)
                ? MCPToolElicitationFallbackReason.SENSITIVE_FORM_BLOCKED.withClientCapabilities(clientCapabilities)
                : MCPToolElicitationFallbackReason.AMBIGUOUS_FIELD_BINDING;
    }
    
    private FormContinuationContext createContinuationContext(final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor, final Map<String, Object> arguments,
                                                              final MCPToolClarificationPolicy.ClarificationForm clarificationForm) {
        return new FormContinuationContext(toolDescriptor.getName(), exchange.sessionId(), clarificationForm.planId(), arguments.hashCode(), clock.instant().plus(FORM_CONTINUATION_TTL),
                UUID.randomUUID().toString());
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
    
    private record FormContinuationContext(String toolName, String sessionId, String planId, int argumentsHashCode, Instant expiresAt, String formRequestId) {

        private boolean isActive(final String activeTransport, final Clock clock, final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor,
                                 final Map<String, Object> arguments, final MCPToolClarificationPolicy.ClarificationForm clarificationForm) {
            return STDIO_TRANSPORT.equals(activeTransport) && sessionId.equals(exchange.sessionId())
                    && toolName.equals(toolDescriptor.getName()) && planId.equals(clarificationForm.planId())
                    && argumentsHashCode == arguments.hashCode() && clock.instant().isBefore(expiresAt);
        }
    }
}
