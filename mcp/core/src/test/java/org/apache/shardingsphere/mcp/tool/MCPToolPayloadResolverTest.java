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
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPToolPayloadResolverTest {
    
    @Test
    void assertResolveWithUnsupportedTool() {
        UnsupportedToolException actualException = assertThrows(UnsupportedToolException.class, () -> createResolver().resolve("session-1", "unsupported_tool", Map.of()));
        assertThat(actualException.getMessage(), is("Unsupported tool."));
    }
    
    @Test
    void assertResolveServiceCapabilities() {
        Map<String, Object> actual = createResolver().resolve("session-1", "get_capabilities", Map.of());
        assertTrue(((List<?>) actual.get("supportedTools")).contains("execute_query"));
    }
    
    @Test
    void assertResolveDatabaseCapabilitiesWithUnknownDatabase() {
        DatabaseCapabilityNotFoundException actualException =
                assertThrows(DatabaseCapabilityNotFoundException.class, () -> createResolver().resolve("session-1", "get_capabilities", Map.of("database", "missing_db")));
        assertThat(actualException.getMessage(), is("Database capability does not exist."));
    }
    
    @Test
    void assertResolveExecuteQueryWithInvalidRequest() {
        MCPInvalidRequestException actualException =
                assertThrows(MCPInvalidRequestException.class, () -> createResolver().resolve("session-1", "execute_query", Map.of("database", "logic_db")));
        assertThat(actualException.getMessage(), is("Database and sql are required."));
    }
    
    @Test
    void assertResolveExecuteQuery() {
        Map<String, Object> actual = createResolver().resolve("session-1", "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "max_rows", 1));
        assertThat(actual.get("result_kind"), is("result_set"));
        assertThat(((List<?>) actual.get("rows")).size(), is(1));
        assertTrue((Boolean) actual.get("truncated"));
    }
    
    @Test
    void assertResolveMetadataTool() {
        Map<String, Object> actual = createResolver().resolve("session-1", "list_databases", Map.of());
        assertThat(((List<?>) actual.get("items")).size(), is(2));
    }
    
    @Test
    void assertResolveMetadataToolWithInvalidRequest() {
        MCPInvalidRequestException actualException =
                assertThrows(MCPInvalidRequestException.class, () -> createResolver().resolve("session-1", "list_tables", Map.of("database", "logic_db")));
        assertThat(actualException.getMessage(), is("Schema is required."));
    }
    
    private MCPToolPayloadResolver createResolver() {
        MCPRuntimeContext runtimeContext = new MCPRuntimeContextTestFactory().create(createDatabaseMetadataSnapshots(), createStatementExecutor());
        runtimeContext.getSessionManager().createSession("session-1");
        return new MCPToolPayloadResolver(runtimeContext);
    }
    
    private DatabaseMetadataSnapshots createDatabaseMetadataSnapshots() {
        Map<String, MCPDatabaseMetadata> databaseSnapshots = new LinkedHashMap<>();
        databaseSnapshots.put("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                new MCPSchemaMetadata("logic_db", "public", List.of(
                        new MCPTableMetadata("logic_db", "public", "orders", List.of(), List.of())), List.of()))));
        databaseSnapshots.put("warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(), List.of()))));
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
