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

package org.apache.shardingsphere.mcp.core.workflow;

import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.workflow.model.IndexPlan;
import org.apache.shardingsphere.mcp.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowExecutionServiceTest {
    
    @Test
    void assertApplyMasksManualArtifactPackage() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPFeatureExecutionFacade.class), "session-1", "plan-1", List.of(), "manual-only");
        Map<?, ?> actualManualArtifactPackage = (Map<?, ?>) actualResponse.get("manual_artifact_package");
        Map<?, ?> actualArtifact = (Map<?, ?>) ((List<?>) actualManualArtifactPackage.get("distsql_artifacts")).get(0);
        assertThat(actualResponse.get("status"), is("awaiting-manual-execution"));
        assertThat(actualArtifact.get("sql"), is("CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
    }
    
    @Test
    void assertApplyRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot());
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPFeatureExecutionFacade.class), "session-2", "plan-1", List.of(), "");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertApplyRejectsInvalidLifecycleStatus() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("clarifying");
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPFeatureExecutionFacade.class), "session-1", "plan-1", List.of(), "");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
    }
    
    @Test
    void assertApplyExecutesApprovedArtifacts() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, executionFacade, "session-1", "plan-1",
                List.of("ddl", "index_ddl", "rule_distsql"), "");
        assertThat(actualResponse.get("status"), is("completed"));
        assertThat(((List<?>) actualResponse.get("executed_ddl")).size(), is(2));
        assertThat(((List<?>) actualResponse.get("executed_distsql")).size(), is(1));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("executed"));
        verify(executionFacade, times(3)).execute(any());
    }
    
    @Test
    void assertApplyCompletesWhenArtifactsAreNotApproved() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, executionFacade, "session-1", "plan-1", List.of("review"), "");
        List<?> actualStepResults = (List<?>) actualResponse.get("step_results");
        assertThat(actualResponse.get("status"), is("completed"));
        assertThat(((List<?>) actualResponse.get("skipped_artifacts")).size(), is(3));
        assertThat(((Map<?, ?>) actualStepResults.get(0)).get("status"), is("skipped"));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("executed"));
        verify(executionFacade, never()).execute(any());
    }
    
    @Test
    void assertApplyReturnsDdlExecutionFailureForDdlArtifact() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("ddl failed"));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, executionFacade, "session-1", "plan-1", List.of("ddl"), "");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        Map<?, ?> actualStep = (Map<?, ?>) ((List<?>) actualResponse.get("step_results")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.DDL_EXECUTION_FAILED));
        assertThat(actualStep.get("artifact_type"), is("add-column"));
    }
    
    @Test
    void assertApplyReturnsDdlExecutionFailureForIndexArtifact() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("index failed"));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, executionFacade, "session-1", "plan-1", List.of("index_ddl"), "");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        Map<?, ?> actualStep = (Map<?, ?>) ((List<?>) actualResponse.get("step_results")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.DDL_EXECUTION_FAILED));
        assertThat(actualStep.get("artifact_type"), is("create-index"));
    }
    
    @Test
    void assertApplyReturnsRuleExecutionFailureForRuleArtifact() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "ALTER MASK RULE orders"));
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("rule failed"));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, executionFacade, "session-1", "plan-1", List.of("rule_distsql"), "");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.RULE_EXECUTION_FAILED));
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
