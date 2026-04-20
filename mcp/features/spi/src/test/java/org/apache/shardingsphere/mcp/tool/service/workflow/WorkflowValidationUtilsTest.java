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

import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationReport;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationSection;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowValidationUtilsTest {
    
    @Test
    void assertCheckValidatePreconditionsRejectsDifferentSession() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setSessionId("session-1");
        snapshot.setStatus("executed");
        Map<String, Object> actualResult = WorkflowValidationUtils.checkValidatePreconditions("session-2", snapshot);
        assertThat(actualResult.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertCheckValidatePreconditionsRejectsInvalidLifecycleStatus() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setSessionId("session-1");
        snapshot.setStatus("clarifying");
        Map<String, Object> actualResult = WorkflowValidationUtils.checkValidatePreconditions("session-1", snapshot);
        assertThat(actualResult.get("status"), is("failed"));
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
        assertTrue(WorkflowValidationUtils.checkValidatePreconditions("session-1", snapshot).isEmpty());
    }
    
    @Test
    void assertCreateValidationIssuesReturnsMismatchIssue() {
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("failed");
        validationReport.getMismatches().add(Map.of("code", WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        List<Map<String, Object>> actualIssues = WorkflowValidationUtils.createValidationIssues(validationReport);
        assertThat(actualIssues.size(), is(1));
        assertThat(actualIssues.get(0).get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
    }
    
    @Test
    void assertResolveOverallStatusReturnsFailedWhenAnySectionFails() {
        String actualStatus = WorkflowValidationUtils.resolveOverallStatus(
                new ValidationSection("passed", List.of(), ""),
                new ValidationSection("failed", List.of(), ""),
                new ValidationSection("passed", List.of(), ""));
        assertThat(actualStatus, is("failed"));
    }
    
    @Test
    void assertResolveValidationStatusReturnsValidatedWhenReportPassed() {
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("passed");
        assertThat(WorkflowValidationUtils.resolveValidationStatus(validationReport), is("validated"));
    }
    
    @Test
    void assertValidateLogicalMetadataWhenColumnExists() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        ValidationReport validationReport = new ValidationReport();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone"))
                .thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        ValidationSection actualValidationSection = WorkflowValidationUtils.validateLogicalMetadata(snapshot, metadataQueryFacade, validationReport);
        assertThat(actualValidationSection.getStatus(), is("passed"));
        assertThat(validationReport.getMismatches(), is(List.of()));
    }
    
    @Test
    void assertValidateLogicalMetadataWhenColumnMissing() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        ValidationReport validationReport = new ValidationReport();
        ValidationSection actualValidationSection = WorkflowValidationUtils.validateLogicalMetadata(snapshot, mock(MCPMetadataQueryFacade.class), validationReport);
        assertThat(actualValidationSection.getStatus(), is("failed"));
        assertThat(((Map<?, ?>) validationReport.getMismatches().get(0)).get("code"), is(WorkflowIssueCode.LOGICAL_METADATA_MISMATCH));
    }
    
    @Test
    void assertFinalizeValidation() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("executed");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        snapshot.setInteractionPlan(interactionPlan);
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("passed");
        WorkflowContextStore contextStore = new WorkflowContextStore();
        contextStore.save(snapshot);
        Map<String, Object> actualResult = WorkflowValidationUtils.finalizeValidation(contextStore, snapshot, validationReport);
        assertThat(actualResult.get("status"), is("validated"));
        assertThat(contextStore.getRequired("plan-1").getStatus(), is("validated"));
        assertThat(contextStore.getRequired("plan-1").getInteractionPlan().getCurrentStep(), is("validated"));
    }
    
    @Test
    void assertCreateMismatchBuildsExpectedPayload() {
        Map<String, Object> actualMismatch = WorkflowValidationUtils.createMismatch("code", "rule", "expected", "actual", "impact", "fix it");
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
        org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest request = new org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("phone");
        result.setRequest(request);
        return result;
    }
}
