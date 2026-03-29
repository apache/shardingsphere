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

import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.tool.MCPToolCatalog;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPToolInputSchemaFactoryTest {
    
    private final MCPToolInputSchemaFactory toolInputSchemaFactory = new MCPToolInputSchemaFactory();
    
    private final MCPToolCatalog toolCatalog = new MCPToolCatalog();
    
    @Test
    void assertCreateInputSchemaWithNoFields() {
        McpSchema.JsonSchema actual = toolInputSchemaFactory.createInputSchema(toolCatalog.findToolDescriptor("list_databases").orElseThrow().getInputDefinition());
        
        assertThat(actual.type(), is("object"));
        assertTrue(actual.properties().isEmpty());
        assertTrue(actual.required().isEmpty());
        assertTrue(actual.additionalProperties());
    }
    
    @Test
    void assertCreateInputSchemaWithOptionalField() {
        McpSchema.JsonSchema actual = toolInputSchemaFactory.createInputSchema(toolCatalog.findToolDescriptor("get_capabilities").orElseThrow().getInputDefinition());
        Map<String, Object> actualDatabase = castToMap(actual.properties().get("database"));
        
        assertThat(actual.properties().size(), is(1));
        assertTrue(actual.required().isEmpty());
        assertThat(String.valueOf(actualDatabase.get("type")), is("string"));
        assertThat(String.valueOf(actualDatabase.get("description")), is("Optional logical database name."));
    }
    
    @Test
    void assertCreateInputSchemaWithArrayField() {
        McpSchema.JsonSchema actual = toolInputSchemaFactory.createInputSchema(toolCatalog.findToolDescriptor("search_metadata").orElseThrow().getInputDefinition());
        Map<String, Object> actualObjectTypes = castToMap(actual.properties().get("object_types"));
        
        assertThat(actual.required().size(), is(1));
        assertThat(actual.required().get(0), is("query"));
        assertThat(String.valueOf(actualObjectTypes.get("type")), is("array"));
        Map<String, Object> actualItems = castToMap(actualObjectTypes.get("items"));
        assertThat(String.valueOf(actualItems.get("type")), is("string"));
        assertThat(String.valueOf(actualItems.get("description")), is("Array element value."));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
}
