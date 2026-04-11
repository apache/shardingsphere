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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.report;

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenarioResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScorecard;

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
        result.append("- suiteId: ").append(scorecard.getSuiteId()).append(System.lineSeparator());
        result.append("- runId: ").append(scorecard.getRunId()).append(System.lineSeparator());
        result.append("- taskSuccessRate: ").append(scorecard.getTaskSuccessRate()).append(System.lineSeparator());
        result.append("- firstCorrectActionRate: ").append(scorecard.getFirstCorrectActionRate()).append(System.lineSeparator());
        result.append("- invalidCallRate: ").append(scorecard.getInvalidCallRate()).append(System.lineSeparator());
        result.append("- averageRoundTrips: ").append(scorecard.getAverageRoundTrips()).append(System.lineSeparator());
        result.append("- queryAnswerFidelity: ").append(scorecard.getQueryAnswerFidelity()).append(System.lineSeparator());
        result.append("- boundaryConfusionRate: ").append(scorecard.getBoundaryConfusionRate()).append(System.lineSeparator());
        result.append("- resourceHitRate: ").append(scorecard.getResourceHitRate()).append(System.lineSeparator());
        result.append("- recoveryRate: ").append(scorecard.getRecoveryRate()).append(System.lineSeparator());
        result.append(System.lineSeparator()).append("## Scenario Results").append(System.lineSeparator());
        for (LLMUsabilityScenarioResult each : scorecard.getScenarioResults()) {
            result.append("- ").append(each.getScenarioId()).append(": ")
                    .append(each.isSuccess() ? "PASS" : "FAIL")
                    .append(" (").append(each.getFailureType().isEmpty() ? "ok" : each.getFailureType()).append(")")
                    .append(System.lineSeparator());
        }
        return result.toString();
    }
}
