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

package org.apache.shardingsphere.test.e2e.mcp.llm.artifact;

import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * LLM E2E artifact writer.
 */
public final class LLME2EArtifactWriter {
    
    /**
     * Write.
     *
     * @param artifactDirectory artifact directory
     * @param artifactBundle artifact bundle
     * @throws IOException IO exception
     */
    public void write(final Path artifactDirectory, final LLME2EArtifactBundle artifactBundle) throws IOException {
        Files.createDirectories(artifactDirectory);
        Files.writeString(artifactDirectory.resolve("system-prompt.md"), artifactBundle.getSystemPrompt());
        Files.writeString(artifactDirectory.resolve("user-prompt.md"), artifactBundle.getUserPrompt());
        Files.writeString(artifactDirectory.resolve("raw-model-output.txt"), String.join(System.lineSeparator() + System.lineSeparator(), artifactBundle.getRawModelOutputs()));
        Files.writeString(artifactDirectory.resolve("interaction-trace.json"), JsonUtils.toJsonString(artifactBundle.getInteractionTrace()));
        Files.writeString(artifactDirectory.resolve("assertion-report.json"), JsonUtils.toJsonString(artifactBundle.getAssertionReport()));
        Files.writeString(artifactDirectory.resolve("mcp-runtime.log"), String.join(System.lineSeparator(), artifactBundle.getMcpRuntimeLogLines()));
        if (null != artifactBundle.getFinalAnswerJson() && !artifactBundle.getFinalAnswerJson().isEmpty()) {
            Files.writeString(artifactDirectory.resolve("final-answer.json"), artifactBundle.getFinalAnswerJson());
        }
    }
}
