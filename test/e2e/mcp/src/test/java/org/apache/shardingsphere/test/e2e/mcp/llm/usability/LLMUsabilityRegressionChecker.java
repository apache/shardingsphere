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

import java.util.LinkedList;
import java.util.List;

final class LLMUsabilityRegressionChecker {
    
    LLMUsabilityComparisonResult compare(final LLMUsabilityScorecard baseline, final LLMUsabilityScorecard candidate,
                                         final LLMUsabilityRegressionBudget regressionBudget) {
        List<String> regressedMetrics = new LinkedList<>();
        List<String> improvedMetrics = new LinkedList<>();
        List<String> stableMetrics = new LinkedList<>();
        classifyHigherIsBetterMetric("task_success_rate", baseline.taskSuccessRate(), candidate.taskSuccessRate(), regressionBudget.maxTaskSuccessRateDrop(),
                regressedMetrics, improvedMetrics, stableMetrics);
        classifyHigherIsBetterMetric("first_correct_action_rate", baseline.firstCorrectActionRate(), candidate.firstCorrectActionRate(),
                regressionBudget.maxFirstCorrectActionRateDrop(), regressedMetrics, improvedMetrics, stableMetrics);
        classifyLowerIsBetterMetric("invalid_call_rate", baseline.invalidCallRate(), candidate.invalidCallRate(),
                regressionBudget.maxInvalidCallRateIncrease(), regressedMetrics, improvedMetrics, stableMetrics);
        classifyLowerIsBetterMetric("average_round_trips", baseline.averageRoundTrips(), candidate.averageRoundTrips(),
                regressionBudget.maxAverageRoundTripsIncrease(), regressedMetrics, improvedMetrics, stableMetrics);
        classifyHigherIsBetterMetric("query_answer_fidelity", baseline.queryAnswerFidelity(), candidate.queryAnswerFidelity(),
                regressionBudget.maxQueryAnswerFidelityDrop(), regressedMetrics, improvedMetrics, stableMetrics);
        classifyLowerIsBetterMetric("boundary_confusion_rate", baseline.boundaryConfusionRate(), candidate.boundaryConfusionRate(),
                regressionBudget.maxBoundaryConfusionRateIncrease(), regressedMetrics, improvedMetrics, stableMetrics);
        boolean regressionDetected = !regressedMetrics.isEmpty();
        String decision = regressionDetected ? "regression_detected" : "within_budget";
        return new LLMUsabilityComparisonResult(baseline.runId(), candidate.runId(), regressionDetected, regressedMetrics, improvedMetrics, stableMetrics, decision);
    }
    
    private void classifyHigherIsBetterMetric(final String metricName, final double baselineValue, final double candidateValue, final double allowedDrop,
                                              final List<String> regressedMetrics, final List<String> improvedMetrics, final List<String> stableMetrics) {
        if (baselineValue - candidateValue > allowedDrop) {
            regressedMetrics.add(metricName);
            return;
        }
        if (candidateValue > baselineValue) {
            improvedMetrics.add(metricName);
            return;
        }
        stableMetrics.add(metricName);
    }
    
    private void classifyLowerIsBetterMetric(final String metricName, final double baselineValue, final double candidateValue, final double allowedIncrease,
                                             final List<String> regressedMetrics, final List<String> improvedMetrics, final List<String> stableMetrics) {
        if (candidateValue - baselineValue > allowedIncrease) {
            regressedMetrics.add(metricName);
            return;
        }
        if (candidateValue < baselineValue) {
            improvedMetrics.add(metricName);
            return;
        }
        stableMetrics.add(metricName);
    }
}
