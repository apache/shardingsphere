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
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

/**
 * LLM usability metric calculator.
 */
public final class LLMUsabilityMetricCalculator {
    
    /**
     * Evaluate scenario.
     *
     * @param scenario scenario
     * @param artifactBundle artifact bundle
     * @return LLM usability scenario result
     */
    public LLMUsabilityScenarioResult evaluateScenario(final LLMUsabilityScenario scenario, final LLME2EArtifactBundle artifactBundle) {
        List<MCPInteractionTraceRecord> interactionTrace = artifactBundle.getInteractionTrace();
        boolean firstCorrectAction = interactionTrace.isEmpty() || scenario.getExpectedFirstActionNames().isEmpty()
                ? !interactionTrace.isEmpty()
                : scenario.getExpectedFirstActionNames().contains(interactionTrace.get(0).getTargetName());
        int invalidCallCount = getInvalidCallCount(interactionTrace);
        boolean resourceHit = hasResourceHit(scenario.getExpectedResourceUris(), interactionTrace);
        boolean errorInteractionObserved = hasErrorInteraction(interactionTrace);
        boolean boundaryConfusion = !scenario.getExpectedFirstActionNames().isEmpty() && !firstCorrectAction;
        LLME2EAssertionReport assertionReport = artifactBundle.getAssertionReport();
        boolean success = assertionReport.isSuccess();
        String failureType = assertionReport.getFailureType();
        String message = assertionReport.getMessage();
        if (success && scenario.isResourceHitRequired() && !resourceHit) {
            success = false;
            failureType = "boundary_confusion";
            message = "Scenario required a resource hit but the trace did not contain the expected resource URI.";
            boundaryConfusion = true;
        }
        if (success && scenario.isRecoveryExpected() && !errorInteractionObserved) {
            success = false;
            failureType = "missing_expected_error_path";
            message = "Scenario expected one recoverable MCP error before final success.";
        }
        boolean recoveredAfterError = success && errorInteractionObserved;
        double queryAnswerFidelity = scenario.isQueryScenario() ? success ? 1.0D : 0.0D : 0.0D;
        return new LLMUsabilityScenarioResult(scenario.getScenarioId(), scenario.getDimension(), scenario.getRuntimeKind(), success, failureType, message,
                firstCorrectAction, invalidCallCount, interactionTrace.size(), resourceHit, recoveredAfterError, queryAnswerFidelity,
                boundaryConfusion, interactionTrace);
    }
    
    /**
     * Create scorecard.
     *
     * @param suiteId suite id
     * @param runId run id 
     * @param scenarioResults scenario results
     * @return LLM usability scorecard
     */
    public LLMUsabilityScorecard createScorecard(final String suiteId, final String runId, final List<LLMUsabilityScenarioResult> scenarioResults) {
        double taskSuccessRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isSuccess);
        double firstCorrectActionRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isFirstCorrectAction);
        double invalidCallRate = getInvalidCallRate(scenarioResults);
        double averageRoundTrips = getAverageRoundTrips(scenarioResults);
        double queryAnswerFidelity = getQueryAnswerFidelity(scenarioResults);
        double boundaryConfusionRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isBoundaryConfusion);
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
            if (dimension == each.getDimension()) {
                matchedResults.add(each);
            }
        }
        int scenarioCount = matchedResults.size();
        double successRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::isSuccess);
        double firstCorrectActionRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::isFirstCorrectAction);
        double invalidCallRate = getInvalidCallRate(matchedResults);
        double averageRoundTrips = getAverageRoundTrips(matchedResults);
        double resourceHitRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::isResourceHit);
        double recoveryRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::isRecoveredAfterError);
        double queryAnswerFidelity = getAverageQueryAnswerFidelity(matchedResults);
        double boundaryConfusionRate = calculateRate(matchedResults, LLMUsabilityScenarioResult::isBoundaryConfusion);
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
            if (LLMUsabilityDimension.TOOL == each.getDimension() || LLMUsabilityDimension.RECOVERY == each.getDimension() || LLMUsabilityDimension.RESOURCE == each.getDimension()) {
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
            total += each.getQueryAnswerFidelity();
        }
        return total / scenarioResults.size();
    }
    
    private double getResourceHitRate(final List<LLMUsabilityScenarioResult> scenarioResults) {
        List<LLMUsabilityScenarioResult> resourceRequiredResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (LLMUsabilityDimension.RESOURCE == each.getDimension()) {
                resourceRequiredResults.add(each);
            }
        }
        return calculateRate(resourceRequiredResults, LLMUsabilityScenarioResult::isResourceHit);
    }
    
    private double getRecoveryRate(final List<LLMUsabilityScenarioResult> scenarioResults) {
        List<LLMUsabilityScenarioResult> recoveryResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (LLMUsabilityDimension.RECOVERY == each.getDimension()) {
                recoveryResults.add(each);
            }
        }
        return calculateRate(recoveryResults, LLMUsabilityScenarioResult::isRecoveredAfterError);
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
            total += each.getInvalidCallCount();
        }
        return total;
    }
    
    private int getRoundTripCountTotal(final List<LLMUsabilityScenarioResult> scenarioResults) {
        int total = 0;
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            total += each.getRoundTripCount();
        }
        return total;
    }
    
    private int getInvalidCallCount(final List<MCPInteractionTraceRecord> interactionTrace) {
        int result = 0;
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!each.isValid() || each.getStructuredContent().containsKey("error_code")) {
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
            if (!"resource_read".equals(each.getActionKind())) {
                continue;
            }
            String resourceUri = String.valueOf(each.getArguments().getOrDefault("uri", ""));
            if (expectedResourceUris.contains(resourceUri)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasErrorInteraction(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!each.isValid()) {
                return true;
            }
            for (Entry<String, Object> entry : each.getStructuredContent().entrySet()) {
                if ("error_code".equals(entry.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }
}
