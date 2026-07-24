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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact;

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousMCPBuilderEvaluationRunner.EvaluationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * MCP Builder evaluation artifact writer.
 */
public final class MCPBuilderEvaluationArtifactWriter {
    
    private final LLME2EArtifactRedactor redactor = new LLME2EArtifactRedactor();
    
    private final LLME2ERuntimeEvidenceValidator runtimeEvidenceValidator = new LLME2ERuntimeEvidenceValidator();
    
    /**
     * Write one evaluation result.
     *
     * @param artifactDirectory artifact directory
     * @param evaluationResult evaluation result
     * @param runtimeEvidence runtime evidence
     * @throws IOException IO exception
     */
    public void write(final Path artifactDirectory, final EvaluationResult evaluationResult, final Map<String, Object> runtimeEvidence) throws IOException {
        runtimeEvidenceValidator.validate(runtimeEvidence);
        writeContent(artifactDirectory.resolve("run-context.json"), JsonUtils.toJsonString(createRunContext(evaluationResult, runtimeEvidence)));
        writeContent(artifactDirectory.resolve("system-prompt.md"), evaluationResult.systemPrompt());
        writeContent(artifactDirectory.resolve("question.txt"), evaluationResult.evaluationCase().question());
        writeContent(artifactDirectory.resolve("expected-answer.txt"), evaluationResult.evaluationCase().answer());
        writeContent(artifactDirectory.resolve("answer.txt"), evaluationResult.actualAnswer());
        writeContent(artifactDirectory.resolve("raw-model-output.txt"),
                String.join(System.lineSeparator() + System.lineSeparator(), evaluationResult.evidence().rawModelOutputs()));
        writeContent(artifactDirectory.resolve("available-tools.json"), JsonUtils.toJsonString(evaluationResult.evidence().toolDefinitions()));
        writeContent(artifactDirectory.resolve("interaction-trace.json"), JsonUtils.toJsonString(evaluationResult.evidence().interactionTrace()));
        writeContent(artifactDirectory.resolve("mcp-runtime.log"), String.join(System.lineSeparator(), evaluationResult.evidence().runtimeLogLines()));
        writeContent(artifactDirectory.resolve("assertion-report.json"), JsonUtils.toJsonString(evaluationResult.assertionReport()));
    }
    
    /**
     * Write the evaluation scorecard.
     *
     * @param scorecardFile scorecard file
     * @param evaluationResults evaluation results
     * @throws IOException IO exception
     */
    public void writeScorecard(final Path scorecardFile, final List<EvaluationResult> evaluationResults) throws IOException {
        long passed = evaluationResults.stream().filter(each -> each.assertionReport().isSuccess()).count();
        double score = evaluationResults.isEmpty() ? 0D : passed * 100D / evaluationResults.size();
        writeContent(scorecardFile, JsonUtils.toJsonString(Map.of(
                "total", evaluationResults.size(),
                "passed", passed,
                "score", score,
                "results", evaluationResults.stream().map(this::createScorecardEntry).toList())));
    }
    
    private Map<String, Object> createRunContext(final EvaluationResult evaluationResult, final Map<String, Object> runtimeEvidence) {
        return Map.of(
                "scenarioId", evaluationResult.evaluationCase().id(),
                "category", evaluationResult.evaluationCase().category(),
                "modelProvider", evaluationResult.modelProvider(),
                "modelName", evaluationResult.modelName(),
                "runtime", runtimeEvidence,
                "failureType", evaluationResult.assertionReport().getFailureType());
    }
    
    private Map<String, Object> createScorecardEntry(final EvaluationResult evaluationResult) {
        return Map.of(
                "id", evaluationResult.evaluationCase().id(),
                "category", evaluationResult.evaluationCase().category(),
                "success", evaluationResult.assertionReport().isSuccess(),
                "failureType", evaluationResult.assertionReport().getFailureType());
    }
    
    private void writeContent(final Path file, final String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, redactor.redact(content));
    }
}
