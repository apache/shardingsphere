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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MCP tool elicitation fallback response factory.
 */
public final class MCPToolElicitationFallbackResponseFactory {
    
    private static final String ELICITATION_SUPPORT_FIELD = "elicitation_support";
    
    private static final String FALLBACK_REASON_FIELD = "fallback_reason";
    
    private static final String FORM_MODE_FIELD = "form_mode";
    
    private static final String URL_MODE_FIELD = "url_mode";
    
    private static final String SELECTED_INTERACTION_FIELD = "selected_interaction";
    
    private final MCPToolClarificationPolicy clarificationPolicy = new MCPToolClarificationPolicy();
    
    /**
     * Create fallback response.
     *
     * @param payload original payload
     * @param fallbackReason fallback reason
     * @param clientCapabilities client elicitation capabilities
     * @return MCP response
     */
    public MCPResponse create(final Map<String, Object> payload, final MCPToolElicitationFallbackReason fallbackReason,
                              final MCPClientElicitationCapabilities clientCapabilities) {
        Map<String, Object> result = new LinkedHashMap<>(payload);
        if (clarificationPolicy.hasSensitiveClarificationQuestions(payload)) {
            result.put(MCPPayloadFieldNames.CLARIFICATION_QUESTIONS, createSanitizedClarificationQuestions(payload));
            result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createSensitiveNextActions());
        }
        result.put(ELICITATION_SUPPORT_FIELD, createElicitationSupportPayload(clientCapabilities, fallbackReason.getSelectedInteraction()));
        result.put(FALLBACK_REASON_FIELD, fallbackReason.getValue());
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
    
    private Map<String, Object> createElicitationSupportPayload(final MCPClientElicitationCapabilities clientCapabilities, final String selectedInteraction) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put(FORM_MODE_FIELD, clientCapabilities.isFormModeSupported());
        result.put(URL_MODE_FIELD, clientCapabilities.isUrlModeSupported());
        result.put(SELECTED_INTERACTION_FIELD, selectedInteraction);
        return result;
    }
}
