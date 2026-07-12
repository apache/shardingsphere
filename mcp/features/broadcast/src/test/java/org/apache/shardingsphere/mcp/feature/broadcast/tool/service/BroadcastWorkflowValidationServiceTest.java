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

import org.apache.shardingsphere.mcp.feature.broadcast.BroadcastFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.broadcast.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.model.BroadcastWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BroadcastWorkflowValidationServiceTest {
    
    private MockedConstruction<BroadcastRuleInspectionService> mockedRuleInspectionServices;
    
    private BroadcastWorkflowValidationService service;
    
    @BeforeEach
    void setUp() {
        mockedRuleInspectionServices = mockConstruction(BroadcastRuleInspectionService.class);
        service = new BroadcastWorkflowValidationService();
    }
    
    @AfterEach
    void tearDown() {
        mockedRuleInspectionServices.close();
    }
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", "executed", "create"));
        Map<String, Object> actual = service.validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), "session-2", workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        BroadcastRuleInspectionService ruleInspectionService = getRuleInspectionService();
        when(ruleInspectionService.queryBroadcastRules(any(), any())).thenReturn(List.of(Map.of("broadcast_table", "t_order")));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", "t_order", "t_order")).thenReturn(true);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actual = service.validate(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(actual.get("overall_status"), is("passed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop");
        workflowSessionContext.save(snapshot);
        BroadcastRuleInspectionService ruleInspectionService = getRuleInspectionService();
        when(ruleInspectionService.queryBroadcastRules(any(), any())).thenReturn(List.of());
        Map<String, Object> actual = service.validate(
                workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWhenRuleMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        BroadcastRuleInspectionService ruleInspectionService = getRuleInspectionService();
        when(ruleInspectionService.queryBroadcastRules(any(), any())).thenReturn(List.of());
        Map<String, Object> actual = service.validate(
                workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
    }
    
    private BroadcastRuleInspectionService getRuleInspectionService() {
        return mockedRuleInspectionServices.constructed().getFirst();
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", "t_order", "t_order")).thenReturn(true);
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        result.setWorkflowKind(BroadcastFeatureDefinition.WORKFLOW_KIND);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        BroadcastWorkflowRequest request = new BroadcastWorkflowRequest();
        request.setDatabase("logic_db");
        request.setTables("t_order");
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
}
