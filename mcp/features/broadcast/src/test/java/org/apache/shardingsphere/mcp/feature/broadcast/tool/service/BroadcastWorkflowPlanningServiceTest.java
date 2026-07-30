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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.broadcast.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.model.BroadcastWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastWorkflowPlanningServiceTest {
    
    @Test
    void assertPlanCreateRule() {
        MCPFeatureQueryFacade queryFacade = mockQueryFacade(List.of());
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), queryFacade, createRequest("create", "t_order,t_order_item"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getWorkflowKind().getValue(), is("broadcast.rule"));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("CREATE BROADCAST TABLE RULE `t_order`, `t_order_item`"));
    }
    
    @Test
    void assertPlanDropRule() {
        MCPFeatureQueryFacade queryFacade = mockQueryFacade(List.of(Map.of("broadcast_table", "t_order")));
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), queryFacade, createRequest("drop", "t_order"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("DROP BROADCAST TABLE RULE `t_order`"));
    }
    
    @Test
    void assertPlanClarifiesMissingTables() {
        BroadcastWorkflowRequest request = createRequest("create", "");
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().getFirst(), is("Please provide one or more logical table names for broadcast rule planning."));
    }
    
    @Test
    void assertPlanClarifiesMissingDatabase() {
        BroadcastWorkflowRequest request = createRequest("create", "t_order");
        request.setDatabase("");
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsUnsupportedDatabaseIdentifier() {
        BroadcastWorkflowRequest request = createRequest("create", "t_order");
        request.setDatabase("bad`database");
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanSingleTableInput() {
        BroadcastWorkflowRequest request = createRequest("create", "");
        request.setTable("t_order");
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), mockQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("CREATE BROADCAST TABLE RULE `t_order`"));
    }
    
    @Test
    void assertPlanRejectsTableAndTablesTogether() {
        BroadcastWorkflowRequest request = createRequest("create", "t_order,t_order_item");
        request.setTable("t_customer");
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_CONFLICT));
        assertThat(actual.getIssues().getFirst().getDetails(), is(Map.of("conflicting_inputs", List.of("table", "tables"))));
    }
    
    @Test
    void assertPlanCreateFailsWhenRuleExists() {
        MCPFeatureQueryFacade queryFacade = mockQueryFacade(List.of(Map.of("broadcast_table", "t_order")));
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), queryFacade, createRequest("create", "t_order"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanCreateFailsForMixedLifecycleTables() {
        MCPFeatureQueryFacade queryFacade = mockQueryFacade(List.of(Map.of("broadcast_table", "t_order")));
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), queryFacade, createRequest("create", "t_order,t_order_item"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
        assertFalse(actual.getRuleArtifacts().iterator().hasNext());
    }
    
    @Test
    void assertPlanDropFailsWhenRuleDoesNotExist() {
        MCPFeatureQueryFacade queryFacade = mockQueryFacade(List.of());
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), queryFacade, createRequest("drop", "t_order"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND));
    }
    
    @Test
    void assertPlanDropFailsForMixedLifecycleTables() {
        MCPFeatureQueryFacade queryFacade = mockQueryFacade(List.of(Map.of("broadcast_table", "t_order")));
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), queryFacade, createRequest("drop", "t_order,t_order_item"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND));
        assertFalse(actual.getRuleArtifacts().iterator().hasNext());
    }
    
    @Test
    void assertPlanDropChecksRemainingTablesAfterMismatch() {
        MCPFeatureQueryFacade queryFacade = mockQueryFacade(List.of(Map.of("broadcast_table", "t_order")));
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), queryFacade, createRequest("drop", "t_order_item,t_order"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().size(), is(1));
    }
    
    @Test
    void assertPlanContinuesPreviousTableList() {
        TestWorkflowSessionContext sessionContext = new TestWorkflowSessionContext();
        BroadcastWorkflowRequest previousRequest = createRequest("create", "t_order");
        WorkflowContextSnapshot previousSnapshot = new WorkflowContextSnapshot();
        previousSnapshot.setPlanId("plan-1");
        previousSnapshot.setRequest(previousRequest);
        sessionContext.save(previousSnapshot);
        BroadcastWorkflowRequest request = new BroadcastWorkflowRequest();
        request.setPlanId("plan-1");
        WorkflowContextSnapshot actual = createService().plan(sessionContext, mockQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRequest().getTable(), is("t_order"));
    }
    
    @Test
    void assertPlanFailsForUnsupportedIdentifier() {
        WorkflowContextSnapshot actual = createService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), createRequest("create", "bad`table"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    private BroadcastWorkflowPlanningService createService() {
        return new BroadcastWorkflowPlanningService();
    }
    
    private MCPFeatureQueryFacade mockQueryFacade(final List<Map<String, Object>> broadcastRules) {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_order", "t_order")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_order_item", "t_order_item")).thenReturn(true);
        when(result.query(eq("logic_db"), any())).thenReturn(broadcastRules);
        return result;
    }
    
    private BroadcastWorkflowRequest createRequest(final String operationType, final String tables) {
        BroadcastWorkflowRequest result = new BroadcastWorkflowRequest();
        result.setDatabase("logic_db");
        result.setOperationType(operationType);
        result.setTables(tables);
        return result;
    }
}
