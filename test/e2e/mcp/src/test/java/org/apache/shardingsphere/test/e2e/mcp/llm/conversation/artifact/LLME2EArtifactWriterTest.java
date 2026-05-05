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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

class LLME2EArtifactWriterTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertWriteRedactsSecretsAndRecordsRunContext() throws IOException {
        LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle("scenario-id", "system", "user",
                "openai-compatible", "qwen3:1.7b", Map.of("descriptorCatalog", "abc123"),
                "{\"api_key\":\"secret-value\"}", List.of("{\"token\":\"raw-secret\"}"), List.of(),
                List.of("Authorization: Bearer runtime-secret"), LLME2EAssertionReport.failure("boom", "failed"));
        new LLME2EArtifactWriter().write(tempDir, artifactBundle);
        final String runContext = Files.readString(tempDir.resolve("run-context.json"));
        final String rawModelOutput = Files.readString(tempDir.resolve("raw-model-output.txt"));
        final String runtimeLog = Files.readString(tempDir.resolve("mcp-runtime.log"));
        final String finalAnswer = Files.readString(tempDir.resolve("final-answer.json"));
        assertThat(runContext, containsString("qwen3:1.7b"));
        assertThat(runContext, containsString("abc123"));
        assertThat(rawModelOutput, not(containsString("raw-secret")));
        assertThat(runtimeLog, not(containsString("runtime-secret")));
        assertThat(finalAnswer, not(containsString("secret-value")));
    }
}
