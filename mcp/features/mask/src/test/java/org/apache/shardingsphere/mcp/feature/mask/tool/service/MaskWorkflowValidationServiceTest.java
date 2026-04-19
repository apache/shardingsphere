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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        contextStore.save(createSnapshot("plan-1", "session-1", "executed", "create"));
        MaskWorkflowValidationService service = new MaskWorkflowValidationService(contextStore, mock(MaskRuleInspectionService.class));
        Map<String, Object> actual = service.validate(mock(MCPFeatureContext.class), "session-2", "plan-1");
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateHappyPath() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        contextStore.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y")));
        MaskWorkflowValidationService service = new MaskWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("validated"));
        assertThat(actual.get("overall_status"), is("passed"));
        assertThat(((Map<?, ?>) actual.get("ddl_validation")).get("status"), is("skipped"));
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop");
        contextStore.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        MaskWorkflowValidationService service = new MaskWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWhenAlgorithmMismatch() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        contextStore.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        MaskWorkflowValidationService service = new MaskWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateWhenSqlExecutionFails() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        contextStore.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y")));
        MaskWorkflowValidationService service = new MaskWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("sql failed"));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("sql_executability_validation")).get("status"), is("failed"));
    }
    
    private MCPFeatureContext createRequestContext(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureExecutionFacade executionFacade) {
        MCPFeatureContext result = mock(MCPFeatureContext.class);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getExecutionFacade()).thenReturn(executionFacade);
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
