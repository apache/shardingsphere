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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowPlanningServiceTest {
    
    @Test
    void assertPlanFailsForUnsupportedIdentifier() {
        WorkflowPlanningService planningService = createPlanningService(mock(RuleInspectionService.class), mock(AlgorithmRecommendationService.class),
                mock(AlgorithmPropertyTemplateService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("bad table");
        request.setColumn("order_id");
        request.setIntentType("mask");
        request.setOperationType("create");
        WorkflowContextSnapshot actualSnapshot = planningService.plan(createRuntimeContext(), "session-1", request);
        assertThat(actualSnapshot.getStatus(), is("failed"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanRequestsIntentClarificationWhenIntentTypeIsMissing() {
        WorkflowPlanningService planningService = createPlanningService(mock(RuleInspectionService.class), mock(AlgorithmRecommendationService.class),
                mock(AlgorithmPropertyTemplateService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowRequest request = createRequest("", "create", "");
        WorkflowContextSnapshot actualSnapshot = planningService.plan(createRuntimeContext(), "session-1", request);
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.INTENT_TYPE_UNCLEAR));
    }
    
    @Test
    void assertPlanRequestsDatabaseWhenDatabaseIsMissing() {
        WorkflowPlanningService planningService = createPlanningService(mock(RuleInspectionService.class), mock(AlgorithmRecommendationService.class),
                mock(AlgorithmPropertyTemplateService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowRequest request = createRequest("mask", "create", "");
        request.setDatabase("");
        WorkflowContextSnapshot actualSnapshot = planningService.plan(createRuntimeContext(), "session-1", request);
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanFailsWhenEncryptRuleAlreadyExistsForCreateLifecycle() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of(Map.of("logic_column", "order_id")));
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, mock(AlgorithmRecommendationService.class),
                mock(AlgorithmPropertyTemplateService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowRequest request = createRequest("encrypt", "create", "可逆 不需要等值 不需要模糊");
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", request);
        assertThat(actualSnapshot.getStatus(), is("failed"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanRequestsMissingRequiredProperties() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskAlgorithms(runtimeContext)).thenReturn(List.of());
        when(ruleInspectionService.enrichMaskAlgorithms(List.of())).thenReturn(List.of());
        when(algorithmRecommendationService.recommendMaskAlgorithms(any(), any(), anyList(), anyList()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "MASK_TYPE", "builtin", null, null, null, 90, "mask", "")));
        when(propertyTemplateService.findRequirements(any(), any(), any(), any()))
                .thenReturn(List.of(new AlgorithmPropertyRequirement("primary", "pattern", true, false, "mask pattern", "")));
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("mask", "create", ""));
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertThat(actualSnapshot.getClarifiedIntent().getPendingQuestions().get(0), is("请提供属性 `pattern`。"));
    }
    
    @Test
    void assertPlanStopsWhenEncryptAlgorithmIsUnavailable() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(runtimeContext)).thenReturn(List.of(Map.of("type", "AES")));
        when(ruleInspectionService.enrichEncryptAlgorithms(List.of(Map.of("type", "AES")))).thenReturn(List.of(Map.of("type", "AES")));
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, new AlgorithmRecommendationService(), propertyTemplateService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowRequest request = createRequest("encrypt", "create", "");
        request.setAlgorithmType("MISSING_ALGO");
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", request);
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
        assertThat(actualSnapshot.getRuleArtifacts().size(), is(0));
        verify(propertyTemplateService, never()).findRequirements(any(), any(), any(), any());
    }
    
    @Test
    void assertPlanStopsWhenMaskAlgorithmIsUnavailable() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskAlgorithms(runtimeContext)).thenReturn(List.of(Map.of("type", "MD5")));
        when(ruleInspectionService.enrichMaskAlgorithms(List.of(Map.of("type", "MD5")))).thenReturn(List.of(Map.of("type", "MD5")));
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, new AlgorithmRecommendationService(), propertyTemplateService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowRequest request = createRequest("mask", "create", "");
        request.setAlgorithmType("MISSING_MASK");
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", request);
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
        assertThat(actualSnapshot.getRuleArtifacts().size(), is(0));
        verify(propertyTemplateService, never()).findRequirements(any(), any(), any(), any());
    }
    
    @Test
    void assertPlanStopsWhenAlgorithmSelectionProducesBlockingIssue() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(runtimeContext)).thenReturn(List.of(Map.of("type", "AES")));
        when(ruleInspectionService.enrichEncryptAlgorithms(List.of(Map.of("type", "AES")))).thenReturn(List.of(Map.of("type", "AES")));
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), anyList(), anyList())).thenAnswer(invocation -> {
            List<WorkflowIssue> actualIssues = invocation.getArgument(3);
            actualIssues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT, "error", "selecting-algorithm",
                    "No like-query algorithm is available.", "Choose another algorithm.", false, Map.of()));
            return List.of();
        });
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("encrypt", "create", ""));
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
        assertFalse(actualSnapshot.getClarifiedIntent().getPendingQuestions().isEmpty());
        verify(propertyTemplateService, never()).findRequirements(any(), any(), any(), any());
    }
    
    @Test
    void assertPlanDefersEncryptArtifactsUntilRequirementsAreClarified() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(runtimeContext)).thenReturn(List.of(Map.of("type", "AES")));
        when(ruleInspectionService.enrichEncryptAlgorithms(List.of(Map.of("type", "AES")))).thenReturn(List.of(Map.of("type", "AES")));
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), anyList(), anyList()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", "builtin", true, true, false, 100, "encrypt", "")));
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        RuleDistSQLPlanningService ruleDistSQLPlanningService = mock(RuleDistSQLPlanningService.class);
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                physicalDDLPlanningService, mock(IndexPlanningService.class), ruleDistSQLPlanningService, mock(WorkflowProxyQueryService.class));
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("encrypt", "create", ""));
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertFalse(actualSnapshot.getClarifiedIntent().getPendingQuestions().isEmpty());
        assertThat(actualSnapshot.getDdlArtifacts().size(), is(0));
        assertThat(actualSnapshot.getRuleArtifacts().size(), is(0));
        verify(propertyTemplateService, never()).findRequirements(any(), any(), any(), any());
        verify(physicalDDLPlanningService, never()).planAddColumnArtifacts(any(), any(), anySet(), any());
        verify(ruleDistSQLPlanningService, never()).planEncryptRule(any(), any(), any(), anyList());
    }
    
    @Test
    void assertPlanRejectsEncryptDropWorkflow() {
        WorkflowPlanningService planningService = createPlanningService(mock(RuleInspectionService.class), mock(AlgorithmRecommendationService.class),
                mock(AlgorithmPropertyTemplateService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(RuleDistSQLPlanningService.class), mock(WorkflowProxyQueryService.class));
        WorkflowContextSnapshot actualSnapshot = planningService.plan(createRuntimeContext(), "session-1", createRequest("encrypt", "drop", "可逆 不需要等值 不需要模糊"));
        assertThat(actualSnapshot.getStatus(), is("failed"));
        assertThat(actualSnapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.ENCRYPT_DROP_UNSUPPORTED));
    }
    
    @Test
    void assertPlanMergesPreviousSnapshotRequestWhenPlanIdIsProvided() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        WorkflowContextStore contextStore = new WorkflowContextStore();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        RuleDistSQLPlanningService ruleDistSQLPlanningService = mock(RuleDistSQLPlanningService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskAlgorithms(runtimeContext)).thenReturn(List.of());
        when(ruleInspectionService.enrichMaskAlgorithms(List.of())).thenReturn(List.of());
        when(algorithmRecommendationService.recommendMaskAlgorithms(any(), any(), anyList(), anyList()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "KEEP_FIRST_N_LAST_M", "builtin", null, null, null, 100, "mask", "")));
        when(propertyTemplateService.findRequirements(any(), any(), any(), any())).thenReturn(List.of(
                new AlgorithmPropertyRequirement("primary", "first-n", true, false, "left", ""),
                new AlgorithmPropertyRequirement("primary", "last-m", true, false, "right", ""),
                new AlgorithmPropertyRequirement("primary", "replace-char", true, false, "replace", "*")));
        when(ruleDistSQLPlanningService.planMaskRule(any(), anyList())).thenReturn(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowPlanningService planningService = createPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), ruleDistSQLPlanningService, mock(WorkflowProxyQueryService.class));
        WorkflowContextSnapshot firstSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("mask", "create", ""));
        assertThat(firstSnapshot.getStatus(), is("clarifying"));
        WorkflowRequest followupRequest = new WorkflowRequest();
        followupRequest.setPlanId(firstSnapshot.getPlanId());
        followupRequest.getPrimaryAlgorithmProperties().put("first-n", "1");
        followupRequest.getPrimaryAlgorithmProperties().put("last-m", "1");
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", followupRequest);
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getRequest().getDatabase(), is("logic_db"));
        assertThat(actualSnapshot.getRequest().getColumn(), is("order_id"));
        assertThat(actualSnapshot.getRequest().getPrimaryAlgorithmProperties().get("replace-char"), is("*"));
    }
    
    @Test
    void assertPlanCreatesMaskDropArtifact() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of(Map.of("column", "order_id")));
        when(ruleInspectionService.queryMaskAlgorithms(runtimeContext)).thenReturn(List.of());
        when(ruleInspectionService.enrichMaskAlgorithms(List.of())).thenReturn(List.of());
        when(algorithmRecommendationService.recommendMaskAlgorithms(any(), any(), anyList(), anyList())).thenReturn(List.of());
        when(propertyTemplateService.findRequirements(any(), any(), any(), any())).thenReturn(List.of());
        RuleDistSQLPlanningService ruleDistSQLPlanningService = mock(RuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planMaskDropRule(any(), anyList())).thenReturn(new RuleArtifact("drop", "DROP MASK RULE orders"));
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), ruleDistSQLPlanningService, mock(WorkflowProxyQueryService.class));
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("mask", "drop", ""));
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getRuleArtifacts().get(0).getSql(), is("DROP MASK RULE orders"));
    }
    
    @Test
    void assertPlanSkipsAlgorithmSelectionForMaskDropWorkflow() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of(Map.of("column", "order_id")));
        RuleDistSQLPlanningService ruleDistSQLPlanningService = mock(RuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planMaskDropRule(any(), anyList())).thenReturn(new RuleArtifact("drop", "DROP MASK RULE orders"));
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), ruleDistSQLPlanningService, mock(WorkflowProxyQueryService.class));
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("mask", "drop", ""));
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getRuleArtifacts().get(0).getSql(), is("DROP MASK RULE orders"));
        verify(algorithmRecommendationService, never()).recommendMaskAlgorithms(any(), any(), anyList(), anyList());
        verify(propertyTemplateService, never()).findRequirements(any(), any(), any(), any());
    }
    
    @Test
    void assertPlanSkipsIndexPlanningWhenIndexDdlIsDisabled() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        RuleDistSQLPlanningService ruleDistSQLPlanningService = mock(RuleDistSQLPlanningService.class);
        WorkflowProxyQueryService proxyQueryService = mock(WorkflowProxyQueryService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(runtimeContext)).thenReturn(List.of());
        when(ruleInspectionService.enrichEncryptAlgorithms(List.of())).thenReturn(List.of());
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), anyList(), anyList()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", "builtin", true, true, false, 95, "encrypt", ""),
                        new AlgorithmCandidate("assisted_query", "CRC32", "builtin", null, null, null, 80, "assist", "")));
        when(propertyTemplateService.findRequirements(any(), any(), any(), any())).thenReturn(List.of());
        when(proxyQueryService.queryColumnDefinition(runtimeContext, "logic_db", "public", "orders", "order_id")).thenReturn("VARCHAR(32)");
        when(physicalDDLPlanningService.planAddColumnArtifacts(any(), any(), anySet(), any()))
                .thenReturn(List.of(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10)));
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any(), anyList())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        IndexPlanningService indexPlanningService = mock(IndexPlanningService.class);
        WorkflowRequest request = createRequest("encrypt", "create", "可逆 等值 不需要模糊");
        request.setAllowIndexDDL(false);
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                physicalDDLPlanningService, indexPlanningService, ruleDistSQLPlanningService, proxyQueryService);
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", request);
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getIndexPlans().size(), is(0));
        verify(indexPlanningService, never()).planIndexes(any(), any(), anySet());
    }
    
    @Test
    void assertPlanCreatesEncryptArtifactsWhenInputsAreComplete() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        IndexPlanningService indexPlanningService = mock(IndexPlanningService.class);
        RuleDistSQLPlanningService ruleDistSQLPlanningService = mock(RuleDistSQLPlanningService.class);
        WorkflowProxyQueryService proxyQueryService = mock(WorkflowProxyQueryService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(runtimeContext)).thenReturn(List.of());
        when(ruleInspectionService.enrichEncryptAlgorithms(List.of())).thenReturn(List.of());
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), anyList(), anyList()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", "builtin", true, true, false, 95, "encrypt", ""),
                        new AlgorithmCandidate("assisted_query", "CRC32", "builtin", null, null, null, 80, "assist", "")));
        when(propertyTemplateService.findRequirements(any(), any(), any(), any())).thenReturn(List.of());
        when(proxyQueryService.queryColumnDefinition(runtimeContext, "logic_db", "public", "orders", "order_id")).thenReturn("VARCHAR(32)");
        when(physicalDDLPlanningService.planAddColumnArtifacts(any(), any(), anySet(), any()))
                .thenReturn(List.of(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10)));
        when(indexPlanningService.planIndexes(any(), any(), anySet()))
                .thenReturn(List.of(new IndexPlan("idx_orders_order_id_assisted_query", "order_id_assisted_query", "assist",
                        "CREATE INDEX idx_orders_order_id_assisted_query ON orders(order_id_assisted_query)")));
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any(), anyList())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                physicalDDLPlanningService, indexPlanningService, ruleDistSQLPlanningService, proxyQueryService);
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("encrypt", "create", "可逆 等值 不需要模糊"));
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getDerivedColumnPlan().getCipherColumnName(), is("order_id_cipher"));
        assertThat(actualSnapshot.getDerivedColumnPlan().getAssistedQueryColumnName(), is("order_id_assisted_query"));
        assertThat(actualSnapshot.getDdlArtifacts().size(), is(1));
        assertThat(actualSnapshot.getIndexPlans().size(), is(1));
        assertThat(actualSnapshot.getRuleArtifacts().size(), is(1));
        verify(proxyQueryService).queryColumnDefinition(runtimeContext, "logic_db", "public", "orders", "order_id");
        verify(physicalDDLPlanningService).planAddColumnArtifacts(any(), any(), anySet(), any());
    }
    
    @Test
    void assertPlanUsesResolvedOperationTypeForInferredEncryptAlterWorkflow() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
        AlgorithmRecommendationService algorithmRecommendationService = mock(AlgorithmRecommendationService.class);
        AlgorithmPropertyTemplateService propertyTemplateService = mock(AlgorithmPropertyTemplateService.class);
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        IndexPlanningService indexPlanningService = mock(IndexPlanningService.class);
        WorkflowProxyQueryService proxyQueryService = mock(WorkflowProxyQueryService.class);
        when(ruleInspectionService.queryEncryptRules(runtimeContext, "logic_db", "orders"))
                .thenReturn(List.of(Map.of("logic_column", "order_id", "cipher_column", "order_id_cipher")));
        when(ruleInspectionService.queryMaskRules(runtimeContext, "logic_db", "orders")).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(runtimeContext)).thenReturn(List.of());
        when(ruleInspectionService.enrichEncryptAlgorithms(List.of())).thenReturn(List.of());
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), anyList(), anyList()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", "builtin", true, true, false, 95, "encrypt", ""),
                        new AlgorithmCandidate("assisted_query", "CRC32", "builtin", null, null, null, 80, "assist", "")));
        when(propertyTemplateService.findRequirements(any(), any(), any(), any())).thenReturn(List.of());
        when(proxyQueryService.queryColumnDefinition(runtimeContext, "logic_db", "public", "orders", "order_id")).thenReturn("VARCHAR(32)");
        when(physicalDDLPlanningService.planAddColumnArtifacts(any(), any(), anySet(), any()))
                .thenReturn(List.of(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_assisted_query VARCHAR(32)", 10)));
        when(indexPlanningService.planIndexes(any(), any(), anySet())).thenReturn(List.of());
        WorkflowPlanningService planningService = createPlanningService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                physicalDDLPlanningService, indexPlanningService, new RuleDistSQLPlanningService(), proxyQueryService);
        WorkflowContextSnapshot actualSnapshot = planningService.plan(runtimeContext, "session-1", createRequest("", "", "给 order_id 加密，修改为可逆等值，不需要模糊"));
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getRequest().getIntentType(), is("encrypt"));
        assertThat(actualSnapshot.getRequest().getOperationType(), is("alter"));
        assertTrue(actualSnapshot.getRuleArtifacts().get(0).getSql().startsWith("ALTER ENCRYPT RULE orders"));
    }
    
    private WorkflowPlanningService createPlanningService(final RuleInspectionService ruleInspectionService, final AlgorithmRecommendationService algorithmRecommendationService,
                                                          final AlgorithmPropertyTemplateService propertyTemplateService, final PhysicalDDLPlanningService physicalDDLPlanningService,
                                                          final IndexPlanningService indexPlanningService, final RuleDistSQLPlanningService ruleDistSQLPlanningService,
                                                          final WorkflowProxyQueryService proxyQueryService) {
        return createPlanningService(new WorkflowContextStore(), ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                physicalDDLPlanningService, indexPlanningService, ruleDistSQLPlanningService, proxyQueryService);
    }
    
    private WorkflowPlanningService createPlanningService(final WorkflowContextStore contextStore, final RuleInspectionService ruleInspectionService,
                                                          final AlgorithmRecommendationService algorithmRecommendationService, final AlgorithmPropertyTemplateService propertyTemplateService,
                                                          final PhysicalDDLPlanningService physicalDDLPlanningService, final IndexPlanningService indexPlanningService,
                                                          final RuleDistSQLPlanningService ruleDistSQLPlanningService, final WorkflowProxyQueryService proxyQueryService) {
        return new WorkflowPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                new DerivedColumnNamingService(), physicalDDLPlanningService, indexPlanningService, ruleDistSQLPlanningService, proxyQueryService);
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        return new MCPRuntimeContext(new MCPSessionManager(Map.of()), ResourceTestDataFactory.createDatabaseMetadataCatalog());
    }
    
    private WorkflowRequest createRequest(final String intentType, final String operationType, final String naturalLanguageIntent) {
        WorkflowRequest result = new WorkflowRequest();
        result.setDatabase("logic_db");
        result.setSchema("");
        result.setTable("orders");
        result.setColumn("order_id");
        result.setIntentType(intentType);
        result.setOperationType(operationType);
        result.setNaturalLanguageIntent(naturalLanguageIntent);
        return result;
    }
}
