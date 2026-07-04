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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * LLM E2E artifact writer.
 */
public final class LLME2EArtifactWriter {
    
    private static final Pattern JSON_SECRET_FIELD_PATTERN = Pattern.compile("(?i)(\"(?:api[_-]?key|token|password|authorization|secret)\"\\s*:\\s*\")([^\"]+)(\")");
    
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+/=-]+");
    
    private static final Pattern ENV_SECRET_ASSIGNMENT_PATTERN = Pattern.compile("(?i)((?:MCP_LLM_API_KEY|HF_TOKEN|HUGGING_FACE_HUB_TOKEN|LLAMA_API_KEY)\\s*=\\s*)\\S+");
    
    private static final List<String> REQUIRED_SCORE_EVIDENCE_KEYS = List.of(
            "runtimeMode", "dockerOwned", "provider", "serverRuntime", "serverImage", "serverImageId", "baseServerImage", "modelReference", "servedModelId",
            "modelQuantization", "modelRevision", "modelFileName", "modelSha256", "modelPackaging", "baseUrlOwnedByTest", "scoreClosing");
    
    /**
     * Write.
     *
     * @param artifactDirectory artifact directory
     * @param artifactBundle artifact bundle
     * @param runtimeEvidence runtime evidence
     * @throws IOException IO exception
     */
    public void write(final Path artifactDirectory, final LLME2EArtifactBundle artifactBundle, final Map<String, Object> runtimeEvidence) throws IOException {
        Files.createDirectories(artifactDirectory);
        Files.writeString(artifactDirectory.resolve("run-context.json"), redact(JsonUtils.toJsonString(createRunContext(artifactBundle, runtimeEvidence))));
        Files.writeString(artifactDirectory.resolve("system-prompt.md"), redact(artifactBundle.getSystemPrompt()));
        Files.writeString(artifactDirectory.resolve("user-prompt.md"), redact(artifactBundle.getUserPrompt()));
        Files.writeString(artifactDirectory.resolve("raw-model-output.txt"), redact(String.join(System.lineSeparator() + System.lineSeparator(), artifactBundle.getRawModelOutputs())));
        Files.writeString(artifactDirectory.resolve("interaction-trace.json"), redact(JsonUtils.toJsonString(artifactBundle.getInteractionTrace())));
        Files.writeString(artifactDirectory.resolve("assertion-report.json"), redact(JsonUtils.toJsonString(artifactBundle.getAssertionReport())));
        Files.writeString(artifactDirectory.resolve("mcp-runtime.log"), redact(String.join(System.lineSeparator(), artifactBundle.getMcpRuntimeLogLines())));
        if (null != artifactBundle.getFinalAnswerJson() && !artifactBundle.getFinalAnswerJson().isEmpty()) {
            Files.writeString(artifactDirectory.resolve("final-answer.json"), redact(artifactBundle.getFinalAnswerJson()));
        }
    }
    
    private Map<String, Object> createRunContext(final LLME2EArtifactBundle artifactBundle, final Map<String, Object> runtimeEvidence) {
        validateRuntimeEvidence(runtimeEvidence);
        return Map.of(
                "scenarioId", artifactBundle.getScenarioId(),
                "modelProvider", artifactBundle.getModelProvider(),
                "modelName", artifactBundle.getModelName(),
                "runtime", runtimeEvidence,
                "failureType", artifactBundle.getAssertionReport().getFailureType());
    }
    
    private void validateRuntimeEvidence(final Map<String, Object> runtimeEvidence) {
        if (!Boolean.TRUE.equals(runtimeEvidence.get("scoreClosing"))) {
            return;
        }
        for (String each : REQUIRED_SCORE_EVIDENCE_KEYS) {
            if (isMissingEvidenceValue(runtimeEvidence.get(each))) {
                throw new IllegalStateException(String.format("Missing score-closing LLM runtime evidence field `%s`.", each));
            }
        }
        if (!Boolean.TRUE.equals(runtimeEvidence.get("dockerOwned")) || !Boolean.TRUE.equals(runtimeEvidence.get("baseUrlOwnedByTest"))) {
            throw new IllegalStateException("Score-closing LLM runtime evidence must be Docker-owned and test-owned.");
        }
    }
    
    private boolean isMissingEvidenceValue(final Object value) {
        return null == value || value instanceof String && ((String) value).isBlank();
    }
    
    private String redact(final String value) {
        String result = JSON_SECRET_FIELD_PATTERN.matcher(value).replaceAll("$1<redacted>$3");
        result = BEARER_TOKEN_PATTERN.matcher(result).replaceAll("$1<redacted>");
        return ENV_SECRET_ASSIGNMENT_PATTERN.matcher(result).replaceAll("$1<redacted>");
    }
}
