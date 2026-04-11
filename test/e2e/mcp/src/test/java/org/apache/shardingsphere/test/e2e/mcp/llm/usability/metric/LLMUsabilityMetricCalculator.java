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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.metric;

import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityDimension;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityDimensionScore;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenarioResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScorecard;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

public final class LLMUsabilityMetricCalculator {
    
    public LLMUsabilityScenarioResult evaluateScenario(final LLMUsabilityScenario scenario, final LLME2EArtifactBundle artifactBundle) {
        List<MCPInteractionTraceRecord> interactionTrace = artifactBundle.interactionTrace();
        boolean firstCorrectAction = interactionTrace.isEmpty() || scenario.expectedFirstActionNames().isEmpty()
                ? !interactionTrace.isEmpty()
                : scenario.expectedFirstActionNames().contains(interactionTrace.get(0).targetName());
        int invalidCallCount = getInvalidCallCount(interactionTrace);
        boolean resourceHit = hasResourceHit(scenario.expectedResourceUris(), interactionTrace);
        boolean errorInteractionObserved = hasErrorInteraction(interactionTrace);
        boolean boundaryConfusion = !scenario.expectedFirstActionNames().isEmpty() && !firstCorrectAction;
        LLME2EAssertionReport assertionReport = artifactBundle.assertionReport();
        boolean success = assertionReport.success();
        String failureType = assertionReport.failureType();
        String message = assertionReport.message();
        if (success && scenario.resourceHitRequired() && !resourceHit) {
            success = false;
            failureType = "boundary_confusion";
            message = "Scenario required a resource hit but the trace did not contain the expected resource URI.";
            boundaryConfusion = true;
        }
        if (success && scenario.recoveryExpected() && !errorInteractionObserved) {
            success = false;
            failureType = "missing_expected_error_path";
            message = "Scenario expected one recoverable MCP error before final success.";
        }
        boolean recoveredAfterError = success && errorInteractionObserved;
        double queryAnswerFidelity = scenario.isQueryScenario() ? success ? 1.0D : 0.0D : 0.0D;
        boolean degradedSuccess = success && (invalidCallCount > 0 || boundaryConfusion || interactionTrace.size() > scenario.llmScenario().requiredToolNames().size());
        return new LLMUsabilityScenarioResult(scenario.scenarioId(), scenario.dimension(), scenario.runtimeKind(), success, failureType, message,
                firstCorrectAction, invalidCallCount, interactionTrace.size(), resourceHit, recoveredAfterError, queryAnswerFidelity,
                boundaryConfusion, degradedSuccess, interactionTrace);
    }
    
    public LLMUsabilityScorecard createScorecard(final String suiteId, final String runId, final List<LLMUsabilityScenarioResult> scenarioResults) {
        double taskSuccessRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::success);
        double firstCorrectActionRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::firstCorrectAction);
        double invalidCallRate = getInvalidCallRate(scenarioResults);
        double averageRoundTrips = getAverageRoundTrips(scenarioResults);
        double queryAnswerFidelity = getQueryAnswerFidelity(scenarioResults);
        double boundaryConfusionRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::boundaryConfusion);
        double resourceHitRate = getResourceHitRate(scenarioResults);
        double recoveryRate = getRecoveryRate(scenarioResults);
        List<LLMUsabilityDimensionScore> dimensionScores = new LinkedList<>();
        for (LLMUsabilityDimension each : LLMUsabilityDimension.values()) {
            dimensionScores.add(createDimensionScore(each, scenarioResults));
        }
        return new LLMUsabilityScorecard(suiteId, runId, taskSuccessRate, firstCorrectActionRate, invalidCallRate, averageRoundTrips,
                queryAnswerFidelity, boundaryConfusionRate, resourceHitRate, recoveryRate, dimensionScores, scenarioResults);
    }
    
    private LLMUsabilityDimensionScore createDimensionScore(final LLMUsabilityDimension dimension, final List<LLMUsabilityScenarioResult> scenarioResults) {
        List<LLMUsabilityScenarioResult> matchedResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (dimension == each.dimension()) {
                matchedResults.add(each);
            }
        }
        int scenarioCount = matchedResults.size();
        double successRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::success);
        double firstCorrectActionRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::firstCorrectAction);
        double invalidCallRate = getInvalidCallRate(matchedResults);
        double averageRoundTrips = getAverageRoundTrips(matchedResults);
        double resourceHitRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::resourceHit);
        double recoveryRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::recoveredAfterError);
        double queryAnswerFidelity = getAverageQueryAnswerFidelity(matchedResults);
        double boundaryConfusionRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::boundaryConfusion);
        return new LLMUsabilityDimensionScore(dimension, scenarioCount, successRate, firstCorrectActionRate, invalidCallRate,
                averageRoundTrips, resourceHitRate, recoveryRate, queryAnswerFidelity, boundaryConfusionRate);
    }
    
    private double calculateRate(final List<LLMUsabilityScenarioResult> scenarioResults, final Predicate<LLMUsabilityScenarioResult> matcher) {
        if (scenarioResults.isEmpty()) {
            return 0.0D;
        }
        int matchedCount = 0;
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (matcher.test(each)) {
                matchedCount++;
            }
        }
        return matchedCount * 1.0D / scenarioResults.size();
    }
    
    private double getQueryAnswerFidelity(final List<LLMUsabilityScenarioResult> scenarioResults) {
        List<LLMUsabilityScenarioResult> queryResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (LLMUsabilityDimension.QUERY == each.dimension() || LLMUsabilityDimension.RECOVERY == each.dimension() || LLMUsabilityDimension.RESOURCE == each.dimension()) {
                queryResults.add(each);
            }
        }
        return getAverageQueryAnswerFidelity(queryResults);
    }
    
    private double getAverageQueryAnswerFidelity(final List<LLMUsabilityScenarioResult> scenarioResults) {
        if (scenarioResults.isEmpty()) {
            return 0.0D;
        }
        double total = 0.0D;
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            total += each.queryAnswerFidelity();
        }
        return total / scenarioResults.size();
    }
    
    private double getResourceHitRate(final List<LLMUsabilityScenarioResult> scenarioResults) {
        List<LLMUsabilityScenarioResult> resourceRequiredResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (LLMUsabilityDimension.RESOURCE == each.dimension()) {
                resourceRequiredResults.add(each);
            }
        }
        return calculateRate(resourceRequiredResults, LLMUsabilityScenarioResult::resourceHit);
    }
    
    private double getRecoveryRate(final List<LLMUsabilityScenarioResult> scenarioResults) {
        List<LLMUsabilityScenarioResult> recoveryResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (LLMUsabilityDimension.RECOVERY == each.dimension()) {
                recoveryResults.add(each);
            }
        }
        return calculateRate(recoveryResults, LLMUsabilityScenarioResult::recoveredAfterError);
    }
    
    private double getInvalidCallRate(final List<LLMUsabilityScenarioResult> scenarioResults) {
        int roundTripCountTotal = getRoundTripCountTotal(scenarioResults);
        return 0 == roundTripCountTotal ? 0.0D : getInvalidCallCountTotal(scenarioResults) * 1.0D / roundTripCountTotal;
    }
    
    private double getAverageRoundTrips(final List<LLMUsabilityScenarioResult> scenarioResults) {
        return scenarioResults.isEmpty() ? 0.0D : getRoundTripCountTotal(scenarioResults) * 1.0D / scenarioResults.size();
    }
    
    private int getInvalidCallCountTotal(final List<LLMUsabilityScenarioResult> scenarioResults) {
        int total = 0;
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            total += each.invalidCallCount();
        }
        return total;
    }
    
    private int getRoundTripCountTotal(final List<LLMUsabilityScenarioResult> scenarioResults) {
        int total = 0;
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            total += each.roundTripCount();
        }
        return total;
    }
    
    private int getInvalidCallCount(final List<MCPInteractionTraceRecord> interactionTrace) {
        int result = 0;
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!each.valid() || each.structuredContent().containsKey("error_code")) {
                result++;
            }
        }
        return result;
    }
    
    private boolean hasResourceHit(final List<String> expectedResourceUris, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (expectedResourceUris.isEmpty()) {
            return true;
        }
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!"resource_read".equals(each.actionKind())) {
                continue;
            }
            String resourceUri = String.valueOf(each.arguments().getOrDefault("uri", ""));
            if (expectedResourceUris.contains(resourceUri)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasErrorInteraction(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!each.valid()) {
                return true;
            }
            for (Entry<String, Object> entry : each.structuredContent().entrySet()) {
                if ("error_code".equals(entry.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
