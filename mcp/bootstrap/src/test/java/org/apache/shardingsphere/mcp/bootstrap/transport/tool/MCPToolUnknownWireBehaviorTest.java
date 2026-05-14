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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import org.apache.shardingsphere.mcp.bootstrap.transport.AbstractMCPWireBehaviorTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MCPToolUnknownWireBehaviorTest extends AbstractMCPWireBehaviorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertUnknownToolUsesJsonRpcError(final String name, final MCPWireClientFactory clientFactory) throws Exception {
        String requestId = "tools-call-unknown-1";
        try (MCPWireClient client = clientFactory.create()) {
            Map<String, Object> actual = client.sendRawRequest(requestId, "tools/call", Map.of("name", "unsupported_tool", "arguments", Map.of()));
            assertJsonRpcErrorWithoutResult(actual, requestId);
            assertFalse(getMap(actual.get("result")).containsKey("isError"));
        }
    }
}
