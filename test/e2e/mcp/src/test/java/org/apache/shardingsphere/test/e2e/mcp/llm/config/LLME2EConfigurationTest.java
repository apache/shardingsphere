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
    
    @BeforeEach
    void setUp() {
        originalRuntimeMode = System.getProperty("mcp.llm.runtime-mode");
    }
    
    @AfterEach
    void tearDown() {
        if (null == originalRuntimeMode) {
            System.clearProperty("mcp.llm.runtime-mode");
        } else {
            System.setProperty("mcp.llm.runtime-mode", originalRuntimeMode);
        }
    }
    
    @Test
    void assertLoadWithDockerRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "docker");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.DOCKER));
    }
    
    @Test
    void assertLoadWithExternalDebugRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "external-debug");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
    }
    
    @Test
    void assertLoadWithInvalidRuntimeMode() {
        System.setProperty("mcp.llm.runtime-mode", "external");
        IllegalStateException actualException = assertThrows(IllegalStateException.class, LLME2EConfiguration::load);
        assertThat(actualException.getMessage(), is("Unsupported MCP LLM runtime mode `external`."));
    }
    
    @Test
    void assertWithBaseUrl() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.EXTERNAL_DEBUG).withBaseUrl("http://127.0.0.1:11434/v1/");
        assertThat(actual.getBaseUrl(), is("http://127.0.0.1:11434/v1"));
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
    }
    
    @Test
    void assertWithReadinessTimeouts() {
        LLME2EConfiguration actual = createConfiguration(RuntimeMode.EXTERNAL_DEBUG).withReadinessTimeouts(1, 2);
        assertThat(actual.getReadyTimeoutSeconds(), is(1));
        assertThat(actual.getRequestTimeoutSeconds(), is(2));
        assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
    }
    
    private LLME2EConfiguration createConfiguration(final RuntimeMode runtimeMode) {
        return new LLME2EConfiguration("http://127.0.0.1:11434/v1", "openai-compatible", "qwen3:1.7b", "ollama", 600, 240, 10,
                Path.of("target/llm-e2e"), "run-id", runtimeMode);
    }
}
