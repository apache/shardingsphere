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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExecuteUpdateToolHandlerTest {
    
    @Test
    void assertHandleUpdateStatement() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(SQLExecutionResponse.updateCount("UPDATE", 1));
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPResponse actual = new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'")));
        assertThat(actual.toPayload().get("statement_class"), is("dml"));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getDatabase(), is("logic_db"));
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
        assertThat(requestCaptor.getValue().getSql(), is("update orders set status = 'PAID'"));
    }
    
    @Test
    void assertRejectReadOnlyQuery() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "select * from orders"))));
        assertThat(actual.getMessage(), is("execute_update does not accept read-only SQL. Use execute_query for read-only SQL."));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertRejectExplainAnalyze() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "EXPLAIN ANALYZE SELECT * FROM orders"))));
        assertThat(actual.getMessage(), is("execute_update does not accept read-only SQL. Use execute_query for read-only SQL."));
        verifyNoInteractions(executionFacade);
    }
}
