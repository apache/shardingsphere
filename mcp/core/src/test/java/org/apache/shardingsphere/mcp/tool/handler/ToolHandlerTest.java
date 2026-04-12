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

package org.apache.shardingsphere.mcp.tool.handler;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.handler.execute.ExecuteSQLToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.SearchMetadataToolHandler;
import org.apache.shardingsphere.mcp.protocol.response.MCPMetadataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolHandlerTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertGetSearchMetadataToolDescriptor() {
        MCPToolDescriptor actual = new SearchMetadataToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("search_metadata"));
        assertThat(actual.getFields().size(), is(6));
    }
    
    @Test
    void assertHandleSearchMetadata() throws SQLException {
        MCPResponse actual =
                new SearchMetadataToolHandler().handle(createRuntimeContext(), "session-1", Map.of("query", "order", "object_types", List.of(SupportedMCPMetadataObjectType.INDEX.name())));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actual, isA(MCPMetadataResponse.class));
        assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
        assertThat(((MetadataSearchHit) ((List<?>) actualPayload.get("items")).get(0)).getName(), is("order_idx"));
    }
    
    @Test
    void assertHandleSearchMetadataWithSequence() throws SQLException {
        MCPResponse actual = new SearchMetadataToolHandler().handle(createRuntimeContext(), "session-1",
                Map.of("database", "runtime_db", "query", "order", "object_types", List.of(SupportedMCPMetadataObjectType.SEQUENCE.name())));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actual, isA(MCPMetadataResponse.class));
        assertThat(((List<?>) actualPayload.get("items")).size(), is(1));
        assertThat(((MetadataSearchHit) ((List<?>) actualPayload.get("items")).get(0)).getName(), is("order_seq"));
    }
    
    @Test
    void assertHandleSearchMetadataWithEmptyQuery() throws SQLException {
        MCPResponse actual = new SearchMetadataToolHandler().handle(createRuntimeContext(), "session-1", Map.of("database", "logic_db"));
        Map<String, Object> actualPayload = actual.toPayload();
        List<String> actualNames = new LinkedList<>();
        for (Object each : (List<?>) actualPayload.get("items")) {
            actualNames.add(((MetadataSearchHit) each).getName());
        }
        assertThat(actual, isA(MCPMetadataResponse.class));
        assertThat(actualNames.size(), is(9));
        assertTrue(actualNames.contains("logic_db"));
        assertTrue(actualNames.contains("order_idx"));
    }
    
    @Test
    void assertGetExecuteQueryToolDescriptor() {
        MCPToolDescriptor actual = new ExecuteSQLToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("execute_query"));
        assertThat(actual.getFields().size(), is(5));
    }
    
    @Test
    void assertHandleExecuteQuery() throws SQLException {
        Map<String, Object> actual = new ExecuteSQLToolHandler().handle(createRuntimeContext(), "session-1", Map.of("database", "logic_db", "sql", "SELECT 1")).toPayload();
        assertThat(actual.get("result_kind"), is("result_set"));
        assertThat(actual.get("statement_class"), is("query"));
        assertThat(actual.get("statement_type"), is("SELECT"));
        assertThat(((List<?>) actual.get("columns")).size(), is(1));
        assertThat(((List<?>) actual.get("rows")).size(), is(1));
    }
    
    @Test
    void assertHandleExecuteQueryWithMissingSql() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new ExecuteSQLToolHandler().handle(createRuntimeContext(), "session-1", Map.of("database", "logic_db")));
        assertThat(actual.getMessage(), is("sql cannot be empty."));
    }
    
    private MCPRuntimeContext createRuntimeContext() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "tool-handler");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        final MCPDatabaseMetadataCatalog metadataCatalog = ResourceTestDataFactory.createDatabaseMetadataCatalog();
        MCPRuntimeContext result = new MCPRuntimeContext(new MCPSessionManager(H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl)), metadataCatalog);
        result.getSessionManager().createSession("session-1");
        return result;
    }
}
