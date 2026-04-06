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
import org.apache.shardingsphere.mcp.tool.handler.execute.ExecuteSQLToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.SearchMetadataToolHandler;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPToolJsonSchemaAdapterTest {
    
    private final MCPToolJsonSchemaAdapter toolInputSchemaFactory = new MCPToolJsonSchemaAdapter();
    
    @Test
    void assertCreateInputSchemaWithRequiredField() {
        McpSchema.JsonSchema actual = toolInputSchemaFactory.createInputSchema(new ExecuteSQLToolHandler().getToolDescriptor().getFields());
        assertThat(actual.type(), is("object"));
        assertThat(actual.required().size(), is(2));
        assertThat(actual.required().get(0), is("database"));
        assertThat(String.valueOf(castToMap(actual.properties().get("database")).get("type")), is("string"));
        assertTrue(actual.additionalProperties());
    }
    
    @Test
    void assertCreateInputSchemaWithOptionalField() {
        McpSchema.JsonSchema actual = toolInputSchemaFactory.createInputSchema(new ExecuteSQLToolHandler().getToolDescriptor().getFields());
        Map<String, Object> actualTimeout = castToMap(actual.properties().get("timeout_ms"));
        
        assertThat(actual.properties().size(), is(5));
        assertThat(String.valueOf(actualTimeout.get("type")), is("integer"));
        assertThat(String.valueOf(actualTimeout.get("description")), is("Optional timeout in milliseconds."));
    }
    
    @Test
    void assertCreateInputSchemaWithArrayField() {
        McpSchema.JsonSchema actual = toolInputSchemaFactory.createInputSchema(new SearchMetadataToolHandler().getToolDescriptor().getFields());
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
