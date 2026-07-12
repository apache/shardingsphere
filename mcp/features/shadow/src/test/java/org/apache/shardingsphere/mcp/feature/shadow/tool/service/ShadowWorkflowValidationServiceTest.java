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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.feature.shadow.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowAlgorithmCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ShadowWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRule() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "shadow_rule", "shadow_rule")).thenReturn(true);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "demo_ds", "demo_ds")).thenReturn(true);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "demo_ds_shadow", "demo_ds_shadow")).thenReturn(true);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_order", "t_order")).thenReturn(true);
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(Map.of(
                "rule_name", "shadow_rule",
                "source_name", "demo_ds",
                "shadow_name", "demo_ds_shadow",
                "shadow_table", "t_order",
                "algorithm_type", "VALUE_MATCH")));
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", createRuleSnapshot());
        assertThat(actual.get("overall_status"), is(WorkflowLifecycle.STATUS_PASSED));
    }
    
    @Test
    void assertValidateCleanupFailure() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "unused_algorithm", "unused_algorithm")).thenReturn(true);
        when(inspectionService.queryAlgorithms(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "unused_algorithm")));
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", createCleanupSnapshot());
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertValidateApplyArtifactsRejectsUnavailableRuleAlgorithm() {
        WorkflowContextSnapshot snapshot = createRuleSnapshot();
        ShadowRuleWorkflowRequest request = (ShadowRuleWorkflowRequest) snapshot.getRequest();
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(ShadowAlgorithm.class, "VALUE_MATCH",
                    WorkflowSQLUtils.createProperties(request.getAlgorithmProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShadowWorkflowValidationService().validate(snapshot, List.of(createRuleDistSQLArtifact("CREATE SHADOW RULE `shadow_rule`(...)")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        }
    }
    
    @Test
    void assertValidateApplyArtifactsRejectsUnavailableDefaultAlgorithm() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setAlgorithmType("SQL_HINT");
        request.putAlgorithmProperties(Map.of("foo", "bar"));
        WorkflowContextSnapshot snapshot = createSnapshot(request, "create");
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(ShadowAlgorithm.class, "SQL_HINT",
                    WorkflowSQLUtils.createProperties(request.getAlgorithmProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShadowWorkflowValidationService().validate(snapshot,
                    List.of(createRuleDistSQLArtifact("CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME='sql_hint')")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("message"), is("Generated shadow DistSQL references algorithm `SQL_HINT`, "
                    + "but it cannot be loaded or initialized by ShadowAlgorithm SPI."));
        }
    }
    
    private ShadowWorkflowValidationService createService(final ShadowInspectionService inspectionService) {
        ShadowWorkflowValidationService result = new ShadowWorkflowValidationService();
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
    
    private WorkflowContextSnapshot createRuleSnapshot() {
        ShadowRuleWorkflowRequest request = new ShadowRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("shadow_rule");
        request.setSourceStorageUnit("demo_ds");
        request.setShadowStorageUnit("demo_ds_shadow");
        request.setTableName("t_order");
        request.setAlgorithmType("VALUE_MATCH");
        return createSnapshot(request, "create");
    }
    
    private WorkflowContextSnapshot createCleanupSnapshot() {
        ShadowAlgorithmCleanupWorkflowRequest request = new ShadowAlgorithmCleanupWorkflowRequest();
        request.setDatabase("logic_db");
        request.setAlgorithmName("unused_algorithm");
        return createSnapshot(request, WorkflowLifecycle.OPERATION_DROP);
    }
    
    private WorkflowContextSnapshot createSnapshot(final WorkflowRequest request, final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        result.setStatus(WorkflowLifecycle.STATUS_EXECUTED);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private ExecutableWorkflowArtifact createRuleDistSQLArtifact(final String sql) {
        return new ExecutableWorkflowArtifact("review-rule-sql", "rule_dist_sql", sql, true);
    }
}
