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

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContextTestFactory;
import org.apache.shardingsphere.mcp.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPToolPayloadResolverTest {
    
    @Test
    void assertResolveWithUnsupportedTool() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "unsupported_tool", Map.of());
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("INVALID_REQUEST"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Unsupported tool."));
    }
    
    @Test
    void assertResolveServiceCapabilities() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "get_capabilities", Map.of());
        assertTrue(actual.isSuccessful());
        assertTrue(((List<?>) actual.getPayload().get("supportedTools")).contains("execute_query"));
    }
    
    @Test
    void assertResolveDatabaseCapabilitiesWithUnknownDatabase() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "get_capabilities", Map.of("database", "missing_db"));
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("NOT_FOUND"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Database capability does not exist."));
    }
    
    @Test
    void assertResolveExecuteQueryWithInvalidRequest() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "execute_query", Map.of("database", "logic_db"));
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("INVALID_REQUEST"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Database and sql are required."));
    }
    
    @Test
    void assertResolveExecuteQuery() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "max_rows", 1));
        assertTrue(actual.isSuccessful());
        assertThat(actual.getPayload().get("result_kind"), is("result_set"));
        assertThat(((List<?>) actual.getPayload().get("rows")).size(), is(1));
        assertTrue((Boolean) actual.getPayload().get("truncated"));
    }
    
    @Test
    void assertResolveMetadataTool() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "list_databases", Map.of());
        assertTrue(actual.isSuccessful());
        assertThat(((List<?>) actual.getPayload().get("items")).size(), is(2));
    }
    
    @Test
    void assertResolveMetadataToolWithInvalidRequest() {
        MCPToolPayloadResult actual = createResolver().resolve("session-1", "list_tables", Map.of("database", "logic_db"));
        assertFalse(actual.isSuccessful());
        assertThat(actual.getErrorCode(), is("INVALID_REQUEST"));
        assertThat(((Map<?, ?>) actual.getPayload()).get("message"), is("Schema is required."));
    }
    
    private MCPToolPayloadResolver createResolver() {
        MCPRuntimeContext runtimeContext = new MCPRuntimeContextTestFactory().create(createDatabaseMetadataSnapshots(), createStatementExecutor());
        runtimeContext.getSessionManager().createSession("session-1");
        return new MCPToolPayloadResolver(runtimeContext);
    }
    
    private DatabaseMetadataSnapshots createDatabaseMetadataSnapshots() {
        Map<String, DatabaseMetadataSnapshot> databaseSnapshots = new LinkedHashMap<>();
        databaseSnapshots.put("logic_db", new DatabaseMetadataSnapshot("MySQL", "", List.of(
                new MetadataObject("logic_db", "public", MetadataObjectType.SCHEMA, "public", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""))));
        databaseSnapshots.put("warehouse", new DatabaseMetadataSnapshot("Hive", "", List.of(
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.SCHEMA, "warehouse", "", ""))));
        return new DatabaseMetadataSnapshots(databaseSnapshots);
    }
    
    private MCPJdbcStatementExecutor createStatementExecutor() {
        MCPJdbcStatementExecutor result = mock(MCPJdbcStatementExecutor.class);
        when(result.execute(any(ExecutionRequest.class), any(ClassificationResult.class))).thenReturn(
                ExecuteQueryResponse.resultSet(List.of(
                        new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INT", false),
                        new ExecuteQueryColumnDefinition("status", "VARCHAR", "VARCHAR", true)),
                        List.of(List.of(1, "NEW")), true));
        return result;
    }
}
