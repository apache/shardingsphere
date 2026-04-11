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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability;

import org.apache.shardingsphere.test.e2e.mcp.llm.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.runtime.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMUsabilityMetricCalculatorTest {
    
    private static final LLME2EScenario SCENARIO = new LLME2EScenario("scenario-a", "system", "user",
            new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2, List.of()),
            List.of("mcp_read_resource", "execute_query"),
            List.of("mcp_read_resource", "execute_query"));
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertEvaluateScenarioCases")
    void assertEvaluateScenario(final String name, final LLMUsabilityScenario scenario, final LLME2EArtifactBundle artifactBundle,
                                final boolean expectedSuccess, final String expectedFailureType, final boolean expectedResourceHit,
                                final boolean expectedRecoveredAfterError, final int expectedInvalidCallCount,
                                final boolean expectedBoundaryConfusion, final double expectedQueryAnswerFidelity,
                                final boolean expectedDegradedSuccess) {
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(scenario, artifactBundle);
        assertThat(actual.success(), is(expectedSuccess));
        assertThat(actual.failureType(), is(expectedFailureType));
        assertThat(actual.resourceHit(), is(expectedResourceHit));
        assertThat(actual.recoveredAfterError(), is(expectedRecoveredAfterError));
        assertThat(actual.invalidCallCount(), is(expectedInvalidCallCount));
        assertThat(actual.boundaryConfusion(), is(expectedBoundaryConfusion));
        assertThat(actual.queryAnswerFidelity(), is(expectedQueryAnswerFidelity));
        assertThat(actual.degradedSuccess(), is(expectedDegradedSuccess));
    }
    
    @Test
    void assertCreateScorecard() {
        List<LLMUsabilityScenarioResult> scenarioResults = List.of(
                new LLMUsabilityScenarioResult("resource-a", LLMUsabilityDimension.RESOURCE, "h2", true, "", "ok", true, 0, 2, true, false, 1.0D, false, false, List.of()),
                new LLMUsabilityScenarioResult("tool-a", LLMUsabilityDimension.TOOL, "h2", false, "boundary_confusion", "bad", false, 1, 3, false, false, 0.0D, true, false, List.of()),
                new LLMUsabilityScenarioResult("recovery-a", LLMUsabilityDimension.RECOVERY, "h2", true, "", "ok", true, 0, 4, true, true, 1.0D, false, true, List.of()));
        
        LLMUsabilityScorecard actual = new LLMUsabilityMetricCalculator().createScorecard("suite-a", "run-a", scenarioResults);
        
        assertThat(actual.taskSuccessRate(), is(2D / 3D));
        assertThat(actual.firstCorrectActionRate(), is(2D / 3D));
        assertThat(actual.invalidCallRate(), is(1D / 9D));
        assertThat(actual.resourceHitRate(), is(1.0D));
        assertThat(actual.recoveryRate(), is(1.0D));
        assertThat(actual.boundaryConfusionRate(), is(1D / 3D));
        assertThat(actual.dimensionScores().size(), is(4));
        assertThat(actual.dimensionScores().get(0).dimension(), is(LLMUsabilityDimension.RESOURCE));
        assertThat(actual.dimensionScores().get(0).scenarioCount(), is(1));
        assertThat(actual.dimensionScores().get(0).resourceHitRate(), is(1.0D));
        assertThat(actual.dimensionScores().get(1).dimension(), is(LLMUsabilityDimension.TOOL));
        assertThat(actual.dimensionScores().get(1).boundaryConfusionRate(), is(1.0D));
        assertThat(actual.dimensionScores().get(3).dimension(), is(LLMUsabilityDimension.RECOVERY));
        assertThat(actual.dimensionScores().get(3).recoveryRate(), is(1.0D));
    }
    
    static Stream<Arguments> assertEvaluateScenarioCases() {
        return Stream.of(
                Arguments.of("resource hit and recovery",
                        new LLMUsabilityScenario("resource-recovery", LLMUsabilityDimension.RECOVERY, "h2", SCENARIO,
                                List.of("mcp_read_resource"), List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders"), true, true),
                        new LLME2EArtifactBundle("resource-recovery", "system", "user", "{\"database\":\"logic_db\"}",
                                List.of("{}"), List.of(
                                        MCPInteractionTraceRecord.createResourceRead(1, "shardingsphere://databases/unknown/schemas/unknown/tables/orders",
                                                Map.of("error_code", "not_found"), 2L),
                                        MCPInteractionTraceRecord.createResourceRead(2, "shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                                Map.of("items", List.of(Map.of("name", "orders"))), 2L),
                                        new MCPInteractionTraceRecord(3, "tool_call", "execute_query", Map.of("sql", "SELECT COUNT(*) AS total_orders FROM orders"),
                                                Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), true, 3L)),
                                List.of(), LLME2EAssertionReport.success("ok")),
                        true, "", true, true, 1, false, 1.0D, true),
                Arguments.of("boundary confusion on first action",
                        new LLMUsabilityScenario("resource-first", LLMUsabilityDimension.RESOURCE, "h2", SCENARIO,
                                List.of("mcp_read_resource"), List.of("shardingsphere://capabilities"), true, false),
                        new LLME2EArtifactBundle("resource-first", "system", "user", "{\"database\":\"logic_db\"}",
                                List.of("{}"), List.of(
                                        new MCPInteractionTraceRecord(1, "tool_call", "search_metadata",
                                                Map.of("database", "logic_db", "query", "orders"), Map.of("items", List.of()), true, 1L),
                                        new MCPInteractionTraceRecord(2, "tool_call", "execute_query", Map.of("sql", "SELECT COUNT(*) AS total_orders FROM orders"),
                                                Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), true, 3L)),
                                List.of(), LLME2EAssertionReport.success("ok")),
                        false, "boundary_confusion", false, false, 0, true, 0.0D, false),
                Arguments.of("missing expected error path",
                        new LLMUsabilityScenario("recovery-without-error", LLMUsabilityDimension.RECOVERY, "h2", SCENARIO,
                                List.of("mcp_read_resource"), List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders"), true, true),
                        new LLME2EArtifactBundle("recovery-without-error", "system", "user", "{\"database\":\"logic_db\"}",
                                List.of("{}"), List.of(
                                        MCPInteractionTraceRecord.createResourceRead(1, "shardingsphere://databases/logic_db/schemas/public/tables/orders",
                                                Map.of("items", List.of(Map.of("name", "orders"))), 2L),
                                        new MCPInteractionTraceRecord(2, "tool_call", "execute_query", Map.of("sql", "SELECT COUNT(*) AS total_orders FROM orders"),
                                                Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), true, 3L)),
                                List.of(), LLME2EAssertionReport.success("ok")),
                        false, "missing_expected_error_path", true, false, 0, false, 0.0D, false));
    }
}
