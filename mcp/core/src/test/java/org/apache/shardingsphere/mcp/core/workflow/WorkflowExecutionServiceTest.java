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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.IndexPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowExecutionServiceTest {
    
    @Test
    void assertApplyMasksManualArtifactPackage() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN phone_mask VARCHAR(64)", 10));
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_phone_mask", "phone_mask", "mask lookup", "CREATE INDEX idx_orders_phone_mask ON orders(phone_mask)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "manual-only");
        assertThat(actualResponse.get("status"), is("awaiting-manual-execution"));
        assertThat(actualResponse.get("response_mode"), is("manual_only"));
        assertThat(actualResponse.get("plan_id"), is("plan-1"));
        assertThat(actualResponse.get("execution_mode"), is("manual-only"));
        List<?> actualNextActions = (List<?>) actualResponse.get("next_actions");
        assertThat(actualNextActions.size(), is(1));
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("type"), is("ask_user"));
        assertThat(((Map<?, ?>) actualResponse.get("manual_follow_up")).get("confirmation_field"), is("manual_artifacts_executed"));
        Map<?, ?> actualManualArtifactSummary = (Map<?, ?>) actualResponse.get("manual_artifact_summary");
        assertThat(actualManualArtifactSummary.get("ddl_artifact_count"), is(1));
        assertThat(actualManualArtifactSummary.get("index_plan_count"), is(1));
        assertThat(actualManualArtifactSummary.get("distsql_artifact_count"), is(1));
        assertThat(actualManualArtifactSummary.get("total_artifact_count"), is(3));
        assertTrue((Boolean) actualManualArtifactSummary.get("external_execution_required"));
        assertTrue((Boolean) actualManualArtifactSummary.get("requires_user_confirmation"));
        assertThat(actualManualArtifactSummary.get("validation_blocked_until"), is("manual_artifacts_executed"));
        assertThat(actualManualArtifactSummary.get("validation_tool_after_manual_execution"), is("database_gateway_validate_workflow"));
        assertThat(((Map<?, ?>) actualManualArtifactSummary.get("validation_arguments_after_manual_execution")).get("plan_id"), is("plan-1"));
        Map<?, ?> actualManualArtifactPackage = (Map<?, ?>) actualResponse.get("manual_artifact_package");
        Map<?, ?> actualArtifact = (Map<?, ?>) ((List<?>) actualManualArtifactPackage.get("distsql_artifacts")).getFirst();
        assertThat(actualArtifact.get("sql"), is("CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
    }
    
    @Test
    void assertApplyMasksRuleOnlyManualArtifactPackage() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "manual-only");
        assertFalse(actualResponse.containsKey("executed_ddl"));
        Map<?, ?> actualManualArtifactSummary = (Map<?, ?>) actualResponse.get("manual_artifact_summary");
        assertFalse(actualManualArtifactSummary.containsKey("ddl_artifact_count"));
        assertFalse(actualManualArtifactSummary.containsKey("index_plan_count"));
        assertThat(actualManualArtifactSummary.get("distsql_artifact_count"), is(1));
        assertThat(actualManualArtifactSummary.get("total_artifact_count"), is(1));
        Map<?, ?> actualManualArtifactPackage = (Map<?, ?>) actualResponse.get("manual_artifact_package");
        assertFalse(actualManualArtifactPackage.containsKey("ddl_artifacts"));
        assertFalse(actualManualArtifactPackage.containsKey("index_plan"));
        assertThat(((List<?>) actualManualArtifactPackage.get("distsql_artifacts")).size(), is(1));
    }
    
    @Test
    void assertApplyRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot());
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-2", workflowSessionContext.getRequired("plan-1"), List.of(),
                "review-then-execute");
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualResponse.get("response_mode"), is("terminal"));
        assertThat(actualResponse.get("plan_id"), is("plan-1"));
        assertThat(actualResponse.get("execution_mode"), is("review-then-execute"));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertApplyRejectsInvalidLifecycleStatus() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("clarifying");
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "review-then-execute");
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualResponse.get("response_mode"), is("terminal"));
        assertThat(actualResponse.get("plan_id"), is("plan-1"));
        assertThat(actualResponse.get("execution_mode"), is("review-then-execute"));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
    }
    
    @Test
    void assertApplyRejectsMissingExecutionMode() {
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> executionService.apply(new InMemoryWorkflowSessionContext(),
                mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP,
                "session-1", createSnapshot(), List.of(), ""));
        assertThat(actual.getMessage(), is("database_gateway_apply_workflow execution_mode is required."));
    }
    
    @Test
    void assertApplyRejectsUnsupportedExecutionMode() {
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> executionService.apply(new InMemoryWorkflowSessionContext(),
                mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP,
                "session-1", createSnapshot(), List.of(), "auto-execute"));
        assertThat(actual.getMessage(), is("database_gateway_apply_workflow execution_mode must be one of [preview, review-then-execute, manual-only]."));
    }
    
    @Test
    void assertApplyRejectsUnsupportedApprovedStep() {
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> executionService.apply(new InMemoryWorkflowSessionContext(),
                mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP,
                "session-1", createSnapshot(), List.of("review"), "review-then-execute"));
        assertThat(actual.getMessage(), is("approved_steps must contain only [ddl, index_ddl, rule_distsql]."));
    }
    
    @Test
    void assertApplyRejectsMissingApprovedSteps() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "review-then-execute");
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        verify(executionFacade, never()).execute(any());
    }
    
    @Test
    void assertApplyRejectsExecutionBeforePreview() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("ddl"), "review-then-execute");
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        verify(executionFacade, never()).execute(any());
    }
    
    @Test
    void assertApplyRejectsInvisibleApprovedSteps() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("ddl"), "review-then-execute");
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        verify(executionFacade, never()).execute(any());
    }
    
    @Test
    void assertApplyPreviewDoesNotExecuteAndPersistsPreview() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler = mock(MCPWorkflowApplySynchronizationHandler.class);
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, workflowApplySynchronizationHandler, "session-1", snapshot, List.of(), "preview");
        assertThat(actualResponse.get("status"), is("preview"));
        assertThat(actualResponse.get("response_mode"), is("preview"));
        assertFalse((boolean) actualResponse.get("would_apply"));
        assertThat(((List<?>) actualResponse.get("preview_artifacts")).size(), is(2));
        Map<?, ?> actualReviewFocus = (Map<?, ?>) actualResponse.get("review_focus");
        assertThat(actualReviewFocus.get("artifact_categories"), is(List.of("add-column", "rule_distsql")));
        assertThat(actualReviewFocus.get("side_effect_scope"), is(List.of("physical-structure", "rule-metadata")));
        assertFalse((Boolean) actualReviewFocus.get("manual_only"));
        assertThat(actualReviewFocus.get("approval_field"), is("approved_steps"));
        assertThat(actualReviewFocus.get("approval_values"), is(List.of("ddl", "rule_distsql")));
        assertThat(actualResponse.get("review_summary"), is("Previewed 2 workflow artifacts with side-effect scope physical-structure, rule-metadata. Nothing has been applied."));
        List<?> actualNextActions = (List<?>) actualResponse.get("next_actions");
        assertThat(actualNextActions.size(), is(1));
        Map<?, ?> actualNextAction = (Map<?, ?>) actualNextActions.getFirst();
        assertThat(actualNextAction.get("type"), is("ask_user"));
        assertThat(actualNextAction.get("required_inputs"), is(List.of("approved_steps")));
        assertFalse(actualNextAction.containsKey("depends_on"));
        assertThat(((Map<?, ?>) actualResponse.get("argument_provenance")).get("plan_id"), is("server_generated"));
        assertThat(((Map<?, ?>) actualResponse.get("argument_provenance")).get("execution_mode"), is("server_defaulted"));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("previewed"));
        verify(executionFacade, never()).execute(any());
        verify(workflowApplySynchronizationHandler, never()).synchronize(any(), any(), any(), any(), any());
    }
    
    @Test
    void assertApplyPreviewWithoutArtifacts() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "preview");
        assertThat(actualResponse.get("review_summary"), is("Previewed 0 workflow artifacts. Nothing has been applied."));
        assertFalse(actualResponse.containsKey("approval_question"));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("next_actions")).getFirst()).get("type"), is("terminal"));
    }
    
    @Test
    void assertApplyPreviewUsesRuleArtifactsOnlyForRuleWorkflow() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "preview");
        assertFalse(actualResponse.containsKey("executed_ddl"));
        assertThat(((List<?>) actualResponse.get("preview_artifacts")).size(), is(1));
        Map<?, ?> actualManualArtifactPackage = (Map<?, ?>) actualResponse.get("manual_artifact_package");
        assertFalse(actualManualArtifactPackage.containsKey("ddl_artifacts"));
        assertFalse(actualManualArtifactPackage.containsKey("index_plan"));
        Map<?, ?> actualReviewFocus = (Map<?, ?>) actualResponse.get("review_focus");
        assertThat(actualReviewFocus.get("artifact_categories"), is(List.of("rule_distsql")));
        assertThat(actualReviewFocus.get("side_effect_scope"), is(List.of("rule-metadata")));
    }
    
    @Test
    void assertApplyPreviewMasksRuleArtifactSql() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "preview");
        Map<?, ?> actualPreviewArtifact = (Map<?, ?>) ((List<?>) actualResponse.get("preview_artifacts")).getFirst();
        assertThat(actualPreviewArtifact.get("sql"), is("CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
        assertFalse(String.valueOf(actualResponse).contains("123456"));
    }
    
    @Test
    void assertApplyPreviewBlocksArtifactValidatorIssues() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("mask.rule"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actualResponse = new WorkflowExecutionService().apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP,
                (workflowSnapshot, artifacts) -> List.of(new WorkflowIssue(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED, "error", "review",
                        "Generated workflow artifact is invalid.", "Regenerate the workflow artifact before approval.", true,
                        Map.of("sql", artifacts.iterator().next().displaySql())).toMap()),
                "session-1", snapshot, List.of(), "preview");
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualResponse.get("response_mode"), is("preview"));
        assertFalse((Boolean) actualResponse.get("would_apply"));
        assertThat(((List<?>) actualResponse.get("preview_artifacts")).size(), is(0));
        assertThat(((List<?>) actualResponse.get("manual_artifacts")).size(), is(0));
        assertThat(actualResponse.get("manual_artifact_package"), is(Map.of()));
        assertThat(((List<?>) actualResponse.get("issues")).size(), is(1));
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst();
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        assertThat(actualIssue.get("message"), is("Generated workflow artifact is invalid."));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("failed"));
        verify(executionFacade, never()).execute(any());
    }
    
    @Test
    void assertApplyPreviewForManualOnlyDoesNotRequireApprovalForExport() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getRequest().setExecutionMode("manual-only");
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                mock(MCPFeatureExecutionFacade.class), MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of(), "preview");
        List<?> actualNextActions = (List<?>) actualResponse.get("next_actions");
        assertThat(actualNextActions.size(), is(1));
        Map<?, ?> actualNextAction = (Map<?, ?>) actualNextActions.getFirst();
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("manual-only"));
    }
    
    @Test
    void assertApplyExecutesApprovedArtifacts() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("ddl", "index_ddl", "rule_distsql"), "review-then-execute");
        assertThat(actualResponse.get("status"), is("completed"));
        assertThat(actualResponse.get("response_mode"), is("executed"));
        assertThat(((List<?>) actualResponse.get("applied_artifacts")).size(), is(3));
        assertThat(((List<?>) actualResponse.get("executed_ddl")).size(), is(2));
        assertThat(((List<?>) actualResponse.get("executed_distsql")).size(), is(1));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("executed"));
        verify(executionFacade, times(3)).execute(any());
    }
    
    @Test
    void assertApplyExecutesRawRuleArtifactAndReturnsMaskedSql() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("rule_distsql"), "review-then-execute");
        assertThat(actualResponse.get("status"), is("completed"));
        assertThat(((List<?>) actualResponse.get("executed_distsql")).getFirst(), is("CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("step_results")).getFirst()).get("sql"),
                is("CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
        assertFalse(String.valueOf(actualResponse).contains("123456"));
        verify(executionFacade).execute(argThat(each -> each.getSql().contains("123456")));
    }
    
    @Test
    void assertApplyRequiresManualExecutionForSecretReference() {
        WorkflowContextSnapshot snapshot = createSecretReferenceSnapshot();
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("rule_distsql"), "review-then-execute");
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualResponse.get("response_mode"), is("recovery"));
        assertThat(actualResponse.get("category"), is(MCPDiagnosticCategory.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED));
        assertThat(((Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED));
        assertThat(((List<?>) actualResponse.get("step_results")).size(), is(0));
        assertTrue(String.valueOf(actualResponse).contains("<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>"));
        assertFalse(String.valueOf(actualResponse).contains("placeholder://secret-value-1"));
        assertFalse(String.valueOf(actualResponse).contains("secret_reference:primary.aes-key-value"));
        verify(executionFacade, never()).execute(any());
    }
    
    @Test
    void assertApplySkipsUnapprovedArtifacts() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler = mock(MCPWorkflowApplySynchronizationHandler.class);
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, workflowApplySynchronizationHandler, "session-1", snapshot, List.of("ddl"), "review-then-execute");
        List<?> actualStepResults = (List<?>) actualResponse.get("step_results");
        assertThat(actualResponse.get("status"), is("completed"));
        assertThat(((List<?>) actualResponse.get("skipped_artifacts")).size(), is(2));
        assertThat(((Map<?, ?>) actualStepResults.getFirst()).get("status"), is("passed"));
        assertThat(((Map<?, ?>) actualStepResults.get(1)).get("status"), is("skipped"));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("executed"));
        verify(executionFacade).execute(any());
        verify(workflowApplySynchronizationHandler, never()).synchronize(any(), any(), any(), any(), any());
    }
    
    @Test
    void assertApplyFailsWhenSynchronizationDoesNotConverge() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "ALTER MASK RULE orders"));
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler = mock(MCPWorkflowApplySynchronizationHandler.class);
        doThrow(new WorkflowSynchronizationException(WorkflowIssueCode.RULE_STATE_MISMATCH, "Mask rule is missing.",
                List.of(Map.of("code", WorkflowIssueCode.RULE_STATE_MISMATCH)))).when(workflowApplySynchronizationHandler).synchronize(any(), any(), any(), any(), any());
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, workflowApplySynchronizationHandler, "session-1", snapshot, List.of("rule_distsql"), "review-then-execute");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst();
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("failed"));
    }
    
    @Test
    void assertApplyReturnsDdlExecutionFailureForDdlArtifact() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN order_id_cipher VARCHAR(32)", 10));
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("ddl failed"));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("ddl"), "review-then-execute");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst();
        Map<?, ?> actualStep = (Map<?, ?>) ((List<?>) actualResponse.get("step_results")).getFirst();
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.DDL_EXECUTION_FAILED));
        assertThat(actualStep.get("artifact_type"), is("add-column"));
    }
    
    @Test
    void assertApplyReturnsDdlExecutionFailureForIndexArtifact() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.getIndexPlans().add(new IndexPlan("idx_orders_order_id_cipher", "order_id_cipher", "assist lookup", "CREATE INDEX idx_orders_order_id_cipher ON orders(order_id_cipher)"));
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("index failed"));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("index_ddl"), "review-then-execute");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst();
        Map<?, ?> actualStep = (Map<?, ?>) ((List<?>) actualResponse.get("step_results")).getFirst();
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.DDL_EXECUTION_FAILED));
        assertThat(actualStep.get("artifact_type"), is("create-index"));
    }
    
    @Test
    void assertApplyReturnsMaskedRuleExecutionFailureForRuleArtifact() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setStatus("previewed");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        WorkflowExecutionService executionService = new WorkflowExecutionService();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("Failed to execute CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        Map<String, Object> actualResponse = executionService.apply(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                executionFacade, MCPWorkflowApplySynchronizationHandler.NO_OP, "session-1", snapshot, List.of("rule_distsql"), "review-then-execute");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).getFirst();
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.RULE_EXECUTION_FAILED));
        assertThat(actualIssue.get("message"), is("Failed to execute CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='******'))"));
        assertFalse(String.valueOf(actualResponse).contains("123456"));
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
    
    private WorkflowContextSnapshot createSecretReferenceSnapshot() {
        WorkflowContextSnapshot result = createSnapshot();
        result.setStatus("previewed");
        result.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='secret_reference:primary.aes-key-value'))"));
        result.getRequest().getPrimaryAlgorithmProperties().put("aes-key-value", "secret_reference:primary.aes-key-value");
        result.getRequest().getPrimaryAlgorithmSecretReferences().put("aes-key-value", SecretReferenceValue.create());
        return result;
    }
}
