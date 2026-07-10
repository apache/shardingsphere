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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExecuteQueryToolHandlerTest {
    
    @Test
    void assertHandleReadOnlyQuery() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(SQLExecutionResponse.resultSet(List.of(), List.of(), false));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPResponse actual = new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "select * from orders")));
        assertThat(actual.toPayload().get("statement_class"), is("query"));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getDatabase(), is("logic_db"));
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
        assertThat(requestCaptor.getValue().getSql(), is("select * from orders"));
        assertThat(requestCaptor.getValue().getMaxRows(), is(100));
        assertThat(requestCaptor.getValue().getTimeoutMs(), is(0));
        assertTrue(requestCaptor.getValue().isReadOnlyExecution());
    }
    
    @Test
    void assertHandleReadOnlyQueryWithSingleSchemaDefault() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(SQLExecutionResponse.resultSet(List.of(), List.of(), false));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(new ShardingSphereSchema("public", mock(DatabaseType.class))));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        when(databaseContext.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1", Map.of("database", "logic_db", "sql", "select * from orders")));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
    }
    
    @Test
    void assertHandleExplain() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(SQLExecutionResponse.resultSet(SupportedMCPStatement.EXPLAIN, "EXPLAIN", List.of(), List.of(), false));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPResponse actual = new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "EXPLAIN SELECT * FROM orders")));
        assertThat(actual.toPayload().get("statement_class"), is("explain"));
        verify(executionFacade).execute(any());
    }
    
    @Test
    void assertHandleExplainUpdate() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(SQLExecutionResponse.resultSet(SupportedMCPStatement.EXPLAIN, "EXPLAIN", List.of(), List.of(), false));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPResponse actual = new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "EXPLAIN UPDATE orders SET status = 'PAID'")));
        assertThat(actual.toPayload().get("statement_class"), is("explain"));
        verify(executionFacade).execute(any());
    }
    
    @Test
    void assertRejectLockingRead() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "SELECT * FROM orders FOR UPDATE"))));
        assertThat(actual.getMessage(), is("Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertRejectUpdateStatement() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "update orders set status = 'PAID'"))));
        assertThat(actual.getMessage(),
                is("database_gateway_execute_query only supports classifier-approved QUERY and EXPLAIN statements. Use database_gateway_execute_update for side-effecting SQL."));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertRejectOutOfRangeMaxRows() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "select * from orders", "max_rows", 5001))));
        assertThat(actual.getMessage(), is("max_rows must be an integer between 0 and 5000."));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertHandleZeroMaxRowsAsDefault() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(SQLExecutionResponse.resultSet(List.of(), List.of(), false));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(new ShardingSphereSchema("public", mock(DatabaseType.class))));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        when(databaseContext.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        new ExecuteQueryToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "select * from orders", "max_rows", 0)));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getMaxRows(), is(100));
    }
}
