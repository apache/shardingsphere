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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
        boolean nextActionFollowed = isNextActionFollowed(interactionTrace);
        boolean approvalViolation = hasApprovalViolation(interactionTrace);
        return new LLMUsabilityScenarioResult(scenario.getScenarioId(), scenario.getDimension(), scenario.getRuntimeKind(), success, failureType, message,
                firstCorrectAction, invalidCallCount, interactionTrace.size(), resourceHit, recoveredAfterError, queryAnswerFidelity,
                boundaryConfusion, nextActionFollowed, approvalViolation, interactionTrace);
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
        double nextActionFollowRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isNextActionFollowed);
        double approvalViolationRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isApprovalViolation);
        return new LLMUsabilityScorecard(suiteId, runId, taskSuccessRate, firstCorrectActionRate, invalidCallRate, averageRoundTrips,
                queryAnswerFidelity, boundaryConfusionRate, resourceHitRate, recoveryRate, nextActionFollowRate, approvalViolationRate, scenarioResults);
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
            if (!MCPInteractionActionNames.RESOURCE_READ_KIND.equals(each.getActionKind())) {
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
    
    private boolean isNextActionFollowed(final List<MCPInteractionTraceRecord> interactionTrace) {
        boolean hasActionableGuidance = false;
        for (int index = 0; index < interactionTrace.size() - 1; index++) {
            List<Map<?, ?>> actions = getActionableNextActions(interactionTrace.get(index));
            if (actions.isEmpty()) {
                continue;
            }
            hasActionableGuidance = true;
            if (!matchesAnyNextAction(actions, interactionTrace.get(index), interactionTrace.get(index + 1))) {
                return false;
            }
        }
        return !hasActionableGuidance || !interactionTrace.isEmpty();
    }
    
    private List<Map<?, ?>> getActionableNextActions(final MCPInteractionTraceRecord interactionTraceRecord) {
        Object nextActions = interactionTraceRecord.getStructuredContent().get("next_actions");
        if (!(nextActions instanceof List)) {
            return List.of();
        }
        List<Map<?, ?>> result = new LinkedList<>();
        for (Object each : (List<?>) nextActions) {
            if (each instanceof Map && isMachineAction((Map<?, ?>) each)) {
                result.add((Map<?, ?>) each);
            }
        }
        return result;
    }
    
    private boolean isMachineAction(final Map<?, ?> action) {
        String actionKind = Objects.toString(action.get("action_kind"), "");
        return "read_resource".equals(actionKind) || "call_tool".equals(actionKind) || "retry_tool".equals(actionKind) || "complete_argument".equals(actionKind);
    }
    
    private boolean matchesAnyNextAction(final List<Map<?, ?>> actions, final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        for (Map<?, ?> each : actions) {
            if (matchesNextAction(each, current, next)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchesNextAction(final Map<?, ?> action, final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        String actionKind = Objects.toString(action.get("action_kind"), "");
        if ("read_resource".equals(actionKind)) {
            return MCPInteractionActionNames.RESOURCE_READ_KIND.equals(next.getActionKind())
                    && Objects.equals(action.get("target_resource"), next.getArguments().get("uri"));
        }
        if ("call_tool".equals(actionKind)) {
            return Objects.equals(action.get("target_tool"), next.getTargetName());
        }
        if ("retry_tool".equals(actionKind)) {
            String targetTool = Objects.toString(action.get("target_tool"), current.getTargetName());
            return Objects.equals(targetTool, next.getTargetName());
        }
        return "complete_argument".equals(actionKind) && MCPInteractionActionNames.COMPLETION_KIND.equals(next.getActionKind());
    }
    
    private boolean hasApprovalViolation(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            Object errorCode = each.getStructuredContent().get("error_code");
            if ("unsafe_sql_execution_attempted".equals(errorCode) || "unsafe_workflow_execution_attempted".equals(errorCode)) {
                return true;
            }
        }
        return false;
    }
}
