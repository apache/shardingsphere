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

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLME2EArtifactWriterTest {
    
    private static final String OLLAMA_IMAGE_DIGEST = "sha256:fcaa568338a6b0993c82f259a5072f46814d6de276cf3dea5b91e281b7f9d149";
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertWriteRedactsSecretsAndRecordsRunContext() throws IOException {
        LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle("scenario-id", "system", "user",
                "openai-compatible", "qwen3:1.7b", Map.of("descriptorCatalog", "abc123"),
                "{\"api_key\":\"secret-value\"}", List.of("{\"token\":\"raw-secret\"}"), List.of(),
                List.of("Authorization: Bearer runtime-secret"), LLME2EAssertionReport.failure("boom", "failed"));
        new LLME2EArtifactWriter().write(tempDir, artifactBundle, Map.of(
                "runtimeMode", "docker",
                "dockerOwned", true,
                "imageName", "ollama/ollama:0.23.1",
                "imageDigest", OLLAMA_IMAGE_DIGEST));
        final Map<String, Object> runContext = JsonUtils.fromJsonString(Files.readString(tempDir.resolve("run-context.json")), new TypeReference<>() {
        });
        final String rawModelOutput = Files.readString(tempDir.resolve("raw-model-output.txt"));
        final String runtimeLog = Files.readString(tempDir.resolve("mcp-runtime.log"));
        final String finalAnswer = Files.readString(tempDir.resolve("final-answer.json"));
        assertThat(runContext.get("modelName"), is("qwen3:1.7b"));
        assertThat(castToMap(runContext.get("capabilityFingerprints")).get("descriptorCatalog"), is("abc123"));
        assertThat(castToMap(runContext.get("runtime")).get("runtimeMode"), is("docker"));
        assertTrue((boolean) castToMap(runContext.get("runtime")).get("dockerOwned"));
        assertThat(castToMap(runContext.get("runtime")).get("imageName"), is("ollama/ollama:0.23.1"));
        assertThat(castToMap(runContext.get("runtime")).get("imageDigest"), is(OLLAMA_IMAGE_DIGEST));
        assertThat(rawModelOutput, not(containsString("raw-secret")));
        assertThat(runtimeLog, not(containsString("runtime-secret")));
        assertThat(finalAnswer, not(containsString("secret-value")));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
}
