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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ShardingWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", "executed", "create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest()));
        Map<String, Object> actual = createService(mock(ShardingInspectionService.class)).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-2", workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateTableRuleHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(Map.of("table", "t_order")));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertValidateTableRuleMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategyDrop() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop",
                ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, request);
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateCleanupHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop",
                ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND, createCleanupRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryAlgorithms(any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertSynchronizeWhenStateDoesNotConverge() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND, createReferenceRuleRequest());
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableReferenceRule(any(), any(), any())).thenReturn(List.of());
        WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class,
                () -> createService(inspectionService).synchronize(snapshot, mock(MCPMetadataQueryFacade.class), createQueryFacade(),
                        mock(MCPFeatureExecutionFacade.class), "session-1"));
        assertThat(actual.getIssueCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    private ShardingWorkflowValidationService createService(final ShardingInspectionService inspectionService) {
        return new ShardingWorkflowValidationService(inspectionService, new WorkflowSynchronizationSupport(1, 0L));
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.getDatabaseType("logic_db")).thenReturn("MySQL");
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final String operationType,
                                                   final WorkflowKind workflowKind, final ShardingWorkflowRequest request) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        result.setWorkflowKind(workflowKind);
        result.setRequest(request);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private ShardingWorkflowRequest createTableRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("t_order");
        result.setColumn("order_id");
        return result;
    }
    
    private ShardingWorkflowRequest createReferenceRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("ref_rule");
        return result;
    }
    
    private ShardingWorkflowRequest createCleanupRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setComponentType("algorithm");
        result.setComponentName("unused_algorithm");
        return result;
    }
}
