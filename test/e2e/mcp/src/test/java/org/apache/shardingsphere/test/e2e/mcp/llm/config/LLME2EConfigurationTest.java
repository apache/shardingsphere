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

package org.apache.shardingsphere.test.e2e.mcp.llm.config;

import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLME2EConfigurationTest {
    
    @TempDir
    private Path tempDir;
    
    private String originalRuntimeMode;
    
    private String originalModel;
    
    private String originalApiKey;
    
    private String originalReadyTimeoutSeconds;
    
    private String originalServerImage;
    
    private String originalBaseServerImage;
    
    private String originalBaseServerImageDigest;
    
    private String originalModelRepository;
    
    private String originalModelFileName;
    
    private String originalModelQuantization;
    
    private String originalModelRevision;
    
    private String originalModelSha256;
    
    @BeforeEach
    void setUp() {
        originalRuntimeMode = System.getProperty("mcp.llm.runtime-mode");
        originalModel = System.getProperty("mcp.llm.model");
        originalApiKey = System.getProperty("mcp.llm.api-key");
        originalReadyTimeoutSeconds = System.getProperty("mcp.llm.ready-timeout-seconds");
        originalServerImage = System.getProperty("mcp.llm.server-image");
        originalBaseServerImage = System.getProperty("mcp.llm.base-server-image");
        originalBaseServerImageDigest = System.getProperty("mcp.llm.base-server-image-digest");
        originalModelRepository = System.getProperty("mcp.llm.model-repository");
        originalModelFileName = System.getProperty("mcp.llm.model-file-name");
        originalModelQuantization = System.getProperty("mcp.llm.model-quantization");
        originalModelRevision = System.getProperty("mcp.llm.model-revision");
        originalModelSha256 = System.getProperty("mcp.llm.model-sha256");
        System.clearProperty("mcp.llm.model");
        System.clearProperty("mcp.llm.api-key");
        System.clearProperty("mcp.llm.ready-timeout-seconds");
        System.clearProperty("mcp.llm.server-image");
        System.clearProperty("mcp.llm.base-server-image");
        System.clearProperty("mcp.llm.base-server-image-digest");
        System.clearProperty("mcp.llm.model-repository");
        System.clearProperty("mcp.llm.model-file-name");
        System.clearProperty("mcp.llm.model-quantization");
        System.clearProperty("mcp.llm.model-revision");
        System.clearProperty("mcp.llm.model-sha256");
    }
    
    @AfterEach
    void tearDown() {
        restoreProperty("mcp.llm.runtime-mode", originalRuntimeMode);
        restoreProperty("mcp.llm.model", originalModel);
        restoreProperty("mcp.llm.api-key", originalApiKey);
        restoreProperty("mcp.llm.ready-timeout-seconds", originalReadyTimeoutSeconds);
        restoreProperty("mcp.llm.server-image", originalServerImage);
        restoreProperty("mcp.llm.base-server-image", originalBaseServerImage);
        restoreProperty("mcp.llm.base-server-image-digest", originalBaseServerImageDigest);
        restoreProperty("mcp.llm.model-repository", originalModelRepository);
        restoreProperty("mcp.llm.model-file-name", originalModelFileName);
        restoreProperty("mcp.llm.model-quantization", originalModelQuantization);
        restoreProperty("mcp.llm.model-revision", originalModelRevision);
        restoreProperty("mcp.llm.model-sha256", originalModelSha256);
    }
    
    @Test
    void assertLoadWithDockerRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "docker");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        Properties expectedProps = EnvironmentPropertiesLoader.loadProperties();
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.DOCKER));
        assertThat(actual.getBaseUrl(), is("http://127.0.0.1:8080/v1"));
        String expectedModelReference = expectedProps.getProperty("mcp.llm.model");
        assertThat(actual.getModelName(), is(expectedModelReference));
        assertThat(actual.getApiKey(), is("mcp-llm-score"));
        assertThat(actual.getServerImage(), is("apache/shardingsphere-mcp-llm-runtime:local"));
        assertThat(actual.getBaseServerImage(), is("ghcr.io/ggml-org/llama.cpp:server-b9191"));
        assertThat(actual.getBaseServerImageDigest(), is(expectedProps.getProperty("mcp.llm.base-server-image-digest")));
        assertThat(actual.getModelMetadata().getRepository(), is(expectedProps.getProperty("mcp.llm.model-repository")));
        assertThat(actual.getModelMetadata().getFileName(), is(expectedProps.getProperty("mcp.llm.model-file-name")));
        assertThat(actual.getModelMetadata().getQuantization(), is(expectedProps.getProperty("mcp.llm.model-quantization")));
        assertThat(actual.getModelMetadata().getRevision(), is(expectedProps.getProperty("mcp.llm.model-revision")));
        assertFalse(actual.getModelSha256().isBlank());
    }
    
    @Test
    void assertLoadWithExternalDebugRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "external-debug");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
        assertThat(actual.getBaseServerImageDigest(), is(EnvironmentPropertiesLoader.loadProperties().getProperty("mcp.llm.base-server-image-digest")));
    }
    
    @Test
    void assertLoadWithInvalidRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "external");
        IllegalStateException actualException = assertThrows(IllegalStateException.class, LLME2EConfiguration::load);
        assertThat(actualException.getMessage(), is("Unsupported MCP LLM runtime mode `external`."));
    }
    
    @Test
    void assertLoadWithMissingRequiredProperty() {
        System.setProperty("mcp.llm.model-repository", " ");
        IllegalStateException actualException = assertThrows(IllegalStateException.class, LLME2EConfiguration::load);
        assertThat(actualException.getMessage(), is("MCP LLM E2E property `mcp.llm.model-repository` is required."));
    }
    
    @Test
    void assertLoadWithInvalidIntegerProperty() {
        System.setProperty("mcp.llm.ready-timeout-seconds", "invalid-number");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getReadyTimeoutSeconds(), is(600));
    }
    
    @Test
    void assertLoadWithConfiguredServerImage() {
        System.setProperty("mcp.llm.runtime-mode", "docker");
        System.setProperty("mcp.llm.server-image", "test/mcp-llm-runtime:test");
        System.setProperty("mcp.llm.base-server-image", "test/llama.cpp:test");
        System.setProperty("mcp.llm.base-server-image-digest", "test-base-server-image-digest");
        System.setProperty("mcp.llm.model-repository", "ggml-org/Qwen3-1.7B-GGUF");
        System.setProperty("mcp.llm.model-file-name", "Qwen3-1.7B-Q4_K_M.gguf");
        System.setProperty("mcp.llm.model-quantization", "Q4_K_M");
        System.setProperty("mcp.llm.model-revision", "daeb8e2d528a760970442092f6bf1e55c3b659eb");
        System.setProperty("mcp.llm.model-sha256", "configured-model-sha256");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getServerImage(), is("test/mcp-llm-runtime:test"));
        assertThat(actual.getBaseServerImage(), is("test/llama.cpp:test"));
        assertThat(actual.getBaseServerImageDigest(), is("test-base-server-image-digest"));
        assertThat(actual.getModelMetadata().getRepository(), is("ggml-org/Qwen3-1.7B-GGUF"));
        assertThat(actual.getModelMetadata().getFileName(), is("Qwen3-1.7B-Q4_K_M.gguf"));
        assertThat(actual.getModelMetadata().getQuantization(), is("Q4_K_M"));
        assertThat(actual.getModelMetadata().getRevision(), is("daeb8e2d528a760970442092f6bf1e55c3b659eb"));
        assertThat(actual.getModelName(), is("ggml-org/Qwen3-1.7B-GGUF:Q4_K_M"));
        assertThat(actual.getModelSha256(), is("configured-model-sha256"));
    }
    
    @Test
    void assertWithModelEndpoint() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.DOCKER).withModelEndpoint("http://127.0.0.1:8081/v1/", "test-api-key");
        assertThat(actual.getBaseUrl(), is("http://127.0.0.1:8081/v1"));
        assertThat(actual.getApiKey(), is("test-api-key"));
        assertThat(actual.getServerImage(), is("apache/shardingsphere-mcp-llm-runtime:local"));
        assertThat(actual.getModelMetadata().getRevision(), is("daeb8e2d528a760970442092f6bf1e55c3b659eb"));
        assertThat(actual.getModelSha256(), is("configured-model-sha256"));
    }
    
    @Test
    void assertWithReadinessTimeouts() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.EXTERNAL_DEBUG).withReadinessTimeouts(1, 2);
        assertThat(actual.getReadyTimeoutSeconds(), is(1));
        assertThat(actual.getRequestTimeoutSeconds(), is(2));
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
        assertThat(actual.getBaseServerImage(), is("ghcr.io/ggml-org/llama.cpp:server-b9191"));
    }
    
    @Test
    void assertCreateArtifactDirectory() throws IOException {
        Path actual = createConfiguration(RuntimeMode.DOCKER, tempDir).createArtifactDirectory("scenario-id");
        assertThat(actual, is(tempDir.resolve("run-id").resolve("scenario-id")));
        assertFalse(Files.notExists(actual));
    }
    
    @Test
    void assertGetChatCompletionsUrl() {
        assertThat(createConfiguration(RuntimeMode.DOCKER).getChatCompletionsUrl(), is("http://127.0.0.1:8080/v1/chat/completions"));
    }
    
    @Test
    void assertGetModelsUrl() {
        assertThat(createConfiguration(RuntimeMode.DOCKER).getModelsUrl(), is("http://127.0.0.1:8080/v1/models"));
    }
    
    @Test
    void assertGetContainerPath() {
        assertThat(createConfiguration(RuntimeMode.DOCKER).getModelMetadata().getContainerPath(), is("/models/Qwen3-1.7B-Q4_K_M.gguf"));
    }
    
    private LLME2EConfiguration createConfiguration(final RuntimeMode runtimeMode) {
        return createConfiguration(runtimeMode, Path.of("target/llm-e2e"));
    }
    
    private LLME2EConfiguration createConfiguration(final RuntimeMode runtimeMode, final Path artifactRoot) {
        return LLME2EConfiguration.builder()
                .baseUrl("http://127.0.0.1:8080/v1")
                .modelProvider("openai-compatible")
                .modelName("ggml-org/Qwen3-1.7B-GGUF:Q4_K_M")
                .apiKey("mcp-llm-score")
                .readyTimeoutSeconds(600)
                .requestTimeoutSeconds(240)
                .maxTurns(10)
                .artifactRoot(artifactRoot)
                .runId("run-id")
                .runtimeMode(runtimeMode)
                .serverImage("apache/shardingsphere-mcp-llm-runtime:local")
                .baseServerImage("ghcr.io/ggml-org/llama.cpp:server-b9191")
                .baseServerImageDigest("")
                .modelMetadata(new LLME2EConfiguration.ModelMetadata("ggml-org/Qwen3-1.7B-GGUF", "Qwen3-1.7B-Q4_K_M.gguf", "Q4_K_M",
                        "daeb8e2d528a760970442092f6bf1e55c3b659eb", "configured-model-sha256"))
                .build();
    }
    
    private void restoreProperty(final String name, final String value) {
        if (null == value) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
