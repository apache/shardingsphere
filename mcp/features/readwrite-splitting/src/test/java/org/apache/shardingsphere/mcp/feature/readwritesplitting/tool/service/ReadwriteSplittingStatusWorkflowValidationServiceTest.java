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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class ReadwriteSplittingStatusWorkflowValidationServiceTest {
    
    @Test
    void assertValidate() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        workflowSessionContext.save(snapshot);
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRuleStatus(any(), any(), any())).thenReturn(List.of(createStatusRow("ENABLED")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(getValidationSection(actual, "rule").get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWithStatusMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        workflowSessionContext.save(snapshot);
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRuleStatus(any(), any(), any())).thenReturn(List.of(createStatusRow("DISABLED")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(getValidationSection(actual, "rule").get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("code"), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertValidateWithDuplicateTargetRows() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        workflowSessionContext.save(snapshot);
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRuleStatus(any(), any(), any())).thenReturn(List.of(createStatusRow("ENABLED"), createStatusRow("DISABLED")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertSynchronize() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRuleStatus(any(), any(), any())).thenReturn(List.of(createStatusRow("ENABLED")));
        createService(inspectionService).synchronize(
                snapshot, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1");
        verify(inspectionService).queryRuleStatus(any(), any(), any());
    }
    
    private ReadwriteSplittingStatusWorkflowValidationService createService(final ReadwriteSplittingInspectionService inspectionService) {
        try (
                MockedConstruction<ReadwriteSplittingInspectionService> ignored = mockConstruction(
                        ReadwriteSplittingInspectionService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(inspectionService)))) {
            return new ReadwriteSplittingStatusWorkflowValidationService();
        }
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "readwrite_ds", "readwrite_ds")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "read_ds_0", "read_ds_0")).thenReturn(true);
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        result.setStatus("executed");
        result.setWorkflowKind(ReadwriteSplittingFeatureDefinition.STATUS_WORKFLOW_KIND);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("enable");
        result.setClarifiedIntent(clarifiedIntent);
        ReadwriteSplittingStatusWorkflowRequest request = new ReadwriteSplittingStatusWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setStorageUnit("read_ds_0");
        request.setTargetStatus("enable");
        result.setRequest(request);
        return result;
    }
    
    private Map<String, Object> createStatusRow(final String status) {
        return Map.of("name", "readwrite_ds", "storage_unit", "read_ds_0", "status", status);
    }
    
    private Map<?, ?> getValidationSection(final Map<String, Object> payload, final String layer) {
        return ((List<?>) payload.get("sections")).stream().map(each -> (Map<?, ?>) each)
                .filter(each -> layer.equals(each.get("layer"))).findFirst().orElse(Map.of());
    }
}
