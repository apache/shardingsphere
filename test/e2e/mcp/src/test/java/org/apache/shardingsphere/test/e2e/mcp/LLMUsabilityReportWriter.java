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

import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class LLMUsabilityReportWriter {
    
    void writeScorecard(final Path outputDirectory, final LLMUsabilityScorecard scorecard) throws IOException {
        Files.createDirectories(outputDirectory);
        Files.writeString(outputDirectory.resolve("scorecard.json"), JsonUtils.toJsonString(scorecard));
        Files.writeString(outputDirectory.resolve("scenario-results.json"), JsonUtils.toJsonString(scorecard.scenarioResults()));
        Files.writeString(outputDirectory.resolve("summary.md"), createSummary(scorecard));
    }
    
    void writeComparison(final Path outputDirectory, final LLMUsabilityComparisonResult comparisonResult) throws IOException {
        Files.createDirectories(outputDirectory);
        Files.writeString(outputDirectory.resolve("comparison.json"), JsonUtils.toJsonString(comparisonResult));
    }
    
    private String createSummary(final LLMUsabilityScorecard scorecard) {
        StringBuilder result = new StringBuilder();
        result.append("# LLM Usability Scorecard").append(System.lineSeparator()).append(System.lineSeparator());
        result.append("- suiteId: ").append(scorecard.suiteId()).append(System.lineSeparator());
        result.append("- runId: ").append(scorecard.runId()).append(System.lineSeparator());
        result.append("- taskSuccessRate: ").append(scorecard.taskSuccessRate()).append(System.lineSeparator());
        result.append("- firstCorrectActionRate: ").append(scorecard.firstCorrectActionRate()).append(System.lineSeparator());
        result.append("- invalidCallRate: ").append(scorecard.invalidCallRate()).append(System.lineSeparator());
        result.append("- averageRoundTrips: ").append(scorecard.averageRoundTrips()).append(System.lineSeparator());
        result.append("- queryAnswerFidelity: ").append(scorecard.queryAnswerFidelity()).append(System.lineSeparator());
        result.append("- boundaryConfusionRate: ").append(scorecard.boundaryConfusionRate()).append(System.lineSeparator());
        result.append("- resourceHitRate: ").append(scorecard.resourceHitRate()).append(System.lineSeparator());
        result.append("- recoveryRate: ").append(scorecard.recoveryRate()).append(System.lineSeparator());
        result.append(System.lineSeparator()).append("## Scenario Results").append(System.lineSeparator());
        for (LLMUsabilityScenarioResult each : scorecard.scenarioResults()) {
            result.append("- ").append(each.scenarioId()).append(": ")
                    .append(each.success() ? "PASS" : "FAIL")
                    .append(" (").append(each.failureType().isEmpty() ? "ok" : each.failureType()).append(")")
                    .append(System.lineSeparator());
        }
        return result.toString();
    }
}
