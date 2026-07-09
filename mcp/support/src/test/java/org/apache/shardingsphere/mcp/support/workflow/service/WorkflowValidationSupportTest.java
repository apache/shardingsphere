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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
        assertThat(actualResult.get("summary"), is("Workflow validation cannot run for plan `plan-1`."));
        assertThat(actualResult.get("plan_id"), is("plan-1"));
        assertThat(actualResult.get("recovery_guidance"), is("Continue the workflow from the same session that created the plan."));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("next_actions")).getFirst()).get("required_inputs"), is(List.of("same_mcp_session")));
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
        assertThat(actualResult.get("recovery_guidance"), is("Execute the workflow first or continue from a validatable status."));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("next_actions")).getFirst()).get("required_inputs"), is(List.of("validatable_workflow_state")));
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
    void assertValidateAndFinalize() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setSessionId("session-1");
        snapshot.setStatus("executed");
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("passed");
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        Map<String, Object> actualResult = validationSupport.validateAndFinalize(workflowSessionContext, "session-1", snapshot, () -> validationReport);
        assertThat(actualResult.get("response_mode"), is("validation"));
        assertThat(actualResult.get("status"), is("validated"));
        assertThat(snapshot.getValidationReport(), is(validationReport));
    }
    
    @Test
    void assertValidateAndFinalizeRejectsInvalidPrecondition() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setSessionId("session-1");
        snapshot.setStatus("clarifying");
        AtomicBoolean reportCreated = new AtomicBoolean();
        Map<String, Object> actualResult = validationSupport.validateAndFinalize(new TestWorkflowSessionContext(), "session-1", snapshot, () -> {
            reportCreated.set(true);
            return new ValidationReport();
        });
        assertThat(actualResult.get("response_mode"), is("terminal"));
        assertFalse(reportCreated.get());
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
    void assertCreateProjectionValidationSql() {
        assertThat(validationSupport.createProjectionValidationSql(createSnapshot(), "MySQL"), is("SELECT phone FROM orders"));
    }
    
    @Test
    void assertCreateProjectionValidationSqlWithSpecialCharacterIdentifiers() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getRequest().setTable("order detail");
        snapshot.getRequest().setColumn("Phone Number");
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockQuoteCharacter("MySQL", QuoteCharacter.BACK_QUOTE, typedSPILoader, databaseTypedSPILoader);
            assertThat(validationSupport.createProjectionValidationSql(snapshot, "MySQL"), is("SELECT `Phone Number` FROM `order detail`"));
        }
    }
    
    @Test
    void assertCreateProjectionValidationSqlWithPostgreSQLIdentifiers() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        snapshot.getRequest().setTable("order detail");
        snapshot.getRequest().setColumn("Phone Number");
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            mockQuoteCharacter("PostgreSQL", QuoteCharacter.QUOTE, typedSPILoader, databaseTypedSPILoader);
            assertThat(validationSupport.createProjectionValidationSql(snapshot, "PostgreSQL"), is("SELECT \"Phone Number\" FROM \"order detail\""));
        }
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
        assertThat(actualResult.get("summary"), is("Workflow validation passed for plan `plan-1`."));
        assertThat(actualResult.get("status"), is("validated"));
        assertThat(actualResult.get("plan_id"), is("plan-1"));
        assertThat(actualResult.get("recovery_guidance"), is(""));
        List<?> actualNextActions = (List<?>) actualResult.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("type"), is("terminal"));
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
        assertThat(actualResult.get("summary"), is("Workflow validation failed for plan `plan-1` with 1 mismatch(es)."));
        assertThat(((Map<?, ?>) ((List<?>) actualResult.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        assertThat(actualResult.get("status"), is("failed"));
        assertThat(actualResult.get("recovery_guidance"), is("Inspect mismatches, adjust the plan or runtime state, then run database_gateway_validate_workflow again."));
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
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("tool_name"), is("database_gateway_plan_encrypt_rule"));
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
        assertThat(actualResult.get("recovery_guidance"), is("Manual-only artifacts are exported but not executed by MCP. Execute them manually, then run database_gateway_validate_workflow again."));
        assertThat(actualNextAction.get("type"), is("ask_user"));
        assertThat(actualNextAction.get("required_inputs"), is(List.of("manual_artifacts_executed")));
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
        assertThat(actualMismatch.get("remediation"), is("fix it"));
    }
    
    private static void mockQuoteCharacter(final String databaseType, final QuoteCharacter quoteCharacter,
                                           final MockedStatic<TypedSPILoader> typedSPILoader, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getQuoteCharacter()).thenReturn(quoteCharacter);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseTypeFromSPI)).thenReturn(Optional.of(dialectDatabaseMetaData));
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
