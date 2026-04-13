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
import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.tool.response.MetadataSearchHit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPToolControllerTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertHandleWithUnsupportedTool() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "unsupported_tool", Map.of()).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Unsupported tool."));
    }
    
    @Test
    void assertHandleSearchMetadata() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("query", "order", "object_types", List.of("index"))).toPayload();
        assertThat(((List<?>) actual.get("items")).size(), is(1));
        assertThat(((MetadataSearchHit) ((List<?>) actual.get("items")).get(0)).getName(), is("order_idx"));
    }
    
    @Test
    void assertHandleSearchMetadataWithSequence() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("database", "runtime_db", "query", "order", "object_types", List.of("sequence"))).toPayload();
        assertThat(((List<?>) actual.get("items")).size(), is(1));
        assertThat(((MetadataSearchHit) ((List<?>) actual.get("items")).get(0)).getName(), is("order_seq"));
    }
    
    @Test
    void assertHandleExecuteQuery() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "execute_query", Map.of("database", "logic_db", "sql", "SELECT 1")).toPayload();
        assertThat(actual.get("result_kind"), is("result_set"));
        assertThat(actual.get("statement_class"), is("query"));
        assertThat(actual.get("statement_type"), is("SELECT"));
        assertThat(((List<?>) actual.get("columns")).size(), is(1));
        assertThat(((List<?>) actual.get("rows")).size(), is(1));
    }
    
    @Test
    void assertHandleExecuteQueryWithMissingSavepointName() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "execute_query", Map.of("database", "logic_db", "sql", "RELEASE SAVEPOINT")).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Savepoint name is required."));
    }
    
    @Test
    void assertHandleWithInvalidRequest() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("schema", "public", "query", "orders")).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Schema cannot be provided without database."));
    }
    
    @Test
    void assertHandleWithMissingQuery() throws SQLException {
        Map<String, Object> actual =
                createController().handle("session-1", "search_metadata", Map.of("database", "logic_db", "object_types", List.of(SupportedMCPMetadataObjectType.TABLE.name()))).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("query is required."));
    }
    
    @Test
    void assertHandleWithBlankQuery() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("query", "   ")).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("query is required."));
    }
    
    @Test
    void assertHandleWithInvalidObjectTypes() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("query", "order", "object_types", List.of("invalid_type"))).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Unsupported object_types value `invalid_type`."));
    }
    
    @Test
    void assertHandleWithBlankDatabaseForExecuteQuery() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "execute_query", Map.of("database", "   ", "sql", "SELECT 1")).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("database is required."));
    }
    
    @Test
    void assertHandleWithBlankSqlForExecuteQuery() throws SQLException {
        Map<String, Object> actual = createController().handle("session-1", "execute_query", Map.of("database", "logic_db", "sql", "   ")).toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("sql is required."));
    }
    
    private MCPToolController createController() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "tool-controller");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPDatabaseMetadataCatalog metadataCatalog = ResourceTestDataFactory.createDatabaseMetadataCatalog();
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl)), metadataCatalog);
        runtimeContext.getSessionManager().createSession("session-1");
        return new MCPToolController(runtimeContext);
    }
}
