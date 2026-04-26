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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPTransportPayloadUtilsTest {
    
    @Test
    void assertCreateCallToolResult() {
        Map<String, Object> payload = Map.of("message", "ok");
        io.modelcontextprotocol.spec.McpSchema.CallToolResult actual = MCPTransportPayloadUtils.createCallToolResult(payload);
        assertFalse(actual.isError());
        assertThat(actual.content().get(0), isA(TextContent.class));
        assertThat(((TextContent) actual.content().get(0)).text(), is(JsonUtils.toJsonString(payload)));
    }
    
    @Test
    void assertCreateCallToolResultWithErrorCode() {
        assertTrue(MCPTransportPayloadUtils.createCallToolResult(Map.of("error_code", "invalid_request")).isError());
    }
    
    @Test
    void assertCreateReadResourceResult() {
        Map<String, Object> payload = Map.of("message", "ok");
        io.modelcontextprotocol.spec.McpSchema.ReadResourceResult actual = MCPTransportPayloadUtils.createReadResourceResult("shardingsphere://capabilities", payload);
        assertThat(actual.contents().get(0), isA(TextResourceContents.class));
        assertThat(((TextResourceContents) actual.contents().get(0)).text(), is(JsonUtils.toJsonString(payload)));
    }
}
