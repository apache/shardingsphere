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

package org.apache.shardingsphere.test.e2e.mcp;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMUsabilityMetricCalculatorTest {
    
    private static final LLME2EScenario SCENARIO = new LLME2EScenario("scenario-a", "system", "user",
            new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) AS total_orders FROM orders", 2, List.of()),
            List.of("mcp_read_resource", "execute_query"),
            List.of("mcp_read_resource", "execute_query"));
    
    @Test
    void assertEvaluateScenarioWithResourceHitAndRecovery() {
        LLMUsabilityScenario scenario = new LLMUsabilityScenario("resource-recovery", LLMUsabilityDimension.RECOVERY, "h2", SCENARIO,
                List.of("mcp_read_resource"), List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders"), true, true);
        LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle("resource-recovery", "system", "user", "{\"database\":\"logic_db\"}",
                List.of("{}"), List.of(
                        MCPInteractionTraceRecord.createResourceRead(1, "shardingsphere://databases/unknown/schemas/unknown/tables/orders", Map.of("error_code", "not_found"), 2L),
                        MCPInteractionTraceRecord.createResourceRead(2, "shardingsphere://databases/logic_db/schemas/public/tables/orders", Map.of("items", List.of(Map.of("name", "orders"))), 2L),
                        new MCPInteractionTraceRecord(3, "tool_call", "execute_query", Map.of("sql", "SELECT COUNT(*) AS total_orders FROM orders"),
                                Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), true, 3L)),
                List.of(), LLME2EAssertionReport.success("ok"));
        
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(scenario, artifactBundle);
        
        assertTrue(actual.success());
        assertTrue(actual.resourceHit());
        assertTrue(actual.recoveredAfterError());
        assertThat(actual.invalidCallCount(), is(1));
    }
    
    @Test
    void assertEvaluateScenarioWithBoundaryConfusion() {
        LLMUsabilityScenario scenario = new LLMUsabilityScenario("resource-first", LLMUsabilityDimension.RESOURCE, "h2", SCENARIO,
                List.of("mcp_read_resource"), List.of("shardingsphere://capabilities"), true, false);
        LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle("resource-first", "system", "user", "{\"database\":\"logic_db\"}",
                List.of("{}"), List.of(
                        new MCPInteractionTraceRecord(1, "tool_call", "search_metadata",
                                Map.of("database", "logic_db", "query", "orders"), Map.of("items", List.of()), true, 1L),
                        new MCPInteractionTraceRecord(2, "tool_call", "execute_query", Map.of("sql", "SELECT COUNT(*) AS total_orders FROM orders"),
                                Map.of("result_kind", "result_set", "rows", List.of(List.of(2))), true, 3L)),
                List.of(), LLME2EAssertionReport.success("ok"));
        
        LLMUsabilityScenarioResult actual = new LLMUsabilityMetricCalculator().evaluateScenario(scenario, artifactBundle);
        
        assertFalse(actual.success());
        assertTrue(actual.boundaryConfusion());
        assertThat(actual.failureType(), is("boundary_confusion"));
    }
    
    @Test
    void assertCreateScorecard() {
        List<LLMUsabilityScenarioResult> scenarioResults = List.of(
                new LLMUsabilityScenarioResult("resource-a", LLMUsabilityDimension.RESOURCE, "h2", true, "", "ok", true, 0, 2, true, false, 1.0D, false, false, List.of()),
                new LLMUsabilityScenarioResult("tool-a", LLMUsabilityDimension.TOOL, "h2", false, "boundary_confusion", "bad", false, 1, 3, false, false, 0.0D, true, false, List.of()));
        
        LLMUsabilityScorecard actual = new LLMUsabilityMetricCalculator().createScorecard("suite-a", "run-a", scenarioResults);
        
        assertThat(actual.taskSuccessRate(), is(0.5D));
        assertThat(actual.firstCorrectActionRate(), is(0.5D));
        assertThat(actual.boundaryConfusionRate(), is(0.5D));
        assertThat(actual.dimensionScores().size(), is(4));
    }
}
