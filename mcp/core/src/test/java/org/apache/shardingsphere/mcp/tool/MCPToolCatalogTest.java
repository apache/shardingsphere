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

package org.apache.shardingsphere.mcp.tool;

import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPToolCatalogTest {
    
    private final MCPToolCatalog toolCatalog = new MCPToolCatalog();
    
    @Test
    void assertGetSupportedTools() {
        assertThat(toolCatalog.getSupportedTools().size(), is(11));
        assertThat(toolCatalog.getSupportedTools().get(5), is("list_indexes"));
    }
    
    @Test
    void assertGetToolDescriptors() {
        MCPToolDescriptor actualListDatabases = toolCatalog.findToolDescriptor("list_databases").orElseThrow();
        MCPToolDescriptor actualGetCapabilities = toolCatalog.findToolDescriptor("get_capabilities").orElseThrow();
        
        assertThat(toolCatalog.getToolDescriptors().size(), is(11));
        assertThat(actualListDatabases.getInputDefinition().getFields().size(), is(0));
        assertThat(actualGetCapabilities.getDescription(), is("ShardingSphere MCP tool: get_capabilities"));
        assertThat(actualGetCapabilities.getDispatchKind(), is(MCPToolDispatchKind.CAPABILITY));
        assertThat(actualGetCapabilities.getInputDefinition().getFields().size(), is(1));
        assertThat(actualGetCapabilities.getInputDefinition().getFields().get(0).getName(), is("database"));
        assertFalse(actualGetCapabilities.getInputDefinition().getFields().get(0).isRequired());
        assertThat(actualGetCapabilities.getInputDefinition().getFields().get(0).getValueDefinition().getType(), is(MCPToolValueDefinition.Type.STRING));
    }
    
    @Test
    void assertContains() {
        assertTrue(toolCatalog.contains("execute_query"));
        assertFalse(toolCatalog.contains("unsupported_tool"));
    }
    
    @Test
    void assertGetTitle() {
        assertThat(toolCatalog.getTitle("search_metadata"), is("Search Metadata"));
    }
    
    @Test
    void assertIsMetadataTool() {
        assertTrue(toolCatalog.isMetadataTool("describe_table"));
        assertFalse(toolCatalog.isMetadataTool("get_capabilities"));
        assertFalse(toolCatalog.isMetadataTool("execute_query"));
    }
    
    @Test
    void assertGetCapabilityDatabase() {
        assertThat(toolCatalog.getCapabilityDatabase(Map.of("database", "logic_db")), is("logic_db"));
        assertThat(toolCatalog.getCapabilityDatabase(Collections.emptyMap()), is(""));
    }
    
    @Test
    void assertCreateMetadataToolRequest() {
        ToolRequest actual = toolCatalog.createMetadataToolRequest("search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("table", "view", "invalid"),
                        "page_size", "8", "page_token", "16"));
        
        assertThat(actual.getToolName(), is("search_metadata"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getQuery(), is("order"));
        assertThat(actual.getPageSize(), is(8));
        assertThat(actual.getPageToken(), is("16"));
        assertTrue(actual.getObjectTypes().contains(MetadataObjectType.TABLE));
        assertTrue(actual.getObjectTypes().contains(MetadataObjectType.VIEW));
        assertThat(actual.getObjectTypes().size(), is(2));
    }
    
    @Test
    void assertCreateExecutionRequest() {
        ExecutionRequest actual = toolCatalog.createExecutionRequest("session-id",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1", "max_rows", "20", "timeout_ms", 3000));
        
        assertThat(actual.getSessionId(), is("session-id"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getSql(), is("SELECT 1"));
        assertThat(actual.getMaxRows(), is(20));
        assertThat(actual.getTimeoutMs(), is(3000));
    }
}
