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

import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLME2EConfigurationTest {
    
    private String originalRuntimeMode;
    
    private String originalModel;
    
    private String originalApiKey;
    
    private String originalServerImage;
    
    private String originalBaseServerImage;
    
    private String originalBaseServerImageDigest;
    
    private String originalModelSha256;
    
    @BeforeEach
    void setUp() {
        originalRuntimeMode = System.getProperty("mcp.llm.runtime-mode");
        originalModel = System.getProperty("mcp.llm.model");
        originalApiKey = System.getProperty("mcp.llm.api-key");
        originalServerImage = System.getProperty("mcp.llm.server-image");
        originalBaseServerImage = System.getProperty("mcp.llm.base-server-image");
        originalBaseServerImageDigest = System.getProperty("mcp.llm.base-server-image-digest");
        originalModelSha256 = System.getProperty("mcp.llm.model-sha256");
        System.clearProperty("mcp.llm.model");
        System.clearProperty("mcp.llm.api-key");
        System.clearProperty("mcp.llm.server-image");
        System.clearProperty("mcp.llm.base-server-image");
        System.clearProperty("mcp.llm.base-server-image-digest");
        System.clearProperty("mcp.llm.model-sha256");
    }
    
    @AfterEach
    void tearDown() {
        restoreProperty("mcp.llm.runtime-mode", originalRuntimeMode);
        restoreProperty("mcp.llm.model", originalModel);
        restoreProperty("mcp.llm.api-key", originalApiKey);
        restoreProperty("mcp.llm.server-image", originalServerImage);
        restoreProperty("mcp.llm.base-server-image", originalBaseServerImage);
        restoreProperty("mcp.llm.base-server-image-digest", originalBaseServerImageDigest);
        restoreProperty("mcp.llm.model-sha256", originalModelSha256);
    }
    
    @Test
    void assertLoadWithDockerRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "docker");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.DOCKER));
        assertThat(actual.getBaseUrl(), is("http://127.0.0.1:8080/v1"));
        assertThat(actual.getModelName(), is("ggml-org/Qwen3-1.7B-GGUF:Q4_K_M"));
        assertThat(actual.getApiKey(), is("mcp-llm-score"));
        assertThat(actual.getServerImage(), is("apache/shardingsphere-mcp-llm-runtime:local"));
        assertThat(actual.getBaseServerImage(), is("ghcr.io/ggml-org/llama.cpp:server"));
        assertThat(actual.getBaseServerImageDigest(), is(""));
        assertFalse(actual.getModelSha256().isBlank());
    }
    
    @Test
    void assertLoadWithExternalDebugRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "external-debug");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
        assertThat(actual.getBaseServerImageDigest(), is(""));
    }
    
    @Test
    void assertLoadWithInvalidRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "external");
        IllegalStateException actualException = assertThrows(IllegalStateException.class, LLME2EConfiguration::load);
        assertThat(actualException.getMessage(), is("Unsupported MCP LLM runtime mode `external`."));
    }
    
    @Test
    void assertLoadWithConfiguredServerImage() {
        System.setProperty("mcp.llm.runtime-mode", "docker");
        System.setProperty("mcp.llm.server-image", "foo/mcp-llm-runtime:bar");
        System.setProperty("mcp.llm.base-server-image", "foo/llama.cpp:bar");
        System.setProperty("mcp.llm.base-server-image-digest", "sha256:foo");
        System.setProperty("mcp.llm.model-sha256", "bar");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getServerImage(), is("foo/mcp-llm-runtime:bar"));
        assertThat(actual.getBaseServerImage(), is("foo/llama.cpp:bar"));
        assertThat(actual.getBaseServerImageDigest(), is("sha256:foo"));
        assertThat(actual.getModelSha256(), is("bar"));
    }
    
    @Test
    void assertWithBaseUrl() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.EXTERNAL_DEBUG).withBaseUrl("http://127.0.0.1:8080/v1/");
        assertThat(actual.getBaseUrl(), is("http://127.0.0.1:8080/v1"));
        assertThat(actual.getApiKey(), is("mcp-llm-score"));
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
    }
    
    @Test
    void assertWithModelEndpoint() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.DOCKER).withModelEndpoint("http://127.0.0.1:8081/v1/", "foo-key");
        assertThat(actual.getBaseUrl(), is("http://127.0.0.1:8081/v1"));
        assertThat(actual.getApiKey(), is("foo-key"));
        assertThat(actual.getServerImage(), is("apache/shardingsphere-mcp-llm-runtime:local"));
        assertThat(actual.getModelSha256(), is("bar"));
    }
    
    @Test
    void assertWithReadinessTimeouts() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.EXTERNAL_DEBUG).withReadinessTimeouts(1, 2);
        assertThat(actual.getReadyTimeoutSeconds(), is(1));
        assertThat(actual.getRequestTimeoutSeconds(), is(2));
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
        assertThat(actual.getBaseServerImage(), is("ghcr.io/ggml-org/llama.cpp:server"));
    }
    
    private LLME2EConfiguration createConfiguration(final RuntimeMode runtimeMode) {
        return new LLME2EConfiguration("http://127.0.0.1:8080/v1", "openai-compatible", "ggml-org/Qwen3-1.7B-GGUF:Q4_K_M", "mcp-llm-score", 600, 240, 10,
                Path.of("target/llm-e2e"), "run-id", runtimeMode, "apache/shardingsphere-mcp-llm-runtime:local", "ghcr.io/ggml-org/llama.cpp:server", "",
                "bar");
    }
    
    private void restoreProperty(final String name, final String value) {
        if (null == value) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
