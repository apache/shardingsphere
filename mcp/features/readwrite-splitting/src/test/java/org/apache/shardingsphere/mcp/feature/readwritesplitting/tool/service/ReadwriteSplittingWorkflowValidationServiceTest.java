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
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReadwriteSplittingWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createRuleSnapshot("plan-1", "session-1", "executed", "create"));
        Map<String, Object> actual = createRuleService(mock(ReadwriteSplittingInspectionService.class))
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-2",
                        workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateRuleHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createRuleSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRules(any(), any())).thenReturn(List.of(createRuleRow()));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actual = createRuleService(inspectionService).validate(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertValidateApplyArtifactsRejectsUnavailableLoadBalancer() {
        WorkflowContextSnapshot snapshot = createRuleSnapshot("plan-1", "session-1", "executed", "create");
        ReadwriteSplittingRuleWorkflowRequest request = (ReadwriteSplittingRuleWorkflowRequest) snapshot.getRequest();
        request.setLoadBalancerType("RANDOM");
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(LoadBalanceAlgorithm.class, "RANDOM",
                    WorkflowSQLUtils.createProperties(request.getLoadBalancerProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ReadwriteSplittingRuleWorkflowValidationService().validate(snapshot, List.of(createRuleDistSQLArtifact()));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        }
    }
    
    @Test
    void assertValidateApplyArtifactsRejectsInvalidWeightProperties() {
        WorkflowContextSnapshot snapshot = createRuleSnapshot("plan-1", "session-1", "executed", "create");
        ReadwriteSplittingRuleWorkflowRequest request = (ReadwriteSplittingRuleWorkflowRequest) snapshot.getRequest();
        request.setReadStorageUnits("read_ds_0,read_ds_1");
        request.setLoadBalancerType("WEIGHT");
        request.putLoadBalancerProperties(Map.of("read_ds_0", "2", "unknown_ds", "1"));
        try (MockedStatic<TypedSPILoader> ignored = mockStatic(TypedSPILoader.class)) {
            List<Map<String, Object>> actual = new ReadwriteSplittingRuleWorkflowValidationService().validate(snapshot, List.of(createRuleDistSQLArtifact()));
            assertThat(actual.size(), is(2));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        }
    }
    
    @Test
    void assertValidateRuleMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createRuleSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRules(any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createRuleService(inspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createRuleSnapshot("plan-1", "session-1", "executed", "drop");
        workflowSessionContext.save(snapshot);
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRules(any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createRuleService(inspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateStatusHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createStatusSnapshot("plan-1", "session-1", "executed", "enable");
        workflowSessionContext.save(snapshot);
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRuleStatus(any(), any(), any())).thenReturn(List.of(createStatusRow("ENABLED")));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        Map<String, Object> actual = createStatusService(inspectionService).validate(
                workflowSessionContext, mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertSynchronizeStatusWhenStateDoesNotConverge() {
        WorkflowContextSnapshot snapshot = createStatusSnapshot("plan-1", "session-1", "executed", "enable");
        ReadwriteSplittingInspectionService inspectionService = mock(ReadwriteSplittingInspectionService.class);
        when(inspectionService.queryRuleStatus(any(), any(), any())).thenReturn(List.of(createStatusRow("DISABLED")));
        WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class,
                () -> createStatusService(inspectionService).synchronize(snapshot, mock(MCPMetadataQueryFacade.class), createQueryFacade(),
                        mock(MCPFeatureExecutionFacade.class), "session-1"));
        assertThat(actual.getIssueCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "readwrite_ds", "readwrite_ds")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "write_ds", "write_ds")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "read_ds_0", "read_ds_0")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "read_ds_1", "read_ds_1")).thenReturn(true);
        return result;
    }
    
    private ReadwriteSplittingRuleWorkflowValidationService createRuleService(final ReadwriteSplittingInspectionService inspectionService) {
        ReadwriteSplittingRuleWorkflowValidationService result = new ReadwriteSplittingRuleWorkflowValidationService();
        try {
            setField(result, "inspectionService", inspectionService);
            setField(result, "workflowSynchronizationSupport", new WorkflowSynchronizationSupport(1, 0L));
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private ReadwriteSplittingStatusWorkflowValidationService createStatusService(final ReadwriteSplittingInspectionService inspectionService) {
        ReadwriteSplittingStatusWorkflowValidationService result = new ReadwriteSplittingStatusWorkflowValidationService();
        try {
            setField(result, "inspectionService", inspectionService);
            setField(result, "workflowSynchronizationSupport", new WorkflowSynchronizationSupport(1, 0L));
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private WorkflowContextSnapshot createRuleSnapshot(final String planId, final String sessionId, final String status, final String operationType) {
        WorkflowContextSnapshot result = createSnapshot(planId, sessionId, status, operationType);
        result.setWorkflowKind(ReadwriteSplittingFeatureDefinition.RULE_WORKFLOW_KIND);
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setWriteStorageUnit("write_ds");
        request.setReadStorageUnits("read_ds_0");
        request.setTransactionalReadQueryStrategy("DYNAMIC");
        result.setRequest(request);
        return result;
    }
    
    private WorkflowContextSnapshot createStatusSnapshot(final String planId, final String sessionId, final String status, final String operationType) {
        WorkflowContextSnapshot result = createSnapshot(planId, sessionId, status, operationType);
        result.setWorkflowKind(ReadwriteSplittingFeatureDefinition.STATUS_WORKFLOW_KIND);
        ReadwriteSplittingStatusWorkflowRequest request = new ReadwriteSplittingStatusWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setStorageUnit("read_ds_0");
        request.setTargetStatus(operationType);
        result.setRequest(request);
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private Map<String, Object> createRuleRow() {
        return Map.of(
                "name", "readwrite_ds",
                "write_storage_unit_name", "write_ds",
                "read_storage_unit_names", "read_ds_0",
                "transactional_read_query_strategy", "DYNAMIC");
    }
    
    private Map<String, Object> createStatusRow(final String status) {
        return Map.of("name", "readwrite_ds", "storage_unit", "read_ds_0", "status", status);
    }
    
    private ExecutableWorkflowArtifact createRuleDistSQLArtifact() {
        return new ExecutableWorkflowArtifact("review-rule-sql", "rule_dist_sql",
                "CREATE READWRITE_SPLITTING RULE `readwrite_ds` (WRITE_STORAGE_UNIT=`write_ds`, READ_STORAGE_UNITS(`read_ds_0`), TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC')", true);
    }
}
