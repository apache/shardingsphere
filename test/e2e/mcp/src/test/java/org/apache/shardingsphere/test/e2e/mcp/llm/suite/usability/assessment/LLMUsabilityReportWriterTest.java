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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMUsabilityReportWriterTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertWriteScorecardSummary() throws IOException {
        LLMUsabilityScenarioResult scenarioResult = new LLMUsabilityScenarioResult("scenario-1", LLMUsabilityDimension.TOOL, "h2",
                List.of("natural-task"), false, "wrong_tool", "Expected database_gateway_execute_query.", false, 1, 3, false, false, 0.0D, true,
                false, false, false, true, List.of());
        LLMUsabilityScorecard scorecard = new LLMUsabilityScorecard("suite-1", "run-1", 75.0D, false, 0.0D, 0.0D, 1.0D, 0.0D,
                0.33D, 3.0D, 0.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, List.of(scenarioResult));
        new LLMUsabilityReportWriter().writeScorecard(tempDir, scorecard);
        assertThat(Files.readString(tempDir.resolve("summary.md")), is(createExpectedSummary()));
    }
    
    private String createExpectedSummary() {
        String lineSeparator = System.lineSeparator();
        return "# LLM Usability Scorecard" + lineSeparator + lineSeparator
                + "- suiteId: suite-1" + lineSeparator
                + "- runId: run-1" + lineSeparator
                + "- overallScore: 75.0" + lineSeparator
                + "- fullScore: false" + lineSeparator
                + "- taskSuccessRate: 0.0" + lineSeparator
                + "- naturalTaskSuccessRate: 0.0" + lineSeparator
                + "- protocolContractSuccessRate: 1.0" + lineSeparator
                + "- firstCorrectActionRate: 0.0" + lineSeparator
                + "- invalidCallRate: 0.33" + lineSeparator
                + "- averageRoundTrips: 3.0" + lineSeparator
                + "- queryAnswerFidelity: 0.0" + lineSeparator
                + "- boundaryConfusionRate: 1.0" + lineSeparator
                + "- resourceHitRate: 0.0" + lineSeparator
                + "- recoveryRate: 0.0" + lineSeparator
                + "- nextActionFollowRate: 0.0" + lineSeparator
                + "- approvalViolationRate: 0.0" + lineSeparator
                + "- nativeToolCallRate: 0.0" + lineSeparator
                + "- harnessRecoveryRate: 1.0" + lineSeparator
                + lineSeparator
                + "## Scenario Results" + lineSeparator
                + "- scenario-1: FAIL failureType=wrong_tool, roundTrips=3, invalidCalls=1, nativeToolCall=false, harnessRecovery=true, message=Expected database_gateway_execute_query."
                + lineSeparator;
    }
}
