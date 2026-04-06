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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMUsabilityRegressionCheckerTest {
    
    @Test
    void assertCompareWithRegression() {
        LLMUsabilityScorecard baseline = new LLMUsabilityScorecard("suite-a", "baseline", 0.90D, 0.80D, 0.05D, 2.50D,
                1.0D, 0.05D, 0.80D, 0.50D, List.of(), List.of());
        LLMUsabilityScorecard candidate = new LLMUsabilityScorecard("suite-a", "candidate", 0.80D, 0.68D, 0.12D, 3.20D,
                0.90D, 0.20D, 0.70D, 0.50D, List.of(), List.of());
        
        LLMUsabilityComparisonResult actual = new LLMUsabilityRegressionChecker().compare(baseline, candidate,
                LLMUsabilityRegressionBudget.createMinimalBaselineBudget());
        
        assertTrue(actual.regressionDetected());
        assertThat(actual.regressedMetrics().toString(), containsString("invalid_call_rate"));
        assertThat(actual.decision(), is("regression_detected"));
    }
    
    @Test
    void assertCompareWithinBudget() {
        LLMUsabilityScorecard baseline = new LLMUsabilityScorecard("suite-a", "baseline", 0.90D, 0.80D, 0.05D, 2.50D,
                1.0D, 0.05D, 0.80D, 0.50D, List.of(), List.of());
        LLMUsabilityScorecard candidate = new LLMUsabilityScorecard("suite-a", "candidate", 0.89D, 0.82D, 0.04D, 2.40D,
                1.0D, 0.05D, 0.82D, 0.50D, List.of(), List.of());
        
        LLMUsabilityComparisonResult actual = new LLMUsabilityRegressionChecker().compare(baseline, candidate,
                LLMUsabilityRegressionBudget.createMinimalBaselineBudget());
        
        assertFalse(actual.regressionDetected());
        assertThat(actual.improvedMetrics().toString(), containsString("first_correct_action_rate"));
        assertThat(actual.decision(), is("within_budget"));
    }
}
