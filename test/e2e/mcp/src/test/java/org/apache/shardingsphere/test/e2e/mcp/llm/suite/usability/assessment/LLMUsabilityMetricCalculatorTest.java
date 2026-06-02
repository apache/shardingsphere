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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment;

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMUsabilityMetricCalculatorTest {
    
    @Test
    void assertEvaluateScenarioWithFollowedNextAction() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_execute_update", Map.of("next_actions", List.of(Map.of(
                        "type", "tool_call",
                        "tool_name", "database_gateway_search_metadata")))),
                createToolCall(2, "database_gateway_search_metadata", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertTrue(actual.isNextActionFollowed());
        assertFalse(actual.isApprovalViolation());
    }
    
    @Test
    void assertEvaluateScenarioWithMissedNextAction() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_execute_update", Map.of("next_actions", List.of(Map.of(
                        "type", "resource_read",
                        "resource_uri", "shardingsphere://capabilities")))),
                MCPInteractionTraceRecord.createCompletion(2, Map.of(), Map.of(), 0L));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertFalse(actual.isNextActionFollowed());
    }
    
    @Test
    void assertEvaluateScenarioWithNestedRecoveryNextAction() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_execute_query", Map.of("recovery", Map.of("next_actions", List.of(Map.of(
                        "type", "resource_read",
                        "resource_uri", "shardingsphere://databases"))))),
                MCPInteractionTraceRecord.createResourceRead(2, "shardingsphere://databases", Map.of(), 0L));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertTrue(actual.isNextActionFollowed());
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "regular tool call guidance, database_gateway_execute_update, '', database_gateway_execute_update",
            "execute side-effect guidance, database_gateway_execute_update, execute, database_gateway_execute_query",
            "review-then-execute side-effect guidance, database_gateway_apply_workflow, review-then-execute, database_gateway_execute_query"
    })
    void assertEvaluateScenarioWithToolCallNextActionGuidance(final String name, final String nextActionToolName, final String executionMode, final String nextToolName) {
        Map<String, Object> nextAction = executionMode.isEmpty()
                ? Map.of("type", "tool_call", "tool_name", nextActionToolName)
                : Map.of("type", "tool_call", "tool_name", nextActionToolName, "arguments", Map.of("execution_mode", executionMode));
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, nextActionToolName, Map.of("next_actions", List.of(nextAction))),
                createToolCall(2, nextToolName, Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertTrue(actual.isNextActionFollowed());
        assertFalse(actual.isApprovalViolation());
    }
    
    @Test
    void assertEvaluateScenarioWithNestedToolCallNextActionFollowed() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_apply_workflow", Map.of("recovery", Map.of("next_actions", List.of(Map.of(
                        "type", "tool_call",
                        "tool_name", "database_gateway_apply_workflow"))))),
                createToolCall(2, "database_gateway_apply_workflow", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertTrue(actual.isNextActionFollowed());
        assertFalse(actual.isApprovalViolation());
    }
    
    @Test
    void assertEvaluateScenarioWithUnsafeWorkflowExecutionViolation() {
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(List.of(
                MCPInteractionTraceRecord.createInvalidAction(1, "tool_call", "database_gateway_apply_workflow", Map.of(), "unsafe_workflow_execution_attempted"))));
        assertTrue(actual.isApprovalViolation());
    }
    
    @Test
    void assertEvaluateScenarioWithDifferentNextActionNotFollowed() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_execute_update", Map.of("next_actions", List.of(Map.of(
                        "type", "tool_call",
                        "tool_name", "database_gateway_execute_update")))),
                createToolCall(2, "database_gateway_execute_query", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertFalse(actual.isNextActionFollowed());
        assertFalse(actual.isApprovalViolation());
    }
    
    @Test
    void assertEvaluateRecoveryScenarioWithCorrectedResource() {
        List<MCPInteractionTraceRecord> trace = List.of(
                MCPInteractionTraceRecord.createResourceRead(1, "shardingsphere://databases/unknown/schemas/unknown/tables/orders",
                        Map.of("found", false, "empty_state", Map.of("state", "not_found"), "next_actions", List.of(Map.of(
                                "type", "resource_read",
                                "resource_uri", "shardingsphere://databases/unknown/schemas/unknown/tables"))),
                        0L),
                MCPInteractionTraceRecord.createResourceRead(2, "shardingsphere://databases/logic_db/schemas/public/tables/orders",
                        Map.of("found", true), 0L));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createRecoveryScenario(), createArtifactBundle(trace));
        assertTrue(actual.isSuccess());
        assertTrue(actual.isRecoveredAfterError());
        assertTrue(actual.isNextActionFollowed());
        assertThat(actual.getInvalidCallCount(), is(0));
    }
    
    @Test
    void assertEvaluateRecoveryScenarioWithExpectedCategory() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_execute_query", Map.of("error_code", "invalid_request", "recovery", Map.of("recovery_category", "missing_context"))),
                createToolCall(2, "database_gateway_execute_query", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createExpectedRecoveryScenario("missing_context"), createArtifactBundle(trace));
        assertTrue(actual.isSuccess());
        assertTrue(actual.isRecoveredAfterError());
        assertThat(actual.getInvalidCallCount(), is(0));
    }
    
    @Test
    void assertEvaluateRecoveryScenarioWithAmbiguityCategory() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_search_metadata", Map.of("ambiguity_state", Map.of("state", "duplicate_names", "ambiguous", true))),
                createToolCall(2, "database_gateway_search_metadata", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createExpectedRecoveryScenario("ambiguous"), createArtifactBundle(trace));
        assertTrue(actual.isSuccess());
        assertTrue(actual.isRecoveredAfterError());
        assertThat(actual.getInvalidCallCount(), is(0));
    }
    
    @Test
    void assertEvaluateRecoveryScenarioWithUnexpectedCategory() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "database_gateway_execute_query", Map.of("error_code", "invalid_request", "recovery", Map.of("recovery_category", "invalid_enum"))),
                createToolCall(2, "database_gateway_execute_query", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createExpectedRecoveryScenario("missing_context"), createArtifactBundle(trace));
        assertFalse(actual.isSuccess());
        assertFalse(actual.isRecoveredAfterError());
        assertThat(actual.getFailureType(), is("missing_expected_error_path"));
        assertThat(actual.getInvalidCallCount(), is(1));
    }
    
    @Test
    void assertEvaluateRecoveryScenarioWithoutExpectedErrorPath() {
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createContextRecoveryScenario(), createArtifactBundle(List.of(
                createToolCall(1, "database_gateway_plan_mask_rule", Map.of()),
                createToolCall(2, "database_gateway_apply_workflow", Map.of()))));
        assertTrue(actual.isSuccess());
        assertTrue(actual.isRecoveredAfterError());
    }
    
    @Test
    void assertCreateScorecardWithActionAndApprovalRates() {
        LLMUsabilityMetricCalculator calculator = new LLMUsabilityMetricCalculator();
        LLMUsabilityScenarioResult followed = calculator.evaluateScenario(createScenario(), createArtifactBundle(List.of(
                createToolCall(1, "database_gateway_execute_update", Map.of("next_actions", List.of(Map.of(
                        "type", "tool_call",
                        "tool_name", "database_gateway_search_metadata")))),
                createToolCall(2, "database_gateway_search_metadata", Map.of()))));
        LLMUsabilityScenarioResult approvalViolation = calculator.evaluateScenario(createScenario(), createArtifactBundle(List.of(
                MCPInteractionTraceRecord.createInvalidAction(1, "tool_call", "database_gateway_execute_update", Map.of(), "unsafe_sql_execution_attempted"))));
        LLMUsabilityScorecard actual = calculator.createScorecard("suite", "run", List.of(followed, approvalViolation));
        assertThat(actual.getNextActionFollowRate(), is(1.0D));
        assertThat(actual.getApprovalViolationRate(), is(0.5D));
        assertThat(actual.getNaturalTaskSuccessRate(), is(1.0D));
        assertThat(actual.getProtocolContractSuccessRate(), is(1.0D));
        assertThat(actual.getNativeToolCallRate(), is(1.0D));
        assertThat(actual.getHarnessRecoveryRate(), is(0.0D));
        assertTrue(actual.getOverallScore() < 100.0D);
        assertFalse(actual.isFullScore());
    }
    
    @Test
    void assertCreateScorecardWithFullScore() {
        LLMUsabilityScenarioResult actualScenario = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(List.of(
                createToolCall(1, "database_gateway_execute_update", Map.of("next_actions", List.of(Map.of(
                        "type", "tool_call",
                        "tool_name", "database_gateway_search_metadata")))),
                createToolCall(2, "database_gateway_search_metadata", Map.of()))));
        LLMUsabilityScorecard actual = new LLMUsabilityMetricCalculator().createScorecard("suite", "run", List.of(actualScenario));
        assertThat(actual.getOverallScore(), is(100.0D));
        assertTrue(actual.isFullScore());
        assertThat(actual.getNaturalTaskSuccessRate(), is(1.0D));
        assertThat(actual.getProtocolContractSuccessRate(), is(1.0D));
        assertThat(actual.getResourceHitRate(), is(1.0D));
        assertThat(actual.getRecoveryRate(), is(1.0D));
        assertThat(actual.getNativeToolCallRate(), is(1.0D));
        assertThat(actual.getHarnessRecoveryRate(), is(0.0D));
    }
    
    @Test
    void assertCreateScorecardWithHarnessRecoveryNotFullScore() {
        LLMUsabilityScenarioResult actualScenario = new LLMUsabilityMetricCalculator().evaluateScenario(createRequiredToolScenario(), createArtifactBundle(List.of(
                createHarnessTextRecovery(1, "database_gateway_execute_update", Map.of("next_actions", List.of(Map.of(
                        "type", "tool_call",
                        "tool_name", "database_gateway_search_metadata")))),
                createToolCall(2, "database_gateway_search_metadata", Map.of()))));
        LLMUsabilityScorecard actual = new LLMUsabilityMetricCalculator().createScorecard("suite", "run", List.of(actualScenario));
        assertThat(actual.getOverallScore(), is(100.0D));
        assertFalse(actual.isFullScore());
        assertThat(actual.getNaturalTaskSuccessRate(), is(1.0D));
        assertThat(actual.getProtocolContractSuccessRate(), is(1.0D));
        assertThat(actual.getNativeToolCallRate(), is(0.0D));
        assertThat(actual.getHarnessRecoveryRate(), is(1.0D));
    }
    
    @Test
    void assertCreateScorecardWithArgumentNormalizationAsNativeCoverage() {
        LLMUsabilityScenarioResult actualScenario = new LLMUsabilityMetricCalculator().evaluateScenario(createRequiredToolScenario(), createArtifactBundle(List.of(
                createArgumentNormalizedToolCall(1, "database_gateway_execute_update", Map.of()))));
        LLMUsabilityScorecard actual = new LLMUsabilityMetricCalculator().createScorecard("suite", "run", List.of(actualScenario));
        assertThat(actual.getOverallScore(), is(100.0D));
        assertTrue(actual.isFullScore());
        assertThat(actual.getNativeToolCallRate(), is(1.0D));
        assertThat(actual.getHarnessRecoveryRate(), is(0.0D));
    }
    
    @Test
    void assertCreateScorecardWithNaturalAndProtocolSuccessRates() {
        LLMUsabilityMetricCalculator calculator = new LLMUsabilityMetricCalculator();
        LLMUsabilityScenarioResult naturalScenario = calculator.evaluateScenario(createScenario(), createArtifactBundle(List.of(
                createToolCall(1, "database_gateway_execute_update", Map.of()))));
        LLMUsabilityScenarioResult protocolScenario = calculator.evaluateScenario(createProtocolScenario(), createFailedArtifactBundle(List.of(
                createToolCall(1, "mcp_read_resource", Map.of()))));
        LLMUsabilityScorecard actual = calculator.createScorecard("suite", "run", List.of(naturalScenario, protocolScenario));
        assertThat(actual.getTaskSuccessRate(), is(0.5D));
        assertThat(actual.getNaturalTaskSuccessRate(), is(1.0D));
        assertThat(actual.getProtocolContractSuccessRate(), is(0.0D));
        assertFalse(actual.isFullScore());
    }
    
    private MCPInteractionTraceRecord createToolCall(final int sequence, final String targetName, final Map<String, Object> structuredContent) {
        return new MCPInteractionTraceRecord(sequence, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, targetName, Map.of(), structuredContent, true, 0L);
    }
    
    private MCPInteractionTraceRecord createHarnessTextRecovery(final int sequence, final String targetName, final Map<String, Object> structuredContent) {
        return new MCPInteractionTraceRecord(sequence, "tool_call", MCPInteractionTraceRecord.HARNESS_TEXT_RECOVERY_ORIGIN, targetName, Map.of(), structuredContent, true, 0L);
    }
    
    private MCPInteractionTraceRecord createArgumentNormalizedToolCall(final int sequence, final String targetName, final Map<String, Object> structuredContent) {
        return new MCPInteractionTraceRecord(sequence, "tool_call", MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN, targetName, Map.of(), structuredContent, true, 0L);
    }
    
    private LLME2EArtifactBundle createArtifactBundle(final List<MCPInteractionTraceRecord> interactionTrace) {
        return new LLME2EArtifactBundle("scenario", "", "", "", "", Map.of(), "", List.of(), interactionTrace, List.of(), LLME2EAssertionReport.isSuccess("ok"));
    }
    
    private LLME2EArtifactBundle createFailedArtifactBundle(final List<MCPInteractionTraceRecord> interactionTrace) {
        return new LLME2EArtifactBundle("scenario", "", "", "", "", Map.of(), "", List.of(), interactionTrace, List.of(), LLME2EAssertionReport.failure("failed", "failed"));
    }
    
    private LLMUsabilityScenario createScenario() {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario", "", "", null, List.of(), List.of());
        return new LLMUsabilityScenario("scenario", LLMUsabilityDimension.TOOL, "runtime", List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "natural"), llmScenario,
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_update"), List.of(), false, false, "");
    }
    
    private LLMUsabilityScenario createRequiredToolScenario() {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario", "", "", null, List.of("database_gateway_execute_update"), List.of("database_gateway_execute_update"));
        return new LLMUsabilityScenario("scenario", LLMUsabilityDimension.TOOL, "runtime", List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "natural"), llmScenario,
                List.of("database_gateway_execute_update"), List.of(), false, false, "");
    }
    
    private LLMUsabilityScenario createProtocolScenario() {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario", "", "", null, List.of("mcp_read_resource"), List.of("mcp_read_resource"));
        return new LLMUsabilityScenario("scenario", LLMUsabilityDimension.RESOURCE, "runtime", List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "protocol"), llmScenario,
                List.of("mcp_read_resource"), List.of(), false, false, "");
    }
    
    private LLMUsabilityScenario createRecoveryScenario() {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario", "", "", null, List.of(), List.of());
        return new LLMUsabilityScenario("scenario", LLMUsabilityDimension.RECOVERY, "runtime", List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "extended", "recovery"), llmScenario,
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders"), true, true, "not_found");
    }
    
    private LLMUsabilityScenario createExpectedRecoveryScenario(final String expectedRecoveryCategory) {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario", "", "", null, List.of(), List.of());
        return new LLMUsabilityScenario("scenario", LLMUsabilityDimension.RECOVERY, "runtime", List.of(LLMUsabilityScenario.NATURAL_TASK_TAG, "extended", "recovery"), llmScenario,
                List.of("database_gateway_execute_query", "database_gateway_search_metadata"), List.of(), false, true, expectedRecoveryCategory);
    }
    
    private LLMUsabilityScenario createContextRecoveryScenario() {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario", "", "", null, List.of(), List.of());
        return new LLMUsabilityScenario("scenario", LLMUsabilityDimension.RECOVERY, "runtime", List.of(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG, "extended", "workflow", "recovery"), llmScenario,
                List.of("database_gateway_plan_mask_rule"), List.of(), false, false, "");
    }
}
