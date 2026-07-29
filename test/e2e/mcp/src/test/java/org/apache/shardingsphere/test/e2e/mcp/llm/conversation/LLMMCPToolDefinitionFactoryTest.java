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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLMMCPToolDefinitionFactoryTest {
    
    @Test
    void assertCreateFromRemote() {
        Map<String, Object> remoteSchema = Map.of("type", "object", "description", "remote-marker", "properties", Map.of());
        List<Map<String, Object>> advertisedTools = List.of(
                createAdvertisedTool("read_only_tool", true, remoteSchema),
                createAdvertisedTool("write_tool", false, Map.of("type", "object")));
        List<Map<String, Object>> actual = new LLMMCPToolDefinitionFactory().createFromRemote(advertisedTools, false);
        assertThat(getToolNames(actual), is(List.of(LLMConversationRunner.READ_RESOURCE_TOOL_NAME, "read_only_tool")));
        assertThat(getParameters(findTool(actual, "read_only_tool")), is(remoteSchema));
    }
    
    @Test
    void assertCreateFromRemoteWithUpdatePreview() {
        List<Map<String, Object>> advertisedTools = List.of(
                createAdvertisedTool("read_only_tool", true, Map.of("type", "object")),
                createAdvertisedTool("database_gateway_execute_update", false, Map.of("type", "object", "description", "live-preview-schema")));
        List<Map<String, Object>> actual = new LLMMCPToolDefinitionFactory().createFromRemote(advertisedTools, true);
        assertThat(getToolNames(actual), is(List.of(
                LLMConversationRunner.READ_RESOURCE_TOOL_NAME, "read_only_tool", "database_gateway_execute_update")));
        assertThat(getParameters(findTool(actual, "database_gateway_execute_update")).get("description"), is("live-preview-schema"));
    }
    
    @Test
    void assertCreateFromRemoteWithoutReadOnlyTool() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new LLMMCPToolDefinitionFactory().createFromRemote(
                        List.of(createAdvertisedTool("write_tool", false, Map.of("type", "object"))), false));
        assertThat(actual.getMessage(), is("MCP runtime did not advertise any read-only tools."));
    }
    
    @Test
    void assertCreateFromRemoteWithoutUpdatePreviewTool() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new LLMMCPToolDefinitionFactory().createFromRemote(
                        List.of(createAdvertisedTool("read_only_tool", true, Map.of("type", "object"))), true));
        assertThat(actual.getMessage(), is("MCP runtime did not advertise required preview tool: database_gateway_execute_update"));
    }
    
    @Test
    void assertCreateFromRemoteWithoutInputSchema() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new LLMMCPToolDefinitionFactory().createFromRemote(
                        List.of(Map.of("name", "read_only_tool", "description", "Remote tool definition.", "annotations", Map.of("readOnlyHint", true))), false));
        assertThat(actual.getMessage(), is("MCP runtime advertised tool without a name or inputSchema."));
    }
    
    private Map<String, Object> createAdvertisedTool(final String toolName, final boolean readOnly, final Map<String, Object> inputSchema) {
        return Map.of("name", toolName, "description", "Remote tool definition.", "inputSchema", inputSchema, "annotations", Map.of("readOnlyHint", readOnly));
    }
    
    private Map<?, ?> getParameters(final Map<?, ?> toolDefinition) {
        return (Map<?, ?>) getFunction(toolDefinition).get("parameters");
    }
    
    private Map<?, ?> getFunction(final Map<?, ?> toolDefinition) {
        return (Map<?, ?>) toolDefinition.get("function");
    }
    
    private List<String> getToolNames(final List<Map<String, Object>> toolDefinitions) {
        return toolDefinitions.stream().map(each -> String.valueOf(getFunction(each).get("name"))).toList();
    }
    
    private Map<?, ?> findTool(final List<Map<String, Object>> toolDefinitions, final String toolName) {
        return toolDefinitions.stream()
                .filter(each -> toolName.equals(((Map<?, ?>) each.get("function")).get("name")))
                .findFirst()
                .orElseThrow();
    }
}
