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

import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContextTestBuilder;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.QueryResult;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPToolPayloadResolverTest {
    
    @Test
    void assertResolveWithUnsupportedTool() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "unsupported_tool", Map.of());
        
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("invalid_request"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Unsupported tool."));
    }
    
    @Test
    void assertResolveServiceCapabilities() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "get_capabilities", Map.of());
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getPayload(), isA(ServiceCapability.class));
        assertTrue(((ServiceCapability) actual.getPayload()).getSupportedTools().contains("execute_query"));
    }
    
    @Test
    void assertResolveDatabaseCapabilitiesWithUnknownDatabase() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "get_capabilities", Map.of("database", "missing_db"));
        
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("not_found"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Database capability does not exist."));
    }
    
    @Test
    void assertResolveExecuteQueryWithInvalidRequest() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "execute_query", Map.of("database", "logic_db"));
        
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("invalid_request"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Database and sql are required."));
    }
    
    @Test
    void assertResolveExecuteQuery() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "max_rows", 1));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getPayload(), isA(Map.class));
        assertThat(((Map<?, ?>) actual.getPayload()).get("result_kind"), is("result_set"));
        assertThat(((List<?>) ((Map<?, ?>) actual.getPayload()).get("rows")).size(), is(1));
        assertTrue((Boolean) ((Map<?, ?>) actual.getPayload()).get("truncated"));
    }
    
    @Test
    void assertResolveMetadataTool() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "list_databases", Map.of());
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getPayload(), isA(Map.class));
        assertThat(((List<?>) ((Map<?, ?>) actual.getPayload()).get("items")).size(), is(2));
    }
    
    @Test
    void assertResolveMetadataToolWithInvalidRequest() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "list_tables", Map.of("database", "logic_db"));
        
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("invalid_request"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Schema is required."));
    }
    
    private MCPToolPayloadResolver createResolver() {
        return new MCPToolPayloadResolver(new MCPRuntimeContextTestBuilder().build(createMetadataCatalog(), createDatabaseRuntime()));
    }
    
    private MetadataCatalog createMetadataCatalog() {
        Map<String, String> databaseTypes = new LinkedHashMap<>();
        databaseTypes.put("logic_db", "MySQL");
        databaseTypes.put("warehouse", "Hive");
        LinkedList<MetadataObject> metadataObjects = new LinkedList<>();
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.SCHEMA, "public", "", ""));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""));
        metadataObjects.add(new MetadataObject("warehouse", "warehouse", MetadataObjectType.SCHEMA, "warehouse", "", ""));
        return new MetadataCatalog(databaseTypes, metadataObjects);
    }
    
    private DatabaseRuntime createDatabaseRuntime() {
        LinkedList<ExecuteQueryColumnDefinition> columns = new LinkedList<>();
        columns.add(new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INT", false));
        columns.add(new ExecuteQueryColumnDefinition("status", "VARCHAR", "VARCHAR", true));
        LinkedList<List<Object>> rows = new LinkedList<>();
        rows.add(new LinkedList<>(List.of(1, "NEW")));
        rows.add(new LinkedList<>(List.of(2, "DONE")));
        Map<String, QueryResult> queryResults = new LinkedHashMap<>();
        queryResults.put("logic_db:orders", new QueryResult(columns, rows));
        return new DatabaseRuntime(queryResults, Map.of());
    }
}
