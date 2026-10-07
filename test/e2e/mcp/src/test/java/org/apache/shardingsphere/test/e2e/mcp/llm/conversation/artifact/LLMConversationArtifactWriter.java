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

import org.apache.shardingsphere.infra.util.json.JsonEngine;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationRunner.Result;
import org.apache.shardingsphere.test.e2e.mcp.support.artifact.MCPArtifactUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * LLM conversation artifact writer.
 */
public final class LLMConversationArtifactWriter {
    
    /**
     * Write one conversation result.
     *
     * @param artifactDirectory artifact directory
     * @param conversationResult conversation result
     * @param sensitiveValues concrete sensitive values
     * @throws IOException IO exception
     */
    public void write(final Path artifactDirectory, final Result conversationResult, final Collection<String> sensitiveValues) throws IOException {
        writeContent(artifactDirectory.resolve("run-context.json"), JsonEngine.marshal(createRunContext(conversationResult)), sensitiveValues);
        writeContent(artifactDirectory.resolve("system-prompt.md"), conversationResult.systemPrompt(), sensitiveValues);
        writeContent(artifactDirectory.resolve("question.txt"), conversationResult.scenario().question(), sensitiveValues);
        writeContent(artifactDirectory.resolve("answer.txt"), conversationResult.actualAnswer(), sensitiveValues);
        writeContent(artifactDirectory.resolve("raw-model-output.txt"),
                String.join(System.lineSeparator() + System.lineSeparator(), conversationResult.evidence().rawModelOutputs()), sensitiveValues);
        writeContent(artifactDirectory.resolve("available-tools.json"), JsonEngine.marshal(conversationResult.evidence().toolDefinitions()), sensitiveValues);
        writeContent(artifactDirectory.resolve("interaction-trace.json"), JsonEngine.marshal(conversationResult.evidence().interactionTrace()), sensitiveValues);
        writeContent(artifactDirectory.resolve("assertion-report.json"), JsonEngine.marshal(conversationResult.assertionReport()), sensitiveValues);
    }
    
    private void writeContent(final Path file, final String content, final Collection<String> sensitiveValues) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, MCPArtifactUtils.redact(content, sensitiveValues));
    }
    
    private Map<String, Object> createRunContext(final Result conversationResult) {
        return Map.of(
                "scenarioId", conversationResult.scenario().id(),
                "modelProvider", conversationResult.modelProvider(),
                "modelName", conversationResult.modelName(),
                "failureType", conversationResult.assertionReport().getFailureType());
    }
}
