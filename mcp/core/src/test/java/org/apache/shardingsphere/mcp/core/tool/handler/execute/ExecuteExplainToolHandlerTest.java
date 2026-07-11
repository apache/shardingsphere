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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.SQLSyntaxErrorException;
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

class ExecuteExplainToolHandlerTest {
    
    @Test
    void assertHandleExplainQuery() {
        MCPSQLExecutionFacade executionFacade = mock(MCPSQLExecutionFacade.class);
        when(executionFacade.execute(any(), any())).thenReturn(SQLExecutionResponse.resultSet(SupportedMCPStatement.EXPLAIN, "EXPLAIN", List.of(), List.of(), false));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPResponse actual = new ExecuteExplainToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "explain_sql", "EXPLAIN SELECT * FROM orders")));
        assertThat(actual.toPayload().get("statement_class"), is("explain"));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        ArgumentCaptor<ClassificationResult> classificationCaptor = ArgumentCaptor.forClass(ClassificationResult.class);
        verify(executionFacade).execute(requestCaptor.capture(), classificationCaptor.capture());
        assertThat(requestCaptor.getValue().getDatabase(), is("logic_db"));
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
        assertThat(requestCaptor.getValue().getSql(), is("EXPLAIN SELECT * FROM orders"));
        assertTrue(requestCaptor.getValue().isReadOnlyExecution());
        assertThat(classificationCaptor.getValue().getStatementClass(), is(SupportedMCPStatement.EXPLAIN));
        assertThat(classificationCaptor.getValue().getExplainedStatementClass().orElseThrow(), is(SupportedMCPStatement.QUERY));
    }
    
    @Test
    void assertHandleExplainQueryWithSingleSchemaDefault() {
        MCPSQLExecutionFacade executionFacade = mock(MCPSQLExecutionFacade.class);
        when(executionFacade.execute(any(), any())).thenReturn(SQLExecutionResponse.resultSet(SupportedMCPStatement.EXPLAIN, "EXPLAIN", List.of(), List.of(), false));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(new ShardingSphereSchema("public", mock(DatabaseType.class))));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        when(databaseContext.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        new ExecuteExplainToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "SELECT * FROM orders", "explain_sql", "EXPLAIN SELECT * FROM orders")));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).execute(requestCaptor.capture(), any());
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
    }
    
    @Test
    void assertRejectMutatingExplainedSql() {
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> new ExecuteExplainToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID'", "explain_sql", "EXPLAIN UPDATE orders SET status = 'PAID'"))));
        assertThat(actual.getMessage(), is("database_gateway_execute_explain_query only supports QUERY statements as the explained SQL."));
        verifyNoInteractions(databaseContext);
    }
    
    @Test
    void assertRejectExplainAnalyze() {
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> new ExecuteExplainToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "explain_sql", "EXPLAIN ANALYZE SELECT * FROM orders"))));
        assertThat(actual.getMessage(), is("EXPLAIN ANALYZE is not supported by the MCP explain query tool."));
        verifyNoInteractions(databaseContext);
    }
    
    @Test
    void assertWrapSyntaxError() {
        MCPSQLExecutionFacade executionFacade = mock(MCPSQLExecutionFacade.class);
        when(executionFacade.execute(any(), any())).thenThrow(new MCPInvalidRequestException("bad explain", new SQLSyntaxErrorException("bad explain")));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        ExplainSQLSyntaxException actual = assertThrows(ExplainSQLSyntaxException.class, () -> new ExecuteExplainToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "explain_sql", "EXPLAIN BROKEN SELECT * FROM orders"))));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getSql(), is("SELECT * FROM orders"));
        assertThat(actual.getExplainSql(), is("EXPLAIN BROKEN SELECT * FROM orders"));
    }
}
