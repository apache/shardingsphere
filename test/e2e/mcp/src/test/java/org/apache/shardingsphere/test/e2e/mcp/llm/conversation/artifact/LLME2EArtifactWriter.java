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
import java.util.Map;
import java.util.regex.Pattern;

/**
 * LLM E2E artifact writer.
 */
public final class LLME2EArtifactWriter {
    
    private static final Pattern JSON_SECRET_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"(?:api[_-]?key|token|password|authorization|secret)\"\\s*:\\s*\")([^\"]+)(\")");
    
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+/=-]+");
    
    /**
     * Write.
     *
     * @param artifactDirectory artifact directory
     * @param artifactBundle artifact bundle
     * @throws IOException IO exception
     */
    public void write(final Path artifactDirectory, final LLME2EArtifactBundle artifactBundle) throws IOException {
        Files.createDirectories(artifactDirectory);
        Files.writeString(artifactDirectory.resolve("run-context.json"), redact(JsonUtils.toJsonString(createRunContext(artifactBundle))));
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
    
    private Map<String, Object> createRunContext(final LLME2EArtifactBundle artifactBundle) {
        return Map.of(
                "scenarioId", artifactBundle.getScenarioId(),
                "modelProvider", artifactBundle.getModelProvider(),
                "modelName", artifactBundle.getModelName(),
                "capabilityFingerprints", artifactBundle.getCapabilityFingerprints(),
                "failureType", artifactBundle.getAssertionReport().getFailureType());
    }
    
    private String redact(final String value) {
        String result = JSON_SECRET_FIELD_PATTERN.matcher(value).replaceAll("$1<redacted>$3");
        return BEARER_TOKEN_PATTERN.matcher(result).replaceAll("$1<redacted>");
    }
}
