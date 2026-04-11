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

import org.apache.shardingsphere.test.e2e.mcp.llm.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.LLME2EArtifactWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.LLMMCPConversationRunner;
import org.apache.shardingsphere.test.e2e.mcp.runtime.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.runtime.production.AbstractProductionRuntimeE2ETest;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractLLMUsabilityE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private final LLME2EConfiguration llmConfiguration = LLME2EConfiguration.load();
    
    private final LLME2EArtifactWriter artifactWriter = new LLME2EArtifactWriter();
    
    private final LLMUsabilityMetricCalculator metricCalculator = new LLMUsabilityMetricCalculator();
    
    private final LLMUsabilityReportWriter reportWriter = new LLMUsabilityReportWriter();
    
    protected final void assertAdvisoryUsabilitySuite(final String suiteId, final Supplier<List<LLMUsabilityScenario>> scenarioSupplier) throws IOException {
        Assumptions.assumeTrue(llmConfiguration.enabled(),
                "Set -Dmcp.llm.e2e.enabled=true or MCP_LLM_E2E_ENABLED=true to run the LLM usability suite.");
        launchProductionRuntime();
        List<LLMUsabilityScenario> scenarios = scenarioSupplier.get();
        List<LLMUsabilityScenarioResult> scenarioResults = new LinkedList<>();
        for (LLMUsabilityScenario each : scenarios) {
            LLME2EArtifactBundle artifactBundle = new LLMMCPConversationRunner(llmConfiguration.maxTurns(),
                    new LLMChatModelClient(llmConfiguration, HttpClient.newHttpClient()),
                    new MCPHttpInteractionClient(getEndpointUri(), createHttpClient())).run(each.llmScenario());
            Path artifactDirectory = llmConfiguration.createArtifactDirectory(each.scenarioId());
            artifactWriter.write(artifactDirectory, artifactBundle);
            scenarioResults.add(metricCalculator.evaluateScenario(each, artifactBundle));
        }
        LLMUsabilityScorecard scorecard = metricCalculator.createScorecard(suiteId, llmConfiguration.runId(), scenarioResults);
        Path suiteDirectory = llmConfiguration.artifactRoot().resolve(llmConfiguration.runId()).resolve(suiteId);
        reportWriter.writeScorecard(suiteDirectory, scorecard);
        assertThat(scorecard.scenarioResults().size(), is(scenarios.size()));
        assertTrue(scorecard.taskSuccessRate() >= 0.0D);
    }
}
