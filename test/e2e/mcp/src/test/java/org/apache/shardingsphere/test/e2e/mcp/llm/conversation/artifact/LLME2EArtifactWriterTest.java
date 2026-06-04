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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLME2EArtifactWriterTest {
    
    private static final String MODEL_NAME = "ggml-org/Qwen3-1.7B-GGUF:Q4_K_M";
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertWriteRedactsSecretsAndRecordsRunContext() throws IOException {
        LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle("scenario-id", "system", "user",
                "openai-compatible", MODEL_NAME, Map.of("descriptorCatalog", "abc123"),
                "{\"api_key\":\"secret-value\"}", List.of("{\"token\":\"raw-secret\"}"), List.of(),
                List.of("Authorization: Bearer runtime-secret", "MCP_LLM_API_KEY=runtime-secret"), LLME2EAssertionReport.failure("boom", "failed"));
        new LLME2EArtifactWriter().write(tempDir, artifactBundle, createScoreClosingEvidence());
        Map<String, Object> runContext = JsonUtils.fromJsonString(Files.readString(tempDir.resolve("run-context.json")), new TypeReference<>() {
        });
        assertThat(runContext.get("modelName"), is(MODEL_NAME));
        assertThat(castToMap(runContext.get("capabilityFingerprints")).get("descriptorCatalog"), is("abc123"));
        assertThat(castToMap(runContext.get("runtime")).get("runtimeMode"), is("docker"));
        assertTrue((boolean) castToMap(runContext.get("runtime")).get("dockerOwned"));
        assertThat(castToMap(runContext.get("runtime")).get("serverRuntime"), is("llama.cpp"));
        assertThat(castToMap(runContext.get("runtime")).get("serverImage"), is("apache/shardingsphere-mcp-llm-runtime:local"));
        assertThat(castToMap(runContext.get("runtime")).get("baseServerImage"), is("ghcr.io/ggml-org/llama.cpp:server"));
        assertThat(castToMap(runContext.get("runtime")).get("modelPackaging"), is("prepackaged"));
        assertThat(Files.readString(tempDir.resolve("raw-model-output.txt")), is("{\"token\":\"<redacted>\"}"));
        assertThat(Files.readString(tempDir.resolve("mcp-runtime.log")), is("Authorization: Bearer <redacted>" + System.lineSeparator() + "MCP_LLM_API_KEY=<redacted>"));
        assertThat(Files.readString(tempDir.resolve("final-answer.json")), is("{\"api_key\":\"<redacted>\"}"));
    }
    
    @Test
    void assertWriteWithMissingScoreClosingEvidence() {
        LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle("scenario-id", "system", "user",
                "openai-compatible", MODEL_NAME, Map.of(), "{}", List.of(), List.of(), List.of(), LLME2EAssertionReport.failure("boom", "failed"));
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                () -> new LLME2EArtifactWriter().write(tempDir, artifactBundle, Map.of("scoreClosing", true)));
        assertThat(actualException.getMessage(), is("Missing score-closing LLM runtime evidence field `runtimeMode`."));
    }
    
    private Map<String, Object> createScoreClosingEvidence() {
        return Map.ofEntries(
                Map.entry("runtimeMode", "docker"),
                Map.entry("dockerOwned", true),
                Map.entry("provider", "openai-compatible"),
                Map.entry("serverRuntime", "llama.cpp"),
                Map.entry("serverImage", "apache/shardingsphere-mcp-llm-runtime:local"),
                Map.entry("serverImageId", "test-server-image-id"),
                Map.entry("baseServerImage", "ghcr.io/ggml-org/llama.cpp:server"),
                Map.entry("baseServerImageDigest", "test-base-server-image-digest"),
                Map.entry("modelReference", MODEL_NAME),
                Map.entry("servedModelId", MODEL_NAME),
                Map.entry("modelQuantization", "Q4_K_M"),
                Map.entry("modelRevision", "daeb8e2d528a760970442092f6bf1e55c3b659eb"),
                Map.entry("modelFileName", "Qwen3-1.7B-Q4_K_M.gguf"),
                Map.entry("modelSha256", "configured-model-sha256"),
                Map.entry("modelPackaging", "prepackaged"),
                Map.entry("baseUrlOwnedByTest", true),
                Map.entry("scoreClosing", true));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
}
