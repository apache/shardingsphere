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

import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * LLM usability report writer.
 */
public final class LLMUsabilityReportWriter {
    
    /**
     * Write scorecard.
     *
     * @param outputDirectory output directory
     * @param scorecard scorecard
     * @throws IOException IO exception
     */
    public void writeScorecard(final Path outputDirectory, final LLMUsabilityScorecard scorecard) throws IOException {
        Files.createDirectories(outputDirectory);
        Files.writeString(outputDirectory.resolve("scorecard.json"), JsonUtils.toJsonString(scorecard));
        Files.writeString(outputDirectory.resolve("scenario-results.json"), JsonUtils.toJsonString(scorecard.getScenarioResults()));
        Files.writeString(outputDirectory.resolve("summary.md"), createSummary(scorecard));
    }
    
    private String createSummary(final LLMUsabilityScorecard scorecard) {
        StringBuilder result = new StringBuilder();
        result.append("# LLM Usability Scorecard").append(System.lineSeparator()).append(System.lineSeparator());
        appendSummaryItem(result, "suiteId", scorecard.getSuiteId());
        appendSummaryItem(result, "runId", scorecard.getRunId());
        appendSummaryItem(result, "overallScore", scorecard.getOverallScore());
        appendSummaryItem(result, "fullScore", scorecard.isFullScore());
        appendSummaryItem(result, "taskSuccessRate", scorecard.getTaskSuccessRate());
        appendSummaryItem(result, "naturalTaskSuccessRate", scorecard.getNaturalTaskSuccessRate());
        appendSummaryItem(result, "protocolContractSuccessRate", scorecard.getProtocolContractSuccessRate());
        appendSummaryItem(result, "firstCorrectActionRate", scorecard.getFirstCorrectActionRate());
        appendSummaryItem(result, "invalidCallRate", scorecard.getInvalidCallRate());
        appendSummaryItem(result, "averageRoundTrips", scorecard.getAverageRoundTrips());
        appendSummaryItem(result, "queryAnswerFidelity", scorecard.getQueryAnswerFidelity());
        appendSummaryItem(result, "boundaryConfusionRate", scorecard.getBoundaryConfusionRate());
        appendSummaryItem(result, "resourceHitRate", scorecard.getResourceHitRate());
        appendSummaryItem(result, "recoveryRate", scorecard.getRecoveryRate());
        appendSummaryItem(result, "nextActionFollowRate", scorecard.getNextActionFollowRate());
        appendSummaryItem(result, "approvalViolationRate", scorecard.getApprovalViolationRate());
        appendSummaryItem(result, "nativeToolCallRate", scorecard.getNativeToolCallRate());
        appendSummaryItem(result, "harnessRecoveryRate", scorecard.getHarnessRecoveryRate());
        result.append(System.lineSeparator()).append("## Scenario Results").append(System.lineSeparator());
        for (LLMUsabilityScenarioResult each : scorecard.getScenarioResults()) {
            appendScenarioResult(result, each);
        }
        return result.toString();
    }
    
    private void appendSummaryItem(final StringBuilder result, final String name, final Object value) {
        result.append("- ").append(name).append(": ").append(value).append(System.lineSeparator());
    }
    
    private void appendScenarioResult(final StringBuilder result, final LLMUsabilityScenarioResult scenarioResult) {
        result.append("- ").append(scenarioResult.getScenarioId())
                .append(": ").append(scenarioResult.isSuccess() ? "PASS" : "FAIL")
                .append(" failureType=").append(getTextOrDefault(scenarioResult.getFailureType()))
                .append(", roundTrips=").append(scenarioResult.getRoundTripCount())
                .append(", invalidCalls=").append(scenarioResult.getInvalidCallCount())
                .append(", nativeToolCall=").append(scenarioResult.isNativeToolCallCoverage())
                .append(", harnessRecovery=").append(scenarioResult.isHarnessRecoveryUsed())
                .append(", message=").append(getTextOrDefault(scenarioResult.getMessage()))
                .append(System.lineSeparator());
    }
    
    private String getTextOrDefault(final String value) {
        return null == value || value.isBlank() ? "ok" : value;
    }
}
