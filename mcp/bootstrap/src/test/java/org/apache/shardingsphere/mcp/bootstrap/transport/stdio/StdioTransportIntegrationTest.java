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

package org.apache.shardingsphere.mcp.bootstrap.transport.stdio;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntime;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.execute.QueryResult;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.ColumnDefinition;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.tool.ToolDispatchResult;
import org.apache.shardingsphere.mcp.tool.ToolRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StdioTransportIntegrationTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertStart() {
        StdioMCPServer stdioMCPServer = createStdioServer();
        
        assertDoesNotThrow(stdioMCPServer::start);
    }
    
    @Test
    void assertStop() {
        StdioMCPServer stdioMCPServer = createStdioServer();
        
        assertDoesNotThrow(stdioMCPServer::stop);
    }
    
    @Test
    void assertInitializeSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        StdioMCPServer stdioMCPServer = new StdioMCPServer(sessionManager, createRuntimeServices(sessionManager));
        
        String actual = stdioMCPServer.initializeSession();
        
        assertTrue(sessionManager.hasSession(actual));
        assertThat(sessionManager.findSession(actual).orElseThrow().getSessionId(), is(actual));
    }
    
    @Test
    void assertInvokeMetadataTool() {
        StdioMCPServer stdioMCPServer = createStdioServer();
        String sessionId = stdioMCPServer.initializeSession();
        
        ToolDispatchResult actual = stdioMCPServer.invokeMetadataTool(sessionId, createMetadataCatalog(),
                new ToolRequest("list_tables", "logic_db", "public", "", "", "", Set.of(), 10, ""));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getMetadataObjects().size(), is(1));
    }
    
    @Test
    void assertExecuteQuery() {
        StdioMCPServer stdioMCPServer = createStdioServer();
        String sessionId = stdioMCPServer.initializeSession();
        ExecutionRequest executionRequest = new ExecutionRequest(sessionId, "logic_db", "MySQL", "public", "SELECT * FROM orders",
                10, 1000, createDatabaseRuntime());
        
        ExecuteQueryResponse actual = stdioMCPServer.executeQuery(sessionId, executionRequest);
        
        assertTrue(actual.isSuccessful());
    }
    
    @Test
    void assertCloseSession() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        StdioMCPServer stdioMCPServer = new StdioMCPServer(sessionManager, createRuntimeServices(sessionManager));
        String sessionId = stdioMCPServer.initializeSession();
        
        stdioMCPServer.closeSession(sessionId);
        
        assertTrue(sessionManager.isClosedSession(sessionId));
    }
    
    @Test
    void assertLaunch() {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        
        try (MCPRuntime actual = runtimeLauncher.launch(new MCPLaunchConfiguration(createTransportConfiguration(false, true, "/mcp"), createRuntimeDatabases()))) {
            assertTrue(actual.getStdioServer().isPresent());
            assertFalse(actual.getStdioServer().orElseThrow().initializeSession().isEmpty());
        }
    }
    
    @Test
    void assertLaunchWithNoTransport() {
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> runtimeLauncher.launch(new MCPLaunchConfiguration(createTransportConfiguration(false, false, "/mcp"), createRuntimeDatabases())));
        
        assertThat(actual.getMessage(), is("At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    private StdioMCPServer createStdioServer() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = createRuntimeServices(sessionManager);
        return new StdioMCPServer(sessionManager, runtimeServices);
    }
    
    private MCPRuntimeServices createRuntimeServices(final MCPSessionManager sessionManager) {
        return new MCPRuntimeServices(sessionManager, new MetadataCatalog(Map.of(), List.of()), new DatabaseRuntime(Map.of(), Map.of()));
    }
    
    private MetadataCatalog createMetadataCatalog() {
        Map<String, String> databaseTypes = new LinkedHashMap<>();
        databaseTypes.put("logic_db", "MySQL");
        LinkedList<MetadataObject> metadataObjects = new LinkedList<>();
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.SCHEMA, "public", "", ""));
        metadataObjects.add(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""));
        return new MetadataCatalog(databaseTypes, metadataObjects);
    }
    
    private DatabaseRuntime createDatabaseRuntime() {
        Map<String, QueryResult> queryResults = new LinkedHashMap<>();
        LinkedList<ColumnDefinition> columns = new LinkedList<>();
        columns.add(new ColumnDefinition("order_id", "INTEGER", "INT", false));
        LinkedList<List<Object>> rows = new LinkedList<>();
        rows.add(new LinkedList<>(List.of(1)));
        queryResults.put("logic_db:orders", new QueryResult(columns, rows));
        Map<String, Integer> updateCounts = new LinkedHashMap<>();
        updateCounts.put("logic_db:orders", 1);
        return new DatabaseRuntime(queryResults, updateCounts);
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "stdio-transport");
        try {
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    private MCPTransportConfiguration createTransportConfiguration(final boolean httpEnabled, final boolean stdioEnabled, final String endpointPath) {
        return new MCPTransportConfiguration(new HttpTransportConfiguration(httpEnabled, "127.0.0.1", 0, endpointPath), new StdioTransportConfiguration(stdioEnabled));
    }
}
