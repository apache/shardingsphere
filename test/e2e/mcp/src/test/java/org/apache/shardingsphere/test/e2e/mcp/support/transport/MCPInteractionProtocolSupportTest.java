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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPInteractionProtocolSupportTest {
    
    @Test
    void assertCreateInitializeRequestParams() {
        assertThat(MCPInteractionProtocolSupport.createInitializeRequestParams("client"), is(Map.of(
                "protocolVersion", MCPInteractionProtocolSupport.PROTOCOL_VERSION,
                "capabilities", Map.of(),
                "clientInfo", Map.of("name", "client", "version", "1.0.0"))));
    }
    
    @Test
    void assertCreateJsonRpcRequest() {
        assertThat(MCPInteractionProtocolSupport.createJsonRpcRequest("id", "tools/list", Map.of("cursor", "next")), is(Map.of(
                "jsonrpc", "2.0",
                "id", "id",
                "method", "tools/list",
                "params", Map.of("cursor", "next"))));
    }
    
    @Test
    void assertCreateJsonRpcNotification() {
        assertThat(MCPInteractionProtocolSupport.createJsonRpcNotification("notifications/initialized", Map.of()), is(Map.of(
                "jsonrpc", "2.0",
                "method", "notifications/initialized",
                "params", Map.of())));
    }
    
    @Test
    void assertCreateJsonRpcRequestBody() {
        Map<String, Object> actual = MCPInteractionPayloads.parseJsonPayload(MCPInteractionProtocolSupport.createJsonRpcRequestBody("id", "tools/list", Map.of()));
        assertThat(actual, is(Map.of("jsonrpc", "2.0", "id", "id", "method", "tools/list", "params", Map.of())));
    }
    
    @Test
    void assertCreateJsonRpcNotificationBody() {
        Map<String, Object> actual = MCPInteractionPayloads.parseJsonPayload(MCPInteractionProtocolSupport.createJsonRpcNotificationBody("notifications/initialized", Map.of()));
        assertThat(actual, is(Map.of("jsonrpc", "2.0", "method", "notifications/initialized", "params", Map.of())));
    }
}
