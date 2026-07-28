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

/**
 * LLM E2E artifact writer.
 */
public final class LLME2EArtifactWriter {
    
    private final LLME2EArtifactRedactor redactor = new LLME2EArtifactRedactor();
    
    private final LLME2ERuntimeEvidenceValidator runtimeEvidenceValidator = new LLME2ERuntimeEvidenceValidator();
    
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
        Files.writeString(artifactDirectory.resolve("run-context.json"), redactor.redact(JsonUtils.toJsonString(createRunContext(artifactBundle, runtimeEvidence))));
        Files.writeString(artifactDirectory.resolve("system-prompt.md"), redactor.redact(artifactBundle.getSystemPrompt()));
        Files.writeString(artifactDirectory.resolve("user-prompt.md"), redactor.redact(artifactBundle.getUserPrompt()));
        Files.writeString(artifactDirectory.resolve("raw-model-output.txt"), redactor.redact(String.join(System.lineSeparator() + System.lineSeparator(), artifactBundle.getRawModelOutputs())));
        Files.writeString(artifactDirectory.resolve("interaction-trace.json"), redactor.redact(JsonUtils.toJsonString(artifactBundle.getInteractionTrace())));
        Files.writeString(artifactDirectory.resolve("assertion-report.json"), redactor.redact(JsonUtils.toJsonString(artifactBundle.getAssertionReport())));
        Files.writeString(artifactDirectory.resolve("mcp-runtime.log"), redactor.redact(String.join(System.lineSeparator(), artifactBundle.getMcpRuntimeLogLines())));
        if (null != artifactBundle.getFinalAnswerJson() && !artifactBundle.getFinalAnswerJson().isEmpty()) {
            Files.writeString(artifactDirectory.resolve("final-answer.json"), redactor.redact(artifactBundle.getFinalAnswerJson()));
        }
    }
    
    private Map<String, Object> createRunContext(final LLME2EArtifactBundle artifactBundle, final Map<String, Object> runtimeEvidence) {
        runtimeEvidenceValidator.validate(runtimeEvidence);
        return Map.of(
                "scenarioId", artifactBundle.getScenarioId(),
                "modelProvider", artifactBundle.getModelProvider(),
                "modelName", artifactBundle.getModelName(),
                "runtime", runtimeEvidence,
                "failureType", artifactBundle.getAssertionReport().getFailureType());
    }
    
}
