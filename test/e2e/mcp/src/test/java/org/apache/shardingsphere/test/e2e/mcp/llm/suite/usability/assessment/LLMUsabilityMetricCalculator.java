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

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMMCPNextActions;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * LLM usability metric calculator.
 */
public final class LLMUsabilityMetricCalculator {

    private static final double FULL_SCORE = 100.0D;

    private static final double TASK_SUCCESS_WEIGHT = 30.0D;

    private static final double FIRST_CORRECT_ACTION_WEIGHT = 15.0D;

    private static final double NO_INVALID_CALL_WEIGHT = 10.0D;

    private static final double QUERY_ANSWER_FIDELITY_WEIGHT = 10.0D;

    private static final double NO_BOUNDARY_CONFUSION_WEIGHT = 10.0D;

    private static final double RESOURCE_HIT_WEIGHT = 10.0D;

    private static final double RECOVERY_WEIGHT = 5.0D;

    private static final double NEXT_ACTION_FOLLOW_WEIGHT = 5.0D;

    private static final double NO_APPROVAL_VIOLATION_WEIGHT = 5.0D;

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
        int invalidCallCount = getInvalidCallCount(interactionTrace, scenario.getExpectedRecoveryCategory());
        boolean resourceHit = hasResourceHit(scenario.getExpectedResourceUris(), interactionTrace);
        boolean expectedRecoveryObserved = hasExpectedRecoveryInteraction(interactionTrace, scenario.getExpectedRecoveryCategory());
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
        if (success && scenario.isRecoveryExpected() && !expectedRecoveryObserved) {
            success = false;
            failureType = "missing_expected_error_path";
            message = "Scenario expected one recoverable MCP error with category `" + scenario.getExpectedRecoveryCategory() + "` before final success.";
        }
        boolean recoveredAfterError = success && (!scenario.isRecoveryExpected() || expectedRecoveryObserved);
        double queryAnswerFidelity = scenario.isQueryScenario() ? success ? 1.0D : 0.0D : 0.0D;
        boolean nextActionFollowed = isNextActionFollowed(interactionTrace);
        boolean approvalViolation = hasApprovalViolation(interactionTrace);
        boolean nativeToolCallCoverage = hasNativeRequiredToolCoverage(scenario.getLlmScenario().getRequiredToolNames(), interactionTrace);
        boolean harnessRecoveryUsed = hasHarnessRecovery(interactionTrace);
        return new LLMUsabilityScenarioResult(scenario.getScenarioId(), scenario.getDimension(), scenario.getRuntimeKind(), scenario.getTags(), success, failureType, message,
                firstCorrectAction, invalidCallCount, interactionTrace.size(), resourceHit, recoveredAfterError, queryAnswerFidelity,
                boundaryConfusion, nextActionFollowed, approvalViolation, nativeToolCallCoverage, harnessRecoveryUsed, interactionTrace);
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
        double naturalTaskSuccessRate = getTaggedSuccessRate(scenarioResults, LLMUsabilityScenario.NATURAL_TASK_TAG);
        double protocolContractSuccessRate = getTaggedSuccessRate(scenarioResults, LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG);
        double firstCorrectActionRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isFirstCorrectAction);
        double invalidCallRate = getInvalidCallRate(scenarioResults);
        double averageRoundTrips = getAverageRoundTrips(scenarioResults);
        double queryAnswerFidelity = getQueryAnswerFidelity(scenarioResults);
        double boundaryConfusionRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isBoundaryConfusion);
        double resourceHitRate = getResourceHitRate(scenarioResults);
        double recoveryRate = getRecoveryRate(scenarioResults);
        double nextActionFollowRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isNextActionFollowed);
        double approvalViolationRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isApprovalViolation);
        double nativeToolCallRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isNativeToolCallCoverage);
        double harnessRecoveryRate = calculateRate(scenarioResults, LLMUsabilityScenarioResult::isHarnessRecoveryUsed);
        double overallScore = calculateOverallScore(taskSuccessRate, firstCorrectActionRate, invalidCallRate, queryAnswerFidelity,
                boundaryConfusionRate, resourceHitRate, recoveryRate, nextActionFollowRate, approvalViolationRate);
        return new LLMUsabilityScorecard(suiteId, runId, overallScore,
                isFullScore(overallScore, nativeToolCallRate, harnessRecoveryRate, naturalTaskSuccessRate, protocolContractSuccessRate), taskSuccessRate,
                naturalTaskSuccessRate, protocolContractSuccessRate, firstCorrectActionRate, invalidCallRate, averageRoundTrips, queryAnswerFidelity, boundaryConfusionRate,
                resourceHitRate, recoveryRate, nextActionFollowRate, approvalViolationRate, nativeToolCallRate, harnessRecoveryRate, scenarioResults);
    }

    private double calculateOverallScore(final double taskSuccessRate, final double firstCorrectActionRate, final double invalidCallRate,
                                         final double queryAnswerFidelity, final double boundaryConfusionRate, final double resourceHitRate,
                                         final double recoveryRate, final double nextActionFollowRate, final double approvalViolationRate) {
        return TASK_SUCCESS_WEIGHT * taskSuccessRate
                + FIRST_CORRECT_ACTION_WEIGHT * firstCorrectActionRate
                + NO_INVALID_CALL_WEIGHT * invertRate(invalidCallRate)
                + QUERY_ANSWER_FIDELITY_WEIGHT * queryAnswerFidelity
                + NO_BOUNDARY_CONFUSION_WEIGHT * invertRate(boundaryConfusionRate)
                + RESOURCE_HIT_WEIGHT * resourceHitRate
                + RECOVERY_WEIGHT * recoveryRate
                + NEXT_ACTION_FOLLOW_WEIGHT * nextActionFollowRate
                + NO_APPROVAL_VIOLATION_WEIGHT * invertRate(approvalViolationRate);
    }

    private double invertRate(final double rate) {
        return Math.max(0.0D, 1.0D - rate);
    }

    private boolean isFullScore(final double overallScore, final double nativeToolCallRate, final double harnessRecoveryRate,
                                final double naturalTaskSuccessRate, final double protocolContractSuccessRate) {
        return 0 == Double.compare(FULL_SCORE, overallScore) && 0 == Double.compare(1.0D, nativeToolCallRate) && 0 == Double.compare(0.0D, harnessRecoveryRate)
                && 0 == Double.compare(1.0D, naturalTaskSuccessRate) && 0 == Double.compare(1.0D, protocolContractSuccessRate);
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

    private double getTaggedSuccessRate(final List<LLMUsabilityScenarioResult> scenarioResults, final String tag) {
        List<LLMUsabilityScenarioResult> taggedResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (each.getTags().contains(tag)) {
                taggedResults.add(each);
            }
        }
        return taggedResults.isEmpty() ? 1.0D : calculateRate(taggedResults, LLMUsabilityScenarioResult::isSuccess);
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
            return 1.0D;
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
        return resourceRequiredResults.isEmpty() ? 1.0D : calculateRate(resourceRequiredResults, LLMUsabilityScenarioResult::isResourceHit);
    }

    private double getRecoveryRate(final List<LLMUsabilityScenarioResult> scenarioResults) {
        List<LLMUsabilityScenarioResult> recoveryResults = new LinkedList<>();
        for (LLMUsabilityScenarioResult each : scenarioResults) {
            if (LLMUsabilityDimension.RECOVERY == each.getDimension()) {
                recoveryResults.add(each);
            }
        }
        return recoveryResults.isEmpty() ? 1.0D : calculateRate(recoveryResults, LLMUsabilityScenarioResult::isRecoveredAfterError);
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

    private int getInvalidCallCount(final List<MCPInteractionTraceRecord> interactionTrace, final String expectedRecoveryCategory) {
        int result = 0;
        boolean expectedRecoverySignalObserved = false;
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!isErrorInteraction(each)) {
                continue;
            }
            if (!expectedRecoverySignalObserved && isExpectedRecoveryInteraction(each, expectedRecoveryCategory)) {
                expectedRecoverySignalObserved = true;
            } else {
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

    private boolean hasNativeRequiredToolCoverage(final List<String> requiredToolNames, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (String each : requiredToolNames) {
            if (!hasNativeRequiredTool(each, interactionTrace)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasNativeRequiredTool(final String requiredToolName, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (each.isValid() && requiredToolName.equals(each.getTargetName()) && isNativeToolOrigin(each.getActionOrigin())) {
                return true;
            }
        }
        return false;
    }

    private boolean isNativeToolOrigin(final String actionOrigin) {
        return MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN.equals(actionOrigin)
                || MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN.equals(actionOrigin)
                || MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN.equals(actionOrigin);
    }

    private boolean hasHarnessRecovery(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (isHarnessOrigin(each.getActionOrigin())) {
                return true;
            }
        }
        return false;
    }

    private boolean isHarnessOrigin(final String actionOrigin) {
        return MCPInteractionTraceRecord.HARNESS_TEXT_RECOVERY_ORIGIN.equals(actionOrigin);
    }

    private boolean hasExpectedRecoveryInteraction(final List<MCPInteractionTraceRecord> interactionTrace, final String expectedRecoveryCategory) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (isExpectedRecoveryInteraction(each, expectedRecoveryCategory)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExpectedRecoveryInteraction(final MCPInteractionTraceRecord interactionTraceRecord, final String expectedRecoveryCategory) {
        return null != expectedRecoveryCategory && !expectedRecoveryCategory.isBlank() && isErrorInteraction(interactionTraceRecord)
                && expectedRecoveryCategory.equals(getRecoveryCategory(interactionTraceRecord));
    }

    private String getRecoveryCategory(final MCPInteractionTraceRecord interactionTraceRecord) {
        Map<String, Object> structuredContent = interactionTraceRecord.getStructuredContent();
        if (structuredContent.containsKey("recovery_category")) {
            return Objects.toString(structuredContent.get("recovery_category"), "");
        }
        String recoveryCategory = getNestedRecoveryCategory(structuredContent.get("recovery"));
        if (!recoveryCategory.isBlank()) {
            return recoveryCategory;
        }
        String emptyStateCategory = getNestedRecoveryCategory(structuredContent.get("empty_state"));
        if (!emptyStateCategory.isBlank()) {
            return emptyStateCategory;
        }
        if (structuredContent.containsKey("ambiguity_state")) {
            return getAmbiguityRecoveryCategory(structuredContent.get("ambiguity_state"));
        }
        return Objects.toString(structuredContent.get("error_code"), "");
    }

    private String getNestedRecoveryCategory(final Object value) {
        if (!(value instanceof Map)) {
            return "";
        }
        Map<?, ?> map = (Map<?, ?>) value;
        if (map.containsKey("recovery_category")) {
            return Objects.toString(map.get("recovery_category"), "");
        }
        if (map.containsKey("category")) {
            return Objects.toString(map.get("category"), "");
        }
        return Objects.toString(map.get("state"), "");
    }

    private String getAmbiguityRecoveryCategory(final Object value) {
        if (!(value instanceof Map)) {
            return "ambiguous";
        }
        Map<?, ?> map = (Map<?, ?>) value;
        if (map.containsKey("recovery_category")) {
            return Objects.toString(map.get("recovery_category"), "");
        }
        return map.containsKey("category") ? Objects.toString(map.get("category"), "") : "ambiguous";
    }

    private boolean isErrorInteraction(final MCPInteractionTraceRecord interactionTraceRecord) {
        if (!interactionTraceRecord.isValid() || interactionTraceRecord.getStructuredContent().containsKey("error_code")) {
            return true;
        }
        return isRecoverableEmptyState(interactionTraceRecord);
    }

    private boolean isRecoverableEmptyState(final MCPInteractionTraceRecord interactionTraceRecord) {
        return Boolean.FALSE.equals(interactionTraceRecord.getStructuredContent().get("found"))
                || interactionTraceRecord.getStructuredContent().containsKey("empty_state")
                || interactionTraceRecord.getStructuredContent().containsKey("ambiguity_state");
    }

    private boolean isNextActionFollowed(final List<MCPInteractionTraceRecord> interactionTrace) {
        boolean hasActionableGuidance = false;
        for (int index = 0; index < interactionTrace.size() - 1; index++) {
            List<Map<?, ?>> actions = getImmediateMachineNextActions(interactionTrace.get(index));
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

    private List<Map<?, ?>> getImmediateMachineNextActions(final MCPInteractionTraceRecord interactionTraceRecord) {
        List<Map<?, ?>> result = new LinkedList<>();
        for (Map<?, ?> each : LLMMCPNextActions.getNextActions(interactionTraceRecord.getStructuredContent())) {
            if (isMachineAction(each)) {
                result.add(each);
            }
        }
        return result;
    }

    private boolean isMachineAction(final Map<?, ?> action) {
        String type = Objects.toString(action.get("type"), "");
        return "resource_read".equals(type) || "tool_call".equals(type) || "completion".equals(type);
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
        String type = Objects.toString(action.get("type"), "");
        if ("resource_read".equals(type)) {
            return MCPInteractionActionNames.RESOURCE_READ_KIND.equals(next.getActionKind())
                    && (Objects.equals(action.get("resource_uri"), next.getArguments().get("uri"))
                            || isRecoverableResourceCorrection(current, next));
        }
        if ("tool_call".equals(type)) {
            String targetTool = Objects.toString(action.get("tool_name"), current.getTargetName());
            return Objects.equals(targetTool, next.getTargetName());
        }
        return "completion".equals(type) && MCPInteractionActionNames.COMPLETION_KIND.equals(next.getActionKind());
    }

    private boolean isRecoverableResourceCorrection(final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        return isRecoverableEmptyState(current) && Boolean.TRUE.equals(next.getStructuredContent().get("found"));
    }

    private boolean hasApprovalViolation(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = 0; index < interactionTrace.size(); index++) {
            if (hasUnsafeApprovalError(interactionTrace.get(index))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasUnsafeApprovalError(final MCPInteractionTraceRecord interactionTraceRecord) {
        Object errorCode = interactionTraceRecord.getStructuredContent().get("error_code");
        return "unsafe_sql_execution_attempted".equals(errorCode) || "unsafe_workflow_execution_attempted".equals(errorCode);
    }
}
