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

package org.apache.shardingsphere.test.e2e.mcp.llm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLME2EConfigurationTest {
    
    @TempDir
    private Path tempDir;
    
    @AfterEach
    void tearDown() {
        System.clearProperty("mcp.llm.e2e.enabled");
        System.clearProperty("mcp.llm.base-url");
        System.clearProperty("mcp.llm.model");
        System.clearProperty("mcp.llm.api-key");
        System.clearProperty("mcp.llm.ready-timeout-seconds");
        System.clearProperty("mcp.llm.request-timeout-seconds");
        System.clearProperty("mcp.llm.max-turns");
        System.clearProperty("mcp.llm.artifact-root");
        System.clearProperty("mcp.llm.run-id");
    }
    
    @Test
    void assertLoad() throws IOException {
        System.setProperty("mcp.llm.e2e.enabled", "true");
        System.setProperty("mcp.llm.base-url", "http://127.0.0.1:11434/v1/");
        System.setProperty("mcp.llm.model", "mock-model");
        System.setProperty("mcp.llm.api-key", "mock-key");
        System.setProperty("mcp.llm.ready-timeout-seconds", "5");
        System.setProperty("mcp.llm.request-timeout-seconds", "7");
        System.setProperty("mcp.llm.max-turns", "9");
        System.setProperty("mcp.llm.artifact-root", tempDir.toString());
        System.setProperty("mcp.llm.run-id", "fixed-run");
        
        final LLME2EConfiguration actual = LLME2EConfiguration.load();
        
        assertTrue(actual.enabled());
        assertThat(actual.baseUrl(), is("http://127.0.0.1:11434/v1"));
        assertThat(actual.modelName(), is("mock-model"));
        assertThat(actual.apiKey(), is("mock-key"));
        assertThat(actual.readyTimeoutSeconds(), is(5));
        assertThat(actual.requestTimeoutSeconds(), is(7));
        assertThat(actual.maxTurns(), is(9));
        assertThat(actual.runId(), is("fixed-run"));
        assertThat(actual.getChatCompletionsUrl(), is("http://127.0.0.1:11434/v1/chat/completions"));
        assertThat(actual.getModelsUrl(), is("http://127.0.0.1:11434/v1/models"));
        final Path artifactDirectory = actual.createArtifactDirectory("scenario-a");
        assertNotNull(artifactDirectory);
        assertTrue(Files.isDirectory(artifactDirectory));
    }
    
    @Test
    void assertLoadWithDefaultRunIdAndArtifactRoot() {
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        assertThat(actual.artifactRoot(), is(Paths.get("target/llm-e2e")));
        assertTrue(actual.runId().matches("\\d{14}-[0-9a-f]{8}"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertLoadWithBlankStringCases")
    void assertLoadWithBlankString(final String name, final String propertyName, final String expectedValue) {
        System.setProperty(propertyName, "   ");
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        String actualValue;
        if ("mcp.llm.base-url".equals(propertyName)) {
            actualValue = actual.baseUrl();
        } else if ("mcp.llm.model".equals(propertyName)) {
            actualValue = actual.modelName();
        } else {
            actualValue = actual.apiKey();
        }
        assertThat(actualValue, is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertLoadWithInvalidIntegerCases")
    void assertLoadWithInvalidInteger(final String name, final String propertyName, final int expectedValue) {
        System.setProperty(propertyName, "invalid");
        
        LLME2EConfiguration actual = LLME2EConfiguration.load();
        
        int actualValue;
        if ("mcp.llm.ready-timeout-seconds".equals(propertyName)) {
            actualValue = actual.readyTimeoutSeconds();
        } else if ("mcp.llm.request-timeout-seconds".equals(propertyName)) {
            actualValue = actual.requestTimeoutSeconds();
        } else {
            actualValue = actual.maxTurns();
        }
        assertThat(actualValue, is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertLoadWithEnvironmentVariableCases")
    void assertLoadWithEnvironmentVariable(final String name, final String environmentName, final String environmentValue,
                                           final String expectedOutputLine) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp",
                System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                "org.apache.shardingsphere.test.e2e.mcp.llm.LLME2EConfigurationEnvProbe");
        Map<String, String> environment = processBuilder.environment();
        clearEnvironmentVariables(environment);
        environment.put(environmentName, environmentValue);
        Process process = processBuilder.start();
        String output = new String(process.getInputStream().readAllBytes())
                + new String(process.getErrorStream().readAllBytes());
        assertThat(process.waitFor(), is(0));
        assertThat(output, containsString(expectedOutputLine));
    }
    
    static Stream<Arguments> assertLoadWithEnvironmentVariableCases() {
        return Stream.of(
                Arguments.of("enabled from environment", "MCP_LLM_E2E_ENABLED", "true", "enabled=true"),
                Arguments.of("normalized base url from environment", "MCP_LLM_BASE_URL", "http://127.0.0.1:22334/v1/",
                        "baseUrl=http://127.0.0.1:22334/v1"),
                Arguments.of("max turns from environment", "MCP_LLM_MAX_TURNS", "11", "maxTurns=11"),
                Arguments.of("artifact root from environment", "MCP_LLM_ARTIFACT_ROOT", "/tmp/llm-artifacts", "artifactRoot=/tmp/llm-artifacts"),
                Arguments.of("run id from environment", "MCP_LLM_RUN_ID", "run-from-env", "runId=run-from-env"),
                Arguments.of("blank model from environment falls back to default", "MCP_LLM_MODEL", "   ", "modelName=qwen3:1.7b"),
                Arguments.of("invalid request timeout from environment falls back to default", "MCP_LLM_REQUEST_TIMEOUT_SECONDS", "invalid",
                        "requestTimeoutSeconds=240"));
    }
    
    static Stream<Arguments> assertLoadWithInvalidIntegerCases() {
        return Stream.of(
                Arguments.of("invalid ready timeout", "mcp.llm.ready-timeout-seconds", 600),
                Arguments.of("invalid request timeout", "mcp.llm.request-timeout-seconds", 240),
                Arguments.of("invalid max turns", "mcp.llm.max-turns", 6));
    }
    
    static Stream<Arguments> assertLoadWithBlankStringCases() {
        return Stream.of(
                Arguments.of("blank base url", "mcp.llm.base-url", "http://127.0.0.1:11434/v1"),
                Arguments.of("blank model", "mcp.llm.model", "qwen3:1.7b"),
                Arguments.of("blank api key", "mcp.llm.api-key", "ollama"));
    }
    
    private void clearEnvironmentVariables(final Map<String, String> environment) {
        environment.remove("MCP_LLM_E2E_ENABLED");
        environment.remove("MCP_LLM_BASE_URL");
        environment.remove("MCP_LLM_MODEL");
        environment.remove("MCP_LLM_API_KEY");
        environment.remove("MCP_LLM_READY_TIMEOUT_SECONDS");
        environment.remove("MCP_LLM_REQUEST_TIMEOUT_SECONDS");
        environment.remove("MCP_LLM_MAX_TURNS");
        environment.remove("MCP_LLM_ARTIFACT_ROOT");
        environment.remove("MCP_LLM_RUN_ID");
    }
}
