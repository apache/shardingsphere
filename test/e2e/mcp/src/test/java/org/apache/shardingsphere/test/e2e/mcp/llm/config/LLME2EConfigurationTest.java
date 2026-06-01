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
import static org.junit.jupiter.api.Assertions.assertThrows;

class LLME2EConfigurationTest {
    
    private String originalRuntimeMode;
    
    private String originalModel;
    
    private String originalApiKey;
    
    private String originalServerImage;
    
    private String originalBaseServerImageDigest;
    
    private String originalArchitecture;
    
    @BeforeEach
    void setUp() {
        originalRuntimeMode = System.getProperty("mcp.llm.runtime-mode");
        originalModel = System.getProperty("mcp.llm.model");
        originalApiKey = System.getProperty("mcp.llm.api-key");
        originalServerImage = System.getProperty("mcp.llm.server-image");
        originalBaseServerImageDigest = System.getProperty("mcp.llm.base-server-image-digest");
        originalArchitecture = System.getProperty("os.arch");
        System.clearProperty("mcp.llm.model");
        System.clearProperty("mcp.llm.api-key");
        System.clearProperty("mcp.llm.server-image");
        System.clearProperty("mcp.llm.base-server-image-digest");
    }
    
    @AfterEach
    void tearDown() {
        restoreProperty("mcp.llm.runtime-mode", originalRuntimeMode);
        restoreProperty("mcp.llm.model", originalModel);
        restoreProperty("mcp.llm.api-key", originalApiKey);
        restoreProperty("mcp.llm.server-image", originalServerImage);
        restoreProperty("mcp.llm.base-server-image-digest", originalBaseServerImageDigest);
        restoreProperty("os.arch", originalArchitecture);
    }
    
    @Test
    void assertLoadWithDockerRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "docker");
        System.setProperty("os.arch", "arm64");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.DOCKER));
        assertThat(actual.getBaseUrl(), is("http://127.0.0.1:8080/v1"));
        assertThat(actual.getModelName(), is("ggml-org/Qwen3-1.7B-GGUF:Q4_K_M"));
        assertThat(actual.getApiKey(), is("mcp-llm-score"));
        assertThat(actual.getServerImage(), is("apache/shardingsphere-mcp-llm-runtime:local"));
        assertThat(actual.getBaseServerImageDigest(), is("sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca"));
    }
    
    @Test
    void assertLoadWithExternalDebugRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "external-debug");
        System.setProperty("os.arch", "riscv64");
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
        System.setProperty("mcp.llm.base-server-image-digest", "sha256:foo");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getServerImage(), is("foo/mcp-llm-runtime:bar"));
        assertThat(actual.getBaseServerImageDigest(), is("sha256:foo"));
    }
    
    @Test
    void assertLoadWithUnsupportedArchitecture() {
        System.setProperty("mcp.llm.runtime-mode", "docker");
        System.setProperty("os.arch", "riscv64");
        IllegalStateException actualException = assertThrows(IllegalStateException.class, LLME2EConfiguration::load);
        assertThat(actualException.getMessage(), is("Unsupported local architecture for MCP LLM Docker score mode: riscv64"));
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
    }
    
    @Test
    void assertWithReadinessTimeouts() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.EXTERNAL_DEBUG).withReadinessTimeouts(1, 2);
        assertThat(actual.getReadyTimeoutSeconds(), is(1));
        assertThat(actual.getRequestTimeoutSeconds(), is(2));
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
    }
    
    private LLME2EConfiguration createConfiguration(final RuntimeMode runtimeMode) {
        return new LLME2EConfiguration("http://127.0.0.1:8080/v1", "openai-compatible", "ggml-org/Qwen3-1.7B-GGUF:Q4_K_M", "mcp-llm-score", 600, 240, 10,
                Path.of("target/llm-e2e"), "run-id", runtimeMode, "apache/shardingsphere-mcp-llm-runtime:local",
                "sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca");
    }
    
    private void restoreProperty(final String name, final String value) {
        if (null == value) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
