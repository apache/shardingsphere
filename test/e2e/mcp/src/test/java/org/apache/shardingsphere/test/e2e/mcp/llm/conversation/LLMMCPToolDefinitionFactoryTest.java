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

import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLMMCPToolDefinitionFactoryTest {
    
    @Test
    void assertOfficialToolDefinitionsUseProductionDescriptors() {
        List<MCPToolDescriptor> toolDescriptors = ToolDefinitionRegistry.getSupportedToolDescriptors();
        List<String> toolNames = toolDescriptors.stream().map(MCPToolDescriptor::getName).toList();
        List<Map<String, Object>> actual = new LLMMCPToolDefinitionFactory().create(toolNames);
        assertThat(getToolNames(actual), is(toolNames));
        for (MCPToolDescriptor each : toolDescriptors) {
            assertOfficialToolDefinition(findTool(actual, each.getName()), each);
        }
    }
    
    @Test
    void assertProtocolBridgeToolDefinitionsKeepBridgeSchemas() {
        List<String> bridgeToolNames = List.of(
                MCPInteractionActionNames.LIST_TOOLS,
                MCPInteractionActionNames.LIST_RESOURCES,
                MCPInteractionActionNames.LIST_RESOURCE_TEMPLATES,
                MCPInteractionActionNames.READ_RESOURCE,
                MCPInteractionActionNames.LIST_PROMPTS,
                MCPInteractionActionNames.GET_PROMPT,
                MCPInteractionActionNames.COMPLETE);
        List<Map<String, Object>> actual = new LLMMCPToolDefinitionFactory().create(bridgeToolNames);
        assertThat(getToolNames(actual), is(bridgeToolNames));
        assertEmptyObjectSchema(getParameters(findTool(actual, MCPInteractionActionNames.LIST_TOOLS)));
        assertEmptyObjectSchema(getParameters(findTool(actual, MCPInteractionActionNames.LIST_RESOURCES)));
        assertEmptyObjectSchema(getParameters(findTool(actual, MCPInteractionActionNames.LIST_RESOURCE_TEMPLATES)));
        assertReadResourceBridgeSchema(getParameters(findTool(actual, MCPInteractionActionNames.READ_RESOURCE)));
        assertEmptyObjectSchema(getParameters(findTool(actual, MCPInteractionActionNames.LIST_PROMPTS)));
        assertGetPromptBridgeSchema(getParameters(findTool(actual, MCPInteractionActionNames.GET_PROMPT)));
        assertCompleteBridgeSchema(getParameters(findTool(actual, MCPInteractionActionNames.COMPLETE)));
    }
    
    @Test
    void assertRemoteToolDefinitionsUseAdvertisedSchemas() {
        Map<String, Object> remoteSchema = Map.of("type", "object", "description", "remote-marker", "properties", Map.of());
        List<Map<String, Object>> advertisedTools = List.of(
                Map.of("name", "read_only_tool", "description", "Remote tool definition.", "inputSchema", remoteSchema),
                Map.of("name", "write_tool", "description", "Excluded tool definition.", "inputSchema", Map.of("type", "object")));
        List<Map<String, Object>> actual = new LLMMCPToolDefinitionFactory().createFromRemote(
                advertisedTools, List.of("read_only_tool"), List.of(MCPInteractionActionNames.LIST_RESOURCES));
        assertThat(getToolNames(actual), is(List.of(MCPInteractionActionNames.LIST_RESOURCES, "read_only_tool")));
        assertThat(getFunction(findTool(actual, "read_only_tool")).get("description"), is("Remote tool definition."));
        assertThat(getParameters(findTool(actual, "read_only_tool")), is(remoteSchema));
    }
    
    @Test
    void assertCreateFromRemoteWithMissingRequiredTool() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new LLMMCPToolDefinitionFactory().createFromRemote(List.of(), List.of("read_only_tool"), List.of()));
        assertThat(actual.getMessage(), is("MCP runtime did not advertise required read-only tools: [read_only_tool]"));
    }
    
    @Test
    void assertCreateFromRemoteWithoutInputSchema() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new LLMMCPToolDefinitionFactory().createFromRemote(List.of(Map.of("name", "read_only_tool")), List.of("read_only_tool"), List.of()));
        assertThat(actual.getMessage(), is("MCP runtime advertised tool without inputSchema: read_only_tool"));
    }
    
    @Test
    void assertCreateWithUnsupportedToolDescriptor() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new LLMMCPToolDefinitionFactory().create(List.of("unsupported_tool")));
        assertThat(actual.getMessage(), is("Unsupported tool descriptor: unsupported_tool"));
    }
    
    private void assertOfficialToolDefinition(final Map<?, ?> toolDefinition, final MCPToolDescriptor toolDescriptor) {
        assertThat(toolDefinition.get("type"), is("function"));
        Map<?, ?> function = getFunction(toolDefinition);
        assertThat(function.get("name"), is(toolDescriptor.getName()));
        assertThat(function.get("description"), is(toolDescriptor.getDescription()));
        Map<?, ?> parameters = getParameters(toolDefinition);
        assertThat(parameters, is(toolDescriptor.getInputSchema()));
    }
    
    private void assertEmptyObjectSchema(final Map<?, ?> parameters) {
        assertThat(parameters.get("type"), is("object"));
        assertThat(parameters.get("properties"), is(Map.of()));
        assertFalse((Boolean) parameters.get("additionalProperties"));
    }
    
    private void assertReadResourceBridgeSchema(final Map<?, ?> parameters) {
        assertThat(parameters.get("type"), is("object"));
        assertThat(getFieldType(parameters, "uri"), is("string"));
        assertThat(parameters.get("required"), is(List.of("uri")));
        assertFalse((Boolean) parameters.get("additionalProperties"));
    }
    
    private void assertGetPromptBridgeSchema(final Map<?, ?> parameters) {
        assertThat(parameters.get("type"), is("object"));
        assertThat(getFieldType(parameters, "name"), is("string"));
        assertThat(getFieldType(parameters, "arguments"), is("object"));
        assertThat(parameters.get("required"), is(List.of("name")));
        assertFalse((Boolean) parameters.get("additionalProperties"));
    }
    
    private void assertCompleteBridgeSchema(final Map<?, ?> parameters) {
        assertThat(parameters.get("type"), is("object"));
        Map<?, ?> reference = getField(parameters, "ref");
        assertThat(reference.get("type"), is("object"));
        assertThat(getFieldType(reference, "type"), is("string"));
        assertThat(((Map<?, ?>) getProperties(reference).get("type")).get("enum"), is(List.of("ref/prompt", "ref/resource")));
        assertThat(getFieldType(reference, "name"), is("string"));
        assertThat(getFieldType(reference, "uri"), is("string"));
        assertThat(reference.get("required"), is(List.of("type")));
        assertFalse((Boolean) reference.get("additionalProperties"));
        Map<?, ?> argument = getField(parameters, "argument");
        assertThat(getFieldType(argument, "name"), is("string"));
        assertThat(getFieldType(argument, "value"), is("string"));
        assertThat(argument.get("required"), is(List.of("name")));
        assertFalse((Boolean) argument.get("additionalProperties"));
        Map<?, ?> context = getField(parameters, "context");
        assertThat(getFieldType(context, "arguments"), is("object"));
        assertThat(context.get("required"), is(List.of("arguments")));
        assertFalse((Boolean) context.get("additionalProperties"));
        assertThat(parameters.get("required"), is(List.of("ref", "argument")));
        assertFalse((Boolean) parameters.get("additionalProperties"));
    }
    
    private Map<?, ?> getParameters(final Map<?, ?> toolDefinition) {
        return (Map<?, ?>) getFunction(toolDefinition).get("parameters");
    }
    
    private Map<?, ?> getFunction(final Map<?, ?> toolDefinition) {
        return (Map<?, ?>) toolDefinition.get("function");
    }
    
    private Map<?, ?> getProperties(final Map<?, ?> schema) {
        return (Map<?, ?>) schema.get("properties");
    }
    
    private Map<?, ?> getField(final Map<?, ?> schema, final String fieldName) {
        return (Map<?, ?>) getProperties(schema).get(fieldName);
    }
    
    private Object getFieldType(final Map<?, ?> schema, final String fieldName) {
        return getField(schema, fieldName).get("type");
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
