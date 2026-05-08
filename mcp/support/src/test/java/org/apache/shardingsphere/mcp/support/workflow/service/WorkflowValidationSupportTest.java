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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowValidationSupportTest {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    @Test
    void assertCheckValidatePreconditionsRejectsDifferentSession() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setSessionId("session-1");
        snapshot.setStatus("executed");
        Map<String, Object> actualResult = validationSupport.checkValidatePreconditions("session-2", snapshot);
        assertThat(actualResult.get("status"), is("failed"));
        assertThat(actualResult.get("plan_id"), is("plan-1"));
        assertThat(actualResult.get("recommended_recovery"), is("Continue the workflow from the same session that created the plan."));
        assertFalse((Boolean) actualResult.get("requires_user_approval"));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertCheckValidatePreconditionsRejectsInvalidLifecycleStatus() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setSessionId("session-1");
        snapshot.setStatus("clarifying");
        Map<String, Object> actualResult = validationSupport.checkValidatePreconditions("session-1", snapshot);
        assertThat(actualResult.get("status"), is("failed"));
        assertThat(actualResult.get("plan_id"), is("plan-1"));
        assertThat(actualResult.get("recommended_recovery"), is("Execute the workflow first or continue from a validatable status."));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
    }
    
    @Test
    void assertCheckValidatePreconditionsAcceptsFailedWorkflowInValidatedStep() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setSessionId("session-1");
        snapshot.setStatus("failed");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("validated");
        snapshot.setInteractionPlan(interactionPlan);
        assertTrue(validationSupport.checkValidatePreconditions("session-1", snapshot).isEmpty());
    }
    
    @Test
    void assertResolveOverallStatusReturnsFailedWhenAnySectionFails() {
        String actualStatus = validationSupport.resolveOverallStatus(
                new ValidationSection("passed", List.of(), ""),
                new ValidationSection("failed", List.of(), ""),
                new ValidationSection("passed", List.of(), ""));
        assertThat(actualStatus, is("failed"));
    }
    
    @Test
    void assertValidateLogicalMetadataWhenColumnExists() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        ValidationReport validationReport = new ValidationReport();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone"))
                .thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        ValidationSection actualValidationSection = validationSupport.validateLogicalMetadata(snapshot, metadataQueryFacade, validationReport);
        assertThat(actualValidationSection.getStatus(), is("passed"));
        assertThat(validationReport.getMismatches(), is(List.of()));
    }
    
    @Test
    void assertValidateLogicalMetadataWhenColumnMissing() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        ValidationReport validationReport = new ValidationReport();
        ValidationSection actualValidationSection = validationSupport.validateLogicalMetadata(snapshot, mock(MCPMetadataQueryFacade.class), validationReport);
        assertThat(actualValidationSection.getStatus(), is("failed"));
        assertThat(((Map<?, ?>) validationReport.getMismatches().get(0)).get("code"), is(WorkflowIssueCode.LOGICAL_METADATA_MISMATCH));
    }
    
    @Test
    void assertCreateProjectionValidationSql() {
        assertThat(validationSupport.createProjectionValidationSql(createSnapshot()), is("SELECT phone FROM orders"));
    }
    
    @Test
    void assertValidateSqlExecutability() {
        ValidationReport validationReport = new ValidationReport();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        ValidationSection actualValidationSection = validationSupport.validateSqlExecutability(executionFacade, "session-1", createSnapshot(), validationReport,
                List.of("SELECT phone FROM orders", "SELECT phone FROM orders WHERE phone = 'sample'"), "Validation SQLs are executable from the logical view.");
        assertThat(actualValidationSection.getStatus(), is("passed"));
        assertThat(actualValidationSection.getEvidence(), is(List.of("SELECT phone FROM orders", "SELECT phone FROM orders WHERE phone = 'sample'")));
        assertThat(validationReport.getMismatches(), is(List.of()));
        verify(executionFacade, times(2)).execute(any());
    }
    
    @Test
    void assertValidateSqlExecutabilityWhenExecutionFails() {
        ValidationReport validationReport = new ValidationReport();
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("sql failed"));
        ValidationSection actualValidationSection = validationSupport.validateSqlExecutability(executionFacade, "session-1", createSnapshot(), validationReport,
                List.of("SELECT phone FROM orders"), "Validation SQL is executable from the logical view.");
        assertThat(actualValidationSection.getStatus(), is("failed"));
        assertThat(validationReport.getMismatches().size(), is(1));
        assertThat(validationReport.getMismatches().get(0).get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
    }
    
    @Test
    void assertFinalizeValidationMarksValidatedStatus() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("executed");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        snapshot.setInteractionPlan(interactionPlan);
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("passed");
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        Map<String, Object> actualResult = validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
        assertThat(actualResult.get("response_mode"), is("validation"));
        assertThat(actualResult.get("status"), is("validated"));
        assertThat(actualResult.get("plan_id"), is("plan-1"));
        assertThat(actualResult.get("recommended_recovery"), is(""));
        assertFalse(actualResult.containsKey("recommended_next_tool"));
        List<?> actualNextActions = (List<?>) actualResult.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("type"), is("terminal"));
        assertFalse((Boolean) ((Map<?, ?>) actualNextActions.get(0)).get("requires_user_approval"));
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("validated"));
        assertThat(workflowSessionContext.getRequired("plan-1").getInteractionPlan().getCurrentStep(), is("validated"));
    }
    
    @Test
    void assertFinalizeValidationReturnsMismatchIssueWhenFailed() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("failed");
        Map<String, Object> mismatch = new LinkedHashMap<>(2, 1F);
        mismatch.put("code", WorkflowIssueCode.SQL_EXECUTABILITY_FAILED);
        validationReport.getMismatches().add(mismatch);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        Map<String, Object> actualResult = validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
        assertThat(actualResult.get("response_mode"), is("validation"));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        assertThat(actualResult.get("status"), is("failed"));
        assertThat(actualResult.get("recommended_recovery"), is("Inspect mismatches, adjust the plan or runtime state, then run validate_workflow again."));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("next_actions")).get(0)).get("type"), is("ask_user"));
    }
    
    @Test
    void assertFinalizeValidationRecommendsPlanningToolWhenWorkflowKindExists() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("failed");
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        Map<String, Object> actualResult = validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
        List<?> actualNextActions = (List<?>) actualResult.get("next_actions");
        assertFalse(actualResult.containsKey("recommended_next_tool"));
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("tool_name"), is("plan_encrypt_rule"));
    }
    
    @Test
    void assertFinalizeValidationRequiresManualConfirmation() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setWorkflowKind(WorkflowKind.valueOf("mask.rule"));
        snapshot.getRequest().setExecutionMode("manual-only");
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("failed");
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        Map<String, Object> actualResult = validationSupport.finalizeValidation(workflowSessionContext, snapshot, validationReport);
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualResult.get("next_actions")).get(0);
        assertThat(actualResult.get("recommended_recovery"), is("Manual-only artifacts are exported but not executed by MCP. Execute them manually, then run validate_workflow again."));
        assertTrue((Boolean) actualResult.get("requires_user_approval"));
        assertThat(actualNextAction.get("type"), is("ask_user"));
        assertThat(actualNextAction.get("required_inputs"), is(List.of("manual_artifacts_executed")));
        assertTrue((Boolean) actualNextAction.get("requires_user_approval"));
        assertFalse(actualNextAction.containsKey("tool_name"));
    }
    
    @Test
    void assertCreateMismatchBuildsExpectedPayload() {
        Map<String, Object> actualMismatch = validationSupport.createMismatch("code", "rule", "expected", "actual", "impact", "fix it");
        assertThat(actualMismatch.get("code"), is("code"));
        assertThat(actualMismatch.get("layer"), is("rule"));
        assertThat(actualMismatch.get("expected"), is("expected"));
        assertThat(actualMismatch.get("actual"), is("actual"));
        assertThat(actualMismatch.get("impact"), is("impact"));
        assertThat(actualMismatch.get("suggested_next_action"), is("fix it"));
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setSessionId("session-1");
        result.setStatus("executed");
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("phone");
        result.setRequest(request);
        return result;
    }
}
