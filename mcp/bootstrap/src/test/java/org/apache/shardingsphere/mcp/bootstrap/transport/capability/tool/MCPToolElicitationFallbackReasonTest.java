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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPToolElicitationFallbackReasonTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getFallbackReasons")
    void assertGetters(final MCPToolElicitationFallbackReason fallbackReason, final String expectedValue, final String expectedSelectedInteraction) {
        assertThat(fallbackReason.getValue(), is(expectedValue));
        assertThat(fallbackReason.getSelectedInteraction(), is(expectedSelectedInteraction));
    }
    
    @Test
    void assertWithClientCapabilitiesUseUrlFallback() {
        MCPToolElicitationFallbackReason actual = MCPToolElicitationFallbackReason.SENSITIVE_FORM_BLOCKED.withClientCapabilities(createClientCapabilities(createFormAndUrlClientCapabilities()));
        assertThat(actual, is(MCPToolElicitationFallbackReason.URL_MODE_NOT_IMPLEMENTED));
    }
    
    @Test
    void assertWithClientCapabilitiesKeepSensitiveFallback() {
        MCPToolElicitationFallbackReason actual =
                MCPToolElicitationFallbackReason.SENSITIVE_FORM_BLOCKED.withClientCapabilities(createClientCapabilities(McpSchema.ClientCapabilities.builder().elicitation().build()));
        assertThat(actual, is(MCPToolElicitationFallbackReason.SENSITIVE_FORM_BLOCKED));
    }
    
    @Test
    void assertWithClientCapabilitiesKeepStructuredFallback() {
        MCPToolElicitationFallbackReason actual = MCPToolElicitationFallbackReason.AMBIGUOUS_FIELD_BINDING.withClientCapabilities(createClientCapabilities(createFormAndUrlClientCapabilities()));
        assertThat(actual, is(MCPToolElicitationFallbackReason.AMBIGUOUS_FIELD_BINDING));
    }
    
    private static Stream<Arguments> getFallbackReasons() {
        return Stream.of(
                Arguments.of(MCPToolElicitationFallbackReason.CLIENT_UNSUPPORTED, "client_unsupported", "structured_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.REMOTE_IDENTITY_REQUIRED, "remote_identity_required", "structured_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.MISSING_PLAN_ID, "missing_plan_id", "structured_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.SENSITIVE_FORM_BLOCKED, "sensitive_form_blocked", "url_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.URL_MODE_NOT_IMPLEMENTED, "url_mode_not_implemented", "url_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.AMBIGUOUS_FIELD_BINDING, "ambiguous_field_binding", "structured_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.ELICITATION_FAILED, "elicitation_failed", "structured_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.MALFORMED_ELICITATION_RESULT, "malformed_elicitation_result", "structured_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.INVALID_ELICITED_CONTENT, "invalid_elicited_content", "structured_fallback"),
                Arguments.of(MCPToolElicitationFallbackReason.STALE_ELICITATION, "stale_elicitation", "structured_fallback"));
    }
    
    private MCPClientElicitationCapabilities createClientCapabilities(final McpSchema.ClientCapabilities clientCapabilities) {
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.getClientCapabilities()).thenReturn(clientCapabilities);
        return MCPClientElicitationCapabilities.from(exchange);
    }
    
    private McpSchema.ClientCapabilities createFormAndUrlClientCapabilities() {
        return new McpSchema.ClientCapabilities(
                Collections.emptyMap(), null, null,
                new McpSchema.ClientCapabilities.Elicitation(new McpSchema.ClientCapabilities.Elicitation.Form(), new McpSchema.ClientCapabilities.Elicitation.Url()));
    }
}
