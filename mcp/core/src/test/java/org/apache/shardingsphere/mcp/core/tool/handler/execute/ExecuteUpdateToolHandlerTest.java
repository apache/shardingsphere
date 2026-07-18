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

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        when(executionFacade.execute(any())).thenReturn(createUpdateResult());
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getSessionIdentity()).thenReturn(new MCPSessionIdentity("session-1", "", "", Map.of()));
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        mockDatabaseCapability(requestContext, "logic_db");
        MCPSuccessPayload actual = new ExecuteUpdateToolHandler().handle(requestContext,
                Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'", "execution_mode", "execute"));
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
    void assertHandleExecutionWithoutApprovalArgument() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(createUpdateResult());
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getSessionIdentity()).thenReturn(new MCPSessionIdentity("session-1", "", "", Map.of()));
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        mockDatabaseCapability(requestContext, "logic_db");
        MCPSuccessPayload actual = new ExecuteUpdateToolHandler().handle(requestContext,
                Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'", "execution_mode", "execute"));
        assertThat(actual.toPayload().get("execution_mode"), is("execute"));
        verify(executionFacade).execute(any());
    }
    
    @Test
    void assertRejectMissingExecutionMode() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        mockDatabaseCapability(requestContext, "logic_db");
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> new ExecuteUpdateToolHandler().handle(requestContext, Map.of("database", "logic_db", "sql", "update orders set status = 'PAID'")));
        assertThat(actual.getMessage(), is("database_gateway_execute_update execution_mode is required."));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertRejectReadOnlyQuery() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        mockDatabaseCapability(requestContext, "logic_db");
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class,
                () -> new ExecuteUpdateToolHandler().handle(requestContext,
                        Map.of("database", "logic_db", "sql", "select * from orders", "execution_mode", "execute")));
        assertThat(actual.getMessage(), is("database_gateway_execute_update does not accept read-only SQL. Use database_gateway_execute_query for read-only SQL."));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertPreviewUpdateStatementWithoutExecuting() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        mockDatabaseCapability(requestContext, "logic_db");
        MCPSuccessPayload actual = new ExecuteUpdateToolHandler().handle(requestContext,
                Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'", "execution_mode", "preview"));
        assertThat(actual.toPayload().get("response_mode"), is("preview"));
        assertThat(actual.toPayload().get("result_kind"), is("preview"));
        assertThat(actual.toPayload().get("preview_semantics"), is("classification_only"));
        assertFalse((boolean) actual.toPayload().get("affected_rows_estimated"));
        assertFalse((boolean) actual.toPayload().get("would_execute"));
        assertThat(actual.toPayload().get("status"), is("PREVIEWED"));
        assertThat(actual.toPayload().get("statement_class"), is("dml"));
        assertThat(actual.toPayload().get("side_effect_scope"), is(List.of("physical-data")));
        assertThat(actual.toPayload().get("summary"), is("Previewed UPDATE statement with side-effect scope physical-data. It has not been executed."));
        assertThat(actual.toPayload().get("review_guidance"),
                is("Review normalized_sql and side_effect_scope before execution. "
                        + "This preview performs database-aware validation and classification; it does not guarantee rule validation, algorithm initialization, affected rows, or runtime success."));
        assertThat(((Map<?, ?>) actual.toPayload().get("suggested_arguments")).get("execution_mode"), is("execute"));
        assertThat(((Map<?, ?>) actual.toPayload().get("argument_provenance")).get("sql"), is("server_generated"));
        assertThat(((Map<?, ?>) actual.toPayload().get("argument_provenance")).get("execution_mode"), is("server_defaulted"));
        List<?> actualNextActions = (List<?>) actual.toPayload().get("next_actions");
        assertThat(actualNextActions.size(), is(2));
        Map<?, ?> actualAskUserAction = (Map<?, ?>) actualNextActions.getFirst();
        assertThat(actualAskUserAction.get("type"), is("ask_user"));
        assertThat(actualAskUserAction.get("order"), is(1));
        assertThat(actualAskUserAction.get("required_inputs"), is(List.of("execution_approved")));
        Map<?, ?> actualToolCallAction = (Map<?, ?>) actualNextActions.get(1);
        assertThat(actualToolCallAction.get("type"), is("tool_call"));
        assertThat(actualToolCallAction.get("order"), is(2));
        assertThat(actualToolCallAction.get("tool_name"), is("database_gateway_execute_update"));
        assertThat(actualToolCallAction.get("depends_on"), is(List.of(1)));
        assertThat(actualToolCallAction.get("reason"),
                is("Execute only after reviewing normalized_sql and side_effect_scope; preview did not validate runtime executability."));
        assertThat(((Map<?, ?>) actualToolCallAction.get("arguments")).get("execution_mode"), is("execute"));
        assertThat(((Map<?, ?>) ((List<?>) actual.toPayload().get("resources_to_read")).getFirst()).get("uri"),
                is("shardingsphere://databases/logic_db/capabilities"));
        assertFalse(actual.toPayload().containsKey("ask_user_when_uncertain"));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertPreviewRuleDistSQLStatementWithoutExecuting() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        mockDatabaseCapability(requestContext, "sharding_db");
        MCPSuccessPayload actual = new ExecuteUpdateToolHandler().handle(requestContext, Map.of("database", "sharding_db", "sql",
                "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'), KEY_GENERATE_STRATEGY(COLUMN=id, TYPE(NAME='snowflake')))",
                "execution_mode", "preview"));
        assertThat(actual.toPayload().get("response_mode"), is("preview"));
        assertThat(actual.toPayload().get("statement_class"), is("ddl"));
        assertThat(actual.toPayload().get("side_effect_scope"), is(List.of("rule-metadata")));
        assertThat(actual.toPayload().get("summary"), is("Previewed CREATE statement with side-effect scope rule-metadata. It has not been executed."));
        assertThat(actual.toPayload().get("review_guidance"),
                is("Review normalized_sql and side_effect_scope before execution. "
                        + "This preview performs database-aware validation and classification; it does not guarantee rule validation, algorithm initialization, affected rows, or runtime success."
                        + " For natural-language rule changes, prefer the matching database_gateway_plan_* workflow tool before raw execution."));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertRejectPreviewWithInvalidTimeout() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPInvalidToolArgumentException actual = assertThrows(MCPInvalidToolArgumentException.class,
                () -> new ExecuteUpdateToolHandler().handle(requestContext,
                        Map.of("database", "logic_db", "schema", "public", "sql", "update orders set status = 'PAID'", "execution_mode", "preview", "timeout_ms", 300001)));
        assertThat(actual.getMessage(), is("timeout_ms must be an integer between 0 and 300000."));
        assertThat(actual.getArgumentPath(), is("timeout_ms"));
        assertThat(actual.getSuggestedValue(), is(0));
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertRejectUnknownExecutionMode() {
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        when(requestContext.getExecutionFacade()).thenReturn(executionFacade);
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> new ExecuteUpdateToolHandler().handle(requestContext,
                        Map.of("database", "logic_db", "sql", "update orders set status = 'PAID'", "execution_mode", "dry-run")));
        assertThat(actual.getMessage(), is("database_gateway_execute_update execution_mode must be one of [execute, preview]."));
        verifyNoInteractions(executionFacade);
    }
    
    private SQLExecutionResult createUpdateResult() {
        return SQLExecutionResult.updateCount(SupportedMCPStatement.DML, "UPDATE", 1, 100, 0, "UPDATE orders SET status = 'PAID'");
    }
    
    private void mockDatabaseCapability(final MCPFeatureRequestContext requestContext, final String database) {
        MCPFeatureCapabilityFacade capabilityFacade = mock(MCPFeatureCapabilityFacade.class);
        MCPDatabaseCapability capability = mock(MCPDatabaseCapability.class);
        when(capability.getDatabaseType()).thenReturn("MySQL");
        when(capabilityFacade.provide(database)).thenReturn(Optional.of(capability));
        when(requestContext.getCapabilityFacade()).thenReturn(capabilityFacade);
    }
    
}
