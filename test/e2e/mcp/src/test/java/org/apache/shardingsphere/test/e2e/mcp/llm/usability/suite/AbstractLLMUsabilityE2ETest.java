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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.suite;

import org.apache.shardingsphere.test.e2e.mcp.llm.AbstractLLMRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.metric.LLMUsabilityMetricCalculator;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenarioResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScorecard;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.report.LLMUsabilityReportWriter;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractLLMUsabilityE2ETest extends AbstractLLMRuntimeE2ETest {
    
    private final LLMUsabilityMetricCalculator metricCalculator = new LLMUsabilityMetricCalculator();
    
    private final LLMUsabilityReportWriter reportWriter = new LLMUsabilityReportWriter();
    
    protected final void assertUsabilitySuite(final String suiteId, final Supplier<List<LLMUsabilityScenario>> scenarioSupplier) throws IOException {
        Assumptions.assumeTrue(isLLMEnabled(),
                "Set -Dmcp.llm.e2e.enabled=true or MCP_LLM_E2E_ENABLED=true to run the LLM usability suite.");
        List<LLMUsabilityScenario> scenarios = scenarioSupplier.get();
        List<LLMUsabilityScenarioResult> scenarioResults = new ArrayList<>(scenarios.size());
        for (LLMUsabilityScenario each : scenarios) {
            scenarioResults.add(metricCalculator.evaluateScenario(each, runConversation(each.getScenarioId(), each.getLlmScenario()).artifactBundle()));
        }
        LLMUsabilityScorecard scorecard = metricCalculator.createScorecard(suiteId, getLLMConfiguration().getRunId(), scenarioResults);
        Path suiteDirectory = getLLMConfiguration().getArtifactRoot().resolve(getLLMConfiguration().getRunId()).resolve(suiteId);
        reportWriter.writeScorecard(suiteDirectory, scorecard);
        String actualFailureSummary = createFailureSummary(scorecard);
        assertThat(actualFailureSummary, scorecard.getScenarioResults().size(), is(scenarios.size()));
        assertThat(actualFailureSummary, scorecard.getTaskSuccessRate(), is(1.0D));
        if (hasResourceHitExpectation(scenarios)) {
            assertThat(actualFailureSummary, scorecard.getResourceHitRate(), is(1.0D));
        }
        if (hasRecoveryExpectation(scenarios)) {
            assertThat(actualFailureSummary, scorecard.getRecoveryRate(), is(1.0D));
        }
    }
    
    private boolean hasResourceHitExpectation(final List<LLMUsabilityScenario> scenarios) {
        for (LLMUsabilityScenario each : scenarios) {
            if (each.isResourceHitRequired()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasRecoveryExpectation(final List<LLMUsabilityScenario> scenarios) {
        for (LLMUsabilityScenario each : scenarios) {
            if (each.isRecoveryExpected()) {
                return true;
            }
        }
        return false;
    }
    
    private String createFailureSummary(final LLMUsabilityScorecard scorecard) {
        StringBuilder result = new StringBuilder("LLM usability suite did not meet the baseline.");
        result.append(" taskSuccessRate=").append(scorecard.getTaskSuccessRate());
        result.append(", resourceHitRate=").append(scorecard.getResourceHitRate());
        result.append(", recoveryRate=").append(scorecard.getRecoveryRate());
        for (LLMUsabilityScenarioResult each : scorecard.getScenarioResults()) {
            if (each.isSuccess()) {
                continue;
            }
            result.append(" [");
            result.append(each.getScenarioId());
            result.append(": ");
            result.append(each.getFailureType());
            result.append(" - ");
            result.append(each.getMessage());
            result.append(']');
        }
        return result.toString();
    }
}
