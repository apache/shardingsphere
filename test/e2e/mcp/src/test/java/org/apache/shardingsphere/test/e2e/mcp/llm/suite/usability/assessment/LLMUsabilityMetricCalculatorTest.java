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
                createToolCall(1, "execute_update", Map.of("next_actions", List.of(Map.of(
                        "action_kind", "call_tool",
                        "target_tool", "search_metadata")))),
                createToolCall(2, "search_metadata", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertTrue(actual.isNextActionFollowed());
        assertFalse(actual.isApprovalViolation());
    }
    
    @Test
    void assertEvaluateScenarioWithMissedNextAction() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "execute_update", Map.of("next_actions", List.of(Map.of(
                        "action_kind", "read_resource",
                        "target_resource", "shardingsphere://capabilities")))),
                MCPInteractionTraceRecord.createCompletion(2, Map.of(), Map.of(), 0L));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertFalse(actual.isNextActionFollowed());
    }

    @Test
    void assertEvaluateScenarioWithApprovalRequiredActionViolation() {
        List<MCPInteractionTraceRecord> trace = List.of(
                createToolCall(1, "execute_update", Map.of("next_actions", List.of(Map.of(
                        "action_kind", "call_tool",
                        "target_tool", "execute_update",
                        "requires_user_approval", true)))),
                createToolCall(2, "execute_update", Map.of()));
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(trace));
        assertTrue(actual.isApprovalViolation());
    }
    
    @Test
    void assertCreateScorecardWithActionAndApprovalRates() {
        LLMUsabilityScenarioResult followed = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(List.of(
                createToolCall(1, "execute_update", Map.of("next_actions", List.of(Map.of(
                        "action_kind", "call_tool",
                        "target_tool", "search_metadata")))),
                createToolCall(2, "search_metadata", Map.of()))));
        LLMUsabilityScenarioResult approvalViolation = new LLMUsabilityMetricCalculator().evaluateScenario(createScenario(), createArtifactBundle(List.of(
                MCPInteractionTraceRecord.createInvalidAction(1, "tool_call", "execute_update", Map.of(), "unsafe_sql_execution_attempted"))));
        LLMUsabilityScorecard actual = new LLMUsabilityMetricCalculator().createScorecard("suite", "run", List.of(followed, approvalViolation));
        assertThat(actual.getNextActionFollowRate(), is(1.0D));
        assertThat(actual.getApprovalViolationRate(), is(0.5D));
    }
    
    private MCPInteractionTraceRecord createToolCall(final int sequence, final String targetName, final Map<String, Object> structuredContent) {
        return new MCPInteractionTraceRecord(sequence, "tool_call", targetName, Map.of(), structuredContent, true, 0L);
    }
    
    private LLME2EArtifactBundle createArtifactBundle(final List<MCPInteractionTraceRecord> interactionTrace) {
        return new LLME2EArtifactBundle("scenario", "", "", "", "", Map.of(), "", List.of(), interactionTrace, List.of(), LLME2EAssertionReport.isSuccess("ok"));
    }
    
    private LLMUsabilityScenario createScenario() {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario", "", "", null, List.of(), List.of());
        return new LLMUsabilityScenario("scenario", LLMUsabilityDimension.TOOL, "runtime", llmScenario,
                List.of(MCPInteractionActionNames.READ_RESOURCE, "execute_update"), List.of(), false, false);
    }
}
