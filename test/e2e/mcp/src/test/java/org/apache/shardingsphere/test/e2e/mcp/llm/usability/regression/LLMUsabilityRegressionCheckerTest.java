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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.regression;

import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScorecard;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class LLMUsabilityRegressionCheckerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCompareCases")
    void assertCompare(final String name, final LLMUsabilityScorecard baseline, final LLMUsabilityScorecard candidate,
                       final boolean expectedRegressionDetected, final String expectedDecision, final String expectedRegressedMetric,
                       final String expectedImprovedMetric, final String expectedStableMetric) {
        LLMUsabilityComparisonResult actual = new LLMUsabilityRegressionChecker().compare(baseline, candidate,
                LLMUsabilityRegressionBudget.createMinimalBaselineBudget());
        assertThat(actual.regressionDetected(), is(expectedRegressionDetected));
        assertThat(actual.decision(), is(expectedDecision));
        if (!expectedRegressedMetric.isEmpty()) {
            assertThat(actual.regressedMetrics().toString(), containsString(expectedRegressedMetric));
        }
        if (!expectedImprovedMetric.isEmpty()) {
            assertThat(actual.improvedMetrics().toString(), containsString(expectedImprovedMetric));
        }
        if (!expectedStableMetric.isEmpty()) {
            assertThat(actual.stableMetrics().toString(), containsString(expectedStableMetric));
        }
    }
    
    static Stream<Arguments> assertCompareCases() {
        LLMUsabilityScorecard baseline = new LLMUsabilityScorecard("suite-a", "baseline", 0.90D, 0.80D, 0.05D, 2.50D,
                1.0D, 0.05D, 0.80D, 0.50D, List.of(), List.of());
        return Stream.of(
                Arguments.of("regression detected", baseline,
                        new LLMUsabilityScorecard("suite-a", "candidate", 0.80D, 0.68D, 0.12D, 3.20D, 0.90D, 0.20D, 0.70D, 0.50D, List.of(), List.of()),
                        true, "regression_detected", "invalid_call_rate", "", ""),
                Arguments.of("within budget with improvements", baseline,
                        new LLMUsabilityScorecard("suite-a", "candidate", 0.89D, 0.82D, 0.04D, 2.40D, 1.0D, 0.05D, 0.82D, 0.50D, List.of(), List.of()),
                        false, "within_budget", "", "first_correct_action_rate", ""),
                Arguments.of("stable metrics", baseline,
                        new LLMUsabilityScorecard("suite-a", "candidate", 0.90D, 0.80D, 0.05D, 2.50D, 1.0D, 0.05D, 0.80D, 0.50D, List.of(), List.of()),
                        false, "within_budget", "", "", "task_success_rate"));
    }
}
