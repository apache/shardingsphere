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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability;

import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment.LLMUsabilityDimension;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMUsabilitySuiteRunnerTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertAssertCoreSuite() throws IOException {
        LLME2EConfiguration configuration = createConfiguration();
        new LLMUsabilitySuiteRunner().assertCoreSuite("core-suite", () -> List.of(createScenario(LLMUsabilityScenario.NATURAL_TASK_TAG)),
                scenario -> createConversationResult(configuration, scenario), configuration);
        assertTrue(Files.isRegularFile(tempDir.resolve("run-id").resolve("core-suite").resolve("scorecard.json")));
    }
    
    @Test
    void assertAssertExtendedSuite() throws IOException {
        LLME2EConfiguration configuration = createConfiguration();
        new LLMUsabilitySuiteRunner().assertExtendedSuite("extended-suite", () -> List.of(createScenario(LLMUsabilityScenario.PROTOCOL_CONTRACT_TAG)),
                scenario -> createConversationResult(configuration, scenario), configuration);
        assertTrue(Files.isRegularFile(tempDir.resolve("run-id").resolve("extended-suite").resolve("scorecard.json")));
    }
    
    private LLMConversationExecutor.ConversationResult createConversationResult(final LLME2EConfiguration configuration, final LLME2EScenario scenario) throws IOException {
        Path artifactDirectory = configuration.getArtifactRoot().resolve(configuration.getRunId()).resolve(scenario.getScenarioId());
        createArtifactFiles(artifactDirectory);
        List<MCPInteractionTraceRecord> trace = List.of(new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN,
                "database_gateway_execute_query", Map.of(), Map.of("result_kind", "result_set"), true, 0L));
        LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle(scenario.getScenarioId(), scenario.getSystemPrompt(), scenario.getUserPrompt(), "provider", "model",
                "{}", List.of("raw-output"), trace, List.of("runtime-log"), LLME2EAssertionReport.isSuccess("ok"));
        return new LLMConversationExecutor.ConversationResult(artifactBundle, artifactDirectory);
    }
    
    private void createArtifactFiles(final Path artifactDirectory) throws IOException {
        Files.createDirectories(artifactDirectory);
        for (String each : List.of("run-context.json", "system-prompt.md", "user-prompt.md", "raw-model-output.txt", "interaction-trace.json", "assertion-report.json", "mcp-runtime.log")) {
            Files.writeString(artifactDirectory.resolve(each), "ok");
        }
    }
    
    private LLMUsabilityScenario createScenario(final String tag) {
        LLME2EScenario llmScenario = new LLME2EScenario("scenario-" + tag, "system", "Count orders.",
                new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 2, List.of()),
                List.of("database_gateway_execute_query"), List.of("database_gateway_execute_query"));
        return new LLMUsabilityScenario("scenario-" + tag, LLMUsabilityDimension.TOOL, "runtime", List.of(tag), llmScenario,
                List.of("database_gateway_execute_query"), List.of(), false, false, "");
    }
    
    private LLME2EConfiguration createConfiguration() {
        return new LLME2EConfiguration("http://127.0.0.1:8080/v1", "provider", "model", "api-key", 1, 1, 1, tempDir, "run-id", RuntimeMode.EXTERNAL_DEBUG,
                "server-image", "base-image", "", new LLME2EConfiguration.ModelMetadata("repository", "model.gguf", "Q4", "revision", "sha256"));
    }
}
