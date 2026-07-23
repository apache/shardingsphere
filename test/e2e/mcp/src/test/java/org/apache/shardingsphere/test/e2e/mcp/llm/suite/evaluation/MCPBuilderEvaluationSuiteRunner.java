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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.evaluation;

import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousMCPBuilderEvaluationRunner;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousMCPBuilderEvaluationRunner.EvaluationResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.MCPBuilderEvaluationArtifactWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.MCPBuilderEvaluationCatalog.EvaluationCase;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.MCPBuilderEvaluationCatalog.EvaluationSuite;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MCPBuilderEvaluationSuiteRunner {
    
    private static final int MAX_EVALUATION_TURNS = 8;
    
    private static final Pattern UNREDACTED_SECRET_PATTERN = Pattern.compile(
            "(?i)(\"(?:api[_-]?key|token|password|authorization|secret)\"\\s*:\\s*\")(?!<redacted>\")([^\"]+)(\")|(Bearer\\s+)(?!<redacted>)[A-Za-z0-9._~+/=-]+");
    
    private static final List<String> REQUIRED_ARTIFACT_FILES = List.of(
            "run-context.json", "system-prompt.md", "question.txt", "expected-answer.txt", "answer.txt", "raw-model-output.txt", "available-tools.json",
            "interaction-trace.json", "mcp-runtime.log", "assertion-report.json");
    
    private final LLME2EConfiguration configuration;
    
    private final Map<String, Object> runtimeEvidence;
    
    private final LLMChatModelClient modelClient;
    
    private final MCPBuilderEvaluationArtifactWriter artifactWriter = new MCPBuilderEvaluationArtifactWriter();
    
    MCPBuilderEvaluationSuiteRunner(final LLME2EConfiguration configuration, final Map<String, Object> runtimeEvidence) {
        this.configuration = configuration;
        this.runtimeEvidence = runtimeEvidence;
        modelClient = new LLMChatModelClient(configuration, HttpClient.newHttpClient());
    }
    
    void assertFullScore(final EvaluationSuite evaluationSuite, final InteractionClientFactory interactionClientFactory) throws IOException {
        assertTrue(Boolean.TRUE.equals(runtimeEvidence.get("scoreClosing")), "MCP Builder evaluation score requires Docker-owned score-closing runtime evidence.");
        List<EvaluationResult> results = new LinkedList<>();
        List<Path> artifactDirectories = new LinkedList<>();
        for (EvaluationCase each : evaluationSuite.cases()) {
            EvaluationResult result = new AutonomousMCPBuilderEvaluationRunner(
                    MAX_EVALUATION_TURNS,
                    modelClient,
                    interactionClientFactory.create(),
                    configuration.getModelName()).run(each);
            Path artifactDirectory = configuration.createArtifactDirectory("mcp-builder-evaluation/" + each.id());
            artifactWriter.write(artifactDirectory, result, runtimeEvidence);
            results.add(result);
            artifactDirectories.add(artifactDirectory);
        }
        Path scorecardFile = configuration.createArtifactDirectory("mcp-builder-evaluation").resolve("scorecard.json");
        artifactWriter.writeScorecard(scorecardFile, results);
        assertArtifactsSecure(artifactDirectories, scorecardFile);
        assertAllPassed(results, evaluationSuite.cases().size());
    }
    
    private void assertArtifactsSecure(final List<Path> artifactDirectories, final Path scorecardFile) throws IOException {
        for (Path each : artifactDirectories) {
            for (String fileName : REQUIRED_ARTIFACT_FILES) {
                assertTrue(Files.isRegularFile(each.resolve(fileName)), () -> "Missing MCP Builder evaluation artifact: " + each.resolve(fileName));
            }
        }
        assertTrue(Files.isRegularFile(scorecardFile), () -> "Missing MCP Builder evaluation scorecard: " + scorecardFile);
        assertNoSecretLeak(scorecardFile.getParent());
    }
    
    private void assertNoSecretLeak(final Path artifactDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(artifactDirectory)) {
            for (Path each : paths.filter(Files::isRegularFile).toList()) {
                String content = Files.readString(each);
                assertFalse(UNREDACTED_SECRET_PATTERN.matcher(content).find(), () -> "Unredacted secret-like value in MCP Builder evaluation artifact: " + each);
                assertFalse(content.contains(configuration.getApiKey()), () -> "Known model API key leaked into MCP Builder evaluation artifact: " + each);
            }
        }
    }
    
    private void assertAllPassed(final List<EvaluationResult> results, final int expectedCaseCount) {
        String failureSummary = createFailureSummary(results);
        assertFalse(results.isEmpty(), "MCP Builder evaluation must execute at least one case.");
        assertTrue(results.size() == expectedCaseCount,
                () -> String.format("MCP Builder evaluation expected %d cases, but executed %d.", expectedCaseCount, results.size()));
        assertTrue(results.stream().allMatch(each -> each.assertionReport().isSuccess()), failureSummary);
    }
    
    private String createFailureSummary(final List<EvaluationResult> results) {
        StringBuilder result = new StringBuilder("MCP Builder evaluation did not reach 100 points.");
        for (EvaluationResult each : results) {
            if (!each.assertionReport().isSuccess()) {
                result.append(String.format(" [%s: %s - %s, actual=`%s`]", each.evaluationCase().id(), each.assertionReport().getFailureType(),
                        each.assertionReport().getMessage(), each.actualAnswer()));
            }
        }
        return result.toString();
    }
    
    @FunctionalInterface
    interface InteractionClientFactory {
        
        MCPInteractionClient create() throws IOException;
    }
}
