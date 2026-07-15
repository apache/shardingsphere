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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPToolElicitationFallbackResponseFactoryTest extends AbstractMCPToolSpecificationFactoryTest {
    
    private final MCPToolElicitationFallbackResponseFactory factory = new MCPToolElicitationFallbackResponseFactory();
    
    @Test
    void assertCreateStructuredFallback() {
        MCPResponse actual = factory.create(Map.of(WorkflowFieldNames.PLAN_ID, "plan-1", "status", "clarifying"),
                MCPToolElicitationFallbackReason.AMBIGUOUS_FIELD_BINDING, createClientCapabilities(McpSchema.ClientCapabilities.builder().elicitation().build()));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("fallback_reason"), is("ambiguous_field_binding"));
        Map<?, ?> actualSupport = (Map<?, ?>) actualPayload.get("elicitation_support");
        assertTrue((Boolean) actualSupport.get("form_mode"));
        assertFalse((Boolean) actualSupport.get("url_mode"));
        assertThat(actualSupport.get("selected_interaction"), is("structured_fallback"));
        assertFalse(actualPayload.containsKey(MCPPayloadFieldNames.NEXT_ACTIONS));
    }
    
    @Test
    void assertCreateSensitiveFallback() {
        MCPResponse actual = factory.create(createClarifyingPayload(createClarifyingQuestion("primary_algorithm_properties.access-token", "string", false, "Provide access token.")),
                MCPToolElicitationFallbackReason.SENSITIVE_FORM_BLOCKED, createClientCapabilities(McpSchema.ClientCapabilities.builder().elicitation().build()));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("fallback_reason"), is("sensitive_form_blocked"));
        assertSensitiveFallback(actualPayload);
    }
    
    private void assertSensitiveFallback(final Map<String, Object> actualPayload) {
        Map<?, ?> actualQuestion = (Map<?, ?>) ((List<?>) actualPayload.get(MCPPayloadFieldNames.CLARIFICATION_QUESTIONS)).get(0);
        assertThat(actualQuestion.get(MCPPayloadFieldNames.INPUT_TYPE), is("secret"));
        assertTrue((boolean) actualQuestion.get(MCPPayloadFieldNames.SECRET));
        assertThat(actualQuestion.get(MCPPayloadFieldNames.MESSAGE), is("Sensitive input must be provided through configured secure channels before continuing the same planner."));
        assertFalse(actualQuestion.containsKey(MCPPayloadFieldNames.DISPLAY_MESSAGE));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get(MCPPayloadFieldNames.NEXT_ACTIONS)).get(0);
        assertThat(actualNextAction.get("type"), is("terminal"));
        assertThat(actualNextAction.get(MCPPayloadFieldNames.REASON),
                is("MCP form elicitation is limited to non-sensitive STDIO continuations; URL mode is not implemented by the MCP runtime."));
        assertFalse(String.valueOf(actualPayload).contains("Provide access token."));
    }
    
    private MCPClientElicitationCapabilities createClientCapabilities(final McpSchema.ClientCapabilities clientCapabilities) {
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.getClientCapabilities()).thenReturn(clientCapabilities);
        return MCPClientElicitationCapabilities.from(exchange);
    }
}
