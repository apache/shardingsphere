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

import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.tool.handler.execute.MCPSQLExecutionFacade;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowExecutionServiceTest {
    
    @Test
    void assertApplyMasksManualArtifactPackage() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        contextStore.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService(contextStore);
        Map<String, Object> actualResponse = executionService.apply(mock(MCPRequestContext.class), "session-1", "plan-1", List.of(), "manual-only");
        Map<?, ?> actualManualArtifactPackage = (Map<?, ?>) actualResponse.get("manual_artifact_package");
        Map<?, ?> actualArtifact = (Map<?, ?>) ((List<?>) actualManualArtifactPackage.get("distsql_artifacts")).get(0);
        assertThat(actualResponse.get("status"), is("awaiting-manual-execution"));
        assertThat(actualArtifact.get("sql"), is("CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
    }
    
    @Test
    void assertApplyRejectsDifferentSession() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        contextStore.save(createSnapshot());
        WorkflowExecutionService executionService = new WorkflowExecutionService(contextStore);
        Map<String, Object> actualResponse = executionService.apply(mock(MCPRequestContext.class), "session-2", "plan-1", List.of(), "");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertApplyRejectsInvalidLifecycleStatus() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("clarifying");
        contextStore.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService(contextStore);
        Map<String, Object> actualResponse = executionService.apply(mock(MCPRequestContext.class), "session-1", "plan-1", List.of(), "");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
    }
    
    @Test
    void assertApplyCompletesWhenArtifactsAreNotApproved() {
        final WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        contextStore.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService(contextStore);
        try (MockedConstruction<MCPSQLExecutionFacade> mockedConstruction = mockConstruction(MCPSQLExecutionFacade.class)) {
            Map<String, Object> actualResponse = executionService.apply(mock(MCPRequestContext.class), "session-1", "plan-1", List.of("review"), "");
            List<?> actualStepResults = (List<?>) actualResponse.get("step_results");
            assertThat(actualResponse.get("status"), is("completed"));
            assertThat(((List<?>) actualResponse.get("skipped_artifacts")).size(), is(3));
            assertThat(((Map<?, ?>) actualStepResults.get(0)).get("status"), is("skipped"));
            assertThat(contextStore.getRequired("plan-1").getStatus(), is("executed"));
            verify(mockedConstruction.constructed().get(0), never()).execute(any());
        }
    }
    
    @Test
    void assertApplyReturnsDdlExecutionFailureForDdlArtifact() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        contextStore.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService(contextStore);
        try (
                MockedConstruction<MCPSQLExecutionFacade> ignored = mockConstruction(MCPSQLExecutionFacade.class,
                        (mock, context) -> when(mock.execute(any())).thenThrow(new IllegalStateException("ddl failed")))) {
            Map<String, Object> actualResponse = executionService.apply(mock(MCPRequestContext.class), "session-1", "plan-1", List.of("ddl"), "");
            Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
            Map<?, ?> actualStep = (Map<?, ?>) ((List<?>) actualResponse.get("step_results")).get(0);
            assertThat(actualResponse.get("status"), is("failed"));
            assertThat(actualIssue.get("code"), is(WorkflowIssueCode.DDL_EXECUTION_FAILED));
            assertThat(actualStep.get("artifact_type"), is("add-column"));
        }
    }
    
    @Test
    void assertApplyReturnsRuleExecutionFailureForRuleArtifact() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "ALTER MASK RULE orders"));
        contextStore.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService(contextStore);
        try (
                MockedConstruction<MCPSQLExecutionFacade> ignored = mockConstruction(MCPSQLExecutionFacade.class,
                        (mock, context) -> when(mock.execute(any())).thenThrow(new IllegalStateException("rule failed")))) {
            Map<String, Object> actualResponse = executionService.apply(mock(MCPRequestContext.class), "session-1", "plan-1", List.of("rule_distsql"), "");
            Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
            assertThat(actualResponse.get("status"), is("failed"));
            assertThat(actualIssue.get("code"), is(WorkflowIssueCode.RULE_EXECUTION_FAILED));
        }
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        result.setStatus("planned");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        result.setInteractionPlan(interactionPlan);
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setExecutionMode("review-then-execute");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        result.setRequest(request);
        return result;
    }
}
