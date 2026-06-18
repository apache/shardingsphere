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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPClientElicitationCapabilitiesTest {
    
    @Test
    void assertFromWithoutClientCapabilities() {
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        MCPClientElicitationCapabilities actual = MCPClientElicitationCapabilities.from(exchange);
        assertFalse(actual.isFormModeSupported());
        assertFalse(actual.isUrlModeSupported());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getClientCapabilities")
    void assertFrom(final String name, final McpSchema.ClientCapabilities clientCapabilities, final boolean expectedFormModeSupported, final boolean expectedUrlModeSupported) {
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.getClientCapabilities()).thenReturn(clientCapabilities);
        MCPClientElicitationCapabilities actual = MCPClientElicitationCapabilities.from(exchange);
        assertFormModeSupported(actual, expectedFormModeSupported);
        assertUrlModeSupported(actual, expectedUrlModeSupported);
    }
    
    private void assertFormModeSupported(final MCPClientElicitationCapabilities actual, final boolean expectedFormModeSupported) {
        if (expectedFormModeSupported) {
            assertTrue(actual.isFormModeSupported());
        } else {
            assertFalse(actual.isFormModeSupported());
        }
    }
    
    private void assertUrlModeSupported(final MCPClientElicitationCapabilities actual, final boolean expectedUrlModeSupported) {
        if (expectedUrlModeSupported) {
            assertTrue(actual.isUrlModeSupported());
        } else {
            assertFalse(actual.isUrlModeSupported());
        }
    }
    
    private static Stream<Arguments> getClientCapabilities() {
        return Stream.of(
                Arguments.of("without elicitation capabilities", new McpSchema.ClientCapabilities(Collections.emptyMap(), null, null, null), false, false),
                Arguments.of("with default elicitation capabilities", McpSchema.ClientCapabilities.builder().elicitation().build(), true, false),
                Arguments.of("with form elicitation capabilities", new McpSchema.ClientCapabilities(
                        Collections.emptyMap(), null, null,
                        new McpSchema.ClientCapabilities.Elicitation(new McpSchema.ClientCapabilities.Elicitation.Form(), null)), true, false),
                Arguments.of("with form and url elicitation capabilities", new McpSchema.ClientCapabilities(
                        Collections.emptyMap(), null, null,
                        new McpSchema.ClientCapabilities.Elicitation(new McpSchema.ClientCapabilities.Elicitation.Form(), new McpSchema.ClientCapabilities.Elicitation.Url())), true, true),
                Arguments.of("with url elicitation capabilities", new McpSchema.ClientCapabilities(
                        Collections.emptyMap(), null, null,
                        new McpSchema.ClientCapabilities.Elicitation(null, new McpSchema.ClientCapabilities.Elicitation.Url())), false, true));
    }
}
