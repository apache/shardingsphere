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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'", "execution_mode", "execute", "approved_by_user", true)));
        assertThat(actual.toPayload().get("response_mode"), is("executed"));
        assertThat(actual.toPayload().get("execution_mode"), is("execute"));
        assertThat(actual.toPayload().get("statement_class"), is("dml"));
        ArgumentCaptor<SQLExecutionRequest> requestCaptor = ArgumentCaptor.forClass(SQLExecutionRequest.class);
        verify(executionFacade).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getDatabase(), is("logic_db"));
        assertThat(requestCaptor.getValue().getSchema(), is("public"));
        assertThat(requestCaptor.getValue().getSql(), is("update orders set status = 'PAID'"));
        assertThat(requestCaptor.getValue().getMaxRows(), is(100));
        assertThat(requestCaptor.getValue().getTimeoutMs(), is(0));
    }

    @Test
    void assertRejectUnapprovedExecution() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'", "execution_mode", "execute"))));
        assertThat(actual.getMessage(), is("execute_update approved_by_user=true is required for real side effects."));
        verifyNoInteractions(executionFacade);
    }

    @Test
    void assertRejectMissingExecutionMode() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "update orders set status = 'PAID'"))));
        assertThat(actual.getMessage(), is("execute_update execution_mode is required."));
        verifyNoInteractions(executionFacade);
    }

    @Test
    void assertRejectReadOnlyQuery() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "select * from orders", "execution_mode", "execute"))));
        assertThat(actual.getMessage(), is("execute_update does not accept read-only SQL. Use execute_query for read-only SQL."));
        verifyNoInteractions(executionFacade);
    }

    @Test
    void assertPreviewUpdateStatementWithoutExecuting() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPResponse actual = new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'", "execution_mode", "preview")));
        assertThat(actual.toPayload().get("response_mode"), is("preview"));
        assertThat(actual.toPayload().get("result_kind"), is("preview"));
        assertThat(actual.toPayload().get("preview_semantics"), is("classification_only"));
        assertFalse((boolean) actual.toPayload().get("affected_rows_estimated"));
        assertFalse((boolean) actual.toPayload().get("would_execute"));
        assertThat(actual.toPayload().get("status"), is("AWAITING_APPROVAL"));
        assertThat(actual.toPayload().get("statement_class"), is("dml"));
        assertThat(actual.toPayload().get("side_effect_scope"), is(List.of("physical-data")));
        assertThat(actual.toPayload().get("approval_summary"), is("Previewed UPDATE statement with side-effect scope physical-data. It has not been executed."));
        assertThat(actual.toPayload().get("approval_question"), is("Do you approve executing this UPDATE statement with side-effect scope physical-data?"));
        assertFalse(actual.toPayload().containsKey("suggested_next_tool"));
        assertThat(((Map<?, ?>) actual.toPayload().get("suggested_arguments")).get("execution_mode"), is("execute"));
        assertTrue((Boolean) ((Map<?, ?>) actual.toPayload().get("suggested_arguments")).get("approved_by_user"));
        assertThat(((Map<?, ?>) actual.toPayload().get("argument_provenance")).get("sql"), is("server_generated"));
        assertThat(((Map<?, ?>) actual.toPayload().get("argument_provenance")).get("execution_mode"), is("server_defaulted"));
        assertThat(((Map<?, ?>) actual.toPayload().get("argument_provenance")).get("approved_by_user"), is("user_provided"));
        List<?> actualNextActions = (List<?>) actual.toPayload().get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("type"), is("ask_user"));
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("order"), is(1));
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("required_inputs"), is(List.of("approved_by_user")));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("tool_name"), is("execute_update"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("depends_on"), is(List.of(1)));
        assertThat(((Map<?, ?>) ((Map<?, ?>) actualNextActions.get(1)).get("arguments")).get("execution_mode"), is("execute"));
        assertTrue((Boolean) ((Map<?, ?>) ((Map<?, ?>) actualNextActions.get(1)).get("arguments")).get("approved_by_user"));
        assertTrue((Boolean) ((Map<?, ?>) actualNextActions.get(1)).get("requires_user_approval"));
        assertThat(((Map<?, ?>) ((List<?>) actual.toPayload().get("resources_to_read")).get(0)).get("uri"), is("shardingsphere://databases/logic_db/capabilities"));
        assertTrue((boolean) actual.toPayload().get("ask_user_when_uncertain"));
        assertFalse(actual.toPayload().containsKey("recommended_next_call"));
        verifyNoInteractions(executionFacade);
    }

    @Test
    void assertRejectUnknownExecutionMode() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "update orders set status = 'PAID'", "execution_mode", "dry-run"))));
        assertThat(actual.getMessage(), is("execute_update execution_mode must be one of [execute, preview]."));
        verifyNoInteractions(executionFacade);
    }

    @Test
    void assertRejectExplainAnalyze() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new ExecuteUpdateToolHandler().handle(databaseContext, new MCPToolCall("session-1",
                Map.of("database", "logic_db", "sql", "EXPLAIN ANALYZE SELECT * FROM orders", "execution_mode", "execute"))));
        assertThat(actual.getMessage(), is("execute_update does not accept read-only SQL. Use execute_query for read-only SQL."));
        verifyNoInteractions(executionFacade);
    }
}
