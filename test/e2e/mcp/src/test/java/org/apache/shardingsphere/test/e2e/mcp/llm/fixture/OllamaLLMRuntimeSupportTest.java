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

package org.apache.shardingsphere.test.e2e.mcp.llm.fixture;

import com.sun.net.httpserver.HttpServer;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class OllamaLLMRuntimeSupportTest {
    
    private static final String DOCKER_REQUIRED_MESSAGE = "Docker is required to start Ollama and pull qwen3:1.7b for MCP LLM E2E.";
    
    @Test
    void assertPrepareWithExternalDebugRuntime() throws InterruptedException, IOException {
        HttpServer server = startModelServer("debug-model");
        try {
            OllamaLLMRuntimeSupport.ModelRuntime actual = OllamaLLMRuntimeSupport.prepare(
                    createConfiguration(RuntimeMode.EXTERNAL_DEBUG, "debug-model", createBaseUrl(server)));
            assertThat(actual.getRuntimeMode(), is(RuntimeMode.EXTERNAL_DEBUG));
            assertThat(actual.getImageName(), is(""));
            assertThat(actual.getConfiguration().getBaseUrl(), is(createBaseUrl(server)));
        } finally {
            server.stop(0);
        }
    }
    
    @Test
    void assertPrepareWithDockerRuntimeIgnoresExternalEndpoint() throws IOException {
        HttpServer server = startModelServer("qwen3:1.7b");
        try (
                MockedStatic<MySQLRuntimeTestSupport> mocked = mockStatic(MySQLRuntimeTestSupport.class)) {
            mocked.when(MySQLRuntimeTestSupport::isDockerAvailable).thenReturn(false);
            mocked.when(() -> MySQLRuntimeTestSupport.createDockerRequiredMessage(DOCKER_REQUIRED_MESSAGE)).thenReturn(DOCKER_REQUIRED_MESSAGE);
            IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> OllamaLLMRuntimeSupport.prepare(
                    createConfiguration(RuntimeMode.DOCKER, "qwen3:1.7b", createBaseUrl(server))));
            assertThat(actualException.getMessage(), is(DOCKER_REQUIRED_MESSAGE));
        } finally {
            server.stop(0);
        }
    }
    
    @Test
    void assertPrepareWithUnavailableExternalDebugRuntime() {
        IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> OllamaLLMRuntimeSupport.prepare(
                createConfiguration(RuntimeMode.EXTERNAL_DEBUG, "debug-model", "http://127.0.0.1:1/v1")));
        assertThat(actualException.getMessage(), is("MCP LLM external-debug mode requires a ready OpenAI-compatible endpoint."));
    }
    
    @Test
    void assertPrepareWithUnsupportedProvider() {
        LLME2EConfiguration config = new LLME2EConfiguration("http://127.0.0.1:11434/v1", "openai", "qwen3:1.7b", "ollama", 600, 240, 10,
                Path.of("target/llm-e2e"), "run-id", RuntimeMode.DOCKER);
        IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> OllamaLLMRuntimeSupport.prepare(config));
        assertThat(actualException.getMessage(), is("MCP LLM E2E requires provider openai-compatible."));
    }
    
    @Test
    void assertPrepareWithUnsupportedDockerModel() {
        IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> OllamaLLMRuntimeSupport.prepare(
                createConfiguration(RuntimeMode.DOCKER, "debug-model", "http://127.0.0.1:11434/v1")));
        assertThat(actualException.getMessage(), is("MCP LLM Docker score mode requires model qwen3:1.7b."));
    }
    
    @Test
    void assertScoreClosingImage() {
        assertThat(OllamaLLMRuntimeSupport.getScoreClosingImage(), is("ollama/ollama:0.23.1"));
    }
    
    private LLME2EConfiguration createConfiguration(final RuntimeMode runtimeMode, final String modelName, final String baseUrl) {
        return new LLME2EConfiguration(baseUrl, "openai-compatible", modelName, "ollama", 2, 2, 10, Path.of("target/llm-e2e"), "run-id", runtimeMode);
    }
    
    private HttpServer startModelServer(final String modelName) throws IOException {
        HttpServer result = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        result.createContext("/v1/models", exchange -> {
            byte[] response = String.format("{\"data\":[{\"id\":\"%s\"}]}", modelName).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        result.start();
        return result;
    }
    
    private String createBaseUrl(final HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    }
}
