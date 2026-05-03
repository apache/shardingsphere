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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.feature.mask.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", "executed", "create"));
        MaskWorkflowValidationService service = createService(mock(MaskRuleInspectionService.class));
        Map<String, Object> actual = service.validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), "session-2", workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateHappyPath() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRule(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y")));
        MaskWorkflowValidationService service = createService(ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(workflowSessionContext, metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(actual.get("overall_status"), is("passed"));
        assertThat(((Map<?, ?>) actual.get("ddl_validation")).get("status"), is("skipped"));
    }
    
    @Test
    void assertSynchronize() throws ReflectiveOperationException {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRule(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y")));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        MaskWorkflowValidationService service = createService(ruleInspectionService);
        service.synchronize(snapshot, metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade, "session-1");
    }
    
    @Test
    void assertSynchronizeWhenStateDoesNotConverge() throws ReflectiveOperationException {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRule(any(), any(), any())).thenReturn(List.of());
        MaskWorkflowValidationService service = createService(ruleInspectionService);
        WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class,
                () -> service.synchronize(snapshot, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1"));
        assertThat(actual.getIssueCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop");
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRule(any(), any(), any())).thenReturn(List.of());
        MaskWorkflowValidationService service = createService(ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(workflowSessionContext, metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWhenAlgorithmMismatch() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRule(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        MaskWorkflowValidationService service = createService(ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(workflowSessionContext, metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateWhenSqlExecutionFails() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRule(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y")));
        MaskWorkflowValidationService service = createService(ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("sql failed"));
        Map<String, Object> actual = service.validate(workflowSessionContext, metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("sql_executability_validation")).get("status"), is("failed"));
    }
    
    private MaskWorkflowValidationService createService(final MaskRuleInspectionService ruleInspectionService) throws ReflectiveOperationException {
        MaskWorkflowValidationService result = new MaskWorkflowValidationService();
        setField(result, "ruleInspectionService", ruleInspectionService);
        setField(result, "workflowSynchronizationSupport", new WorkflowSynchronizationSupport(1, 0L));
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("phone");
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
}
