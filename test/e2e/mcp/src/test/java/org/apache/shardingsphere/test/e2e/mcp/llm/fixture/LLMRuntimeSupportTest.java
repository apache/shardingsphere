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
import com.sun.net.httpserver.HttpExchange;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class LLMRuntimeSupportTest {
    
    private static final String REQUIRED_MODEL = "ggml-org/Qwen3-1.7B-GGUF:Q4_K_M";
    
    private static final LLME2EConfiguration.ModelMetadata MODEL_METADATA = new LLME2EConfiguration.ModelMetadata(
            "ggml-org/Qwen3-1.7B-GGUF", "Qwen3-1.7B-Q4_K_M.gguf", "Q4_K_M", "daeb8e2d528a760970442092f6bf1e55c3b659eb", "configured-model-sha256");
    
    private static final String DOCKER_REQUIRED_MESSAGE = "Docker is required to start the prepackaged llama.cpp server for MCP LLM E2E.";
    
    @Test
    void assertPrepareWithExternalDebugRuntime() throws InterruptedException, IOException {
        HttpServer server = startModelServer("debug-model");
        try {
            LLMRuntimeSupport.ModelRuntime actual = LLMRuntimeSupport.prepare(createConfiguration(RuntimeMode.EXTERNAL_DEBUG, "debug-model", createBaseUrl(server)));
            assertThat(actual.getConfiguration().getBaseUrl(), is(createBaseUrl(server)));
            assertThat(actual.getEvidence().get("runtimeMode"), is("external-debug"));
            assertThat(actual.getEvidence().get("provider"), is("openai-compatible"));
            assertThat(actual.getEvidence().get("serverRuntime"), is("external-openai-compatible"));
            assertFalse((boolean) actual.getEvidence().get("scoreClosing"));
        } finally {
            server.stop(0);
        }
    }
    
    @Test
    void assertPrepareWithDockerRuntimeIgnoresExternalEndpoint() throws IOException {
        HttpServer server = startModelServer(REQUIRED_MODEL);
        try (MockedStatic<MySQLRuntimeTestSupport> mocked = mockStatic(MySQLRuntimeTestSupport.class)) {
            mocked.when(MySQLRuntimeTestSupport::isDockerAvailable).thenReturn(false);
            mocked.when(() -> MySQLRuntimeTestSupport.createDockerRequiredMessage(DOCKER_REQUIRED_MESSAGE)).thenReturn(DOCKER_REQUIRED_MESSAGE);
            IllegalStateException actualException = assertThrows(IllegalStateException.class,
                    () -> LLMRuntimeSupport.prepare(createConfiguration(RuntimeMode.DOCKER, REQUIRED_MODEL, createBaseUrl(server))));
            assertThat(actualException.getMessage(), is(DOCKER_REQUIRED_MESSAGE));
        } finally {
            server.stop(0);
        }
    }
    
    @Test
    void assertPrepareWithUnavailableExternalDebugRuntime() {
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                () -> LLMRuntimeSupport.prepare(createConfiguration(RuntimeMode.EXTERNAL_DEBUG, "debug-model", "http://127.0.0.1:1/v1")));
        assertThat(actualException.getMessage(), is("MCP LLM external-debug mode requires a ready OpenAI-compatible endpoint."));
    }
    
    @Test
    void assertPrepareWithUnsupportedProvider() {
        LLME2EConfiguration config = new LLME2EConfiguration("http://127.0.0.1:8080/v1", "openai", REQUIRED_MODEL, "mcp-llm-score", 600, 240, 10,
                Path.of("target/llm-e2e"), "run-id", RuntimeMode.DOCKER, "llama.cpp", "apache/shardingsphere-mcp-llm-runtime:local", "ghcr.io/ggml-org/llama.cpp:server",
                "test-base-server-image-digest", MODEL_METADATA);
        IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> LLMRuntimeSupport.prepare(config));
        assertThat(actualException.getMessage(), is("MCP LLM E2E requires provider openai-compatible."));
    }
    
    private LLME2EConfiguration createConfiguration(final RuntimeMode runtimeMode, final String modelName, final String baseUrl) {
        return new LLME2EConfiguration(baseUrl, "openai-compatible", modelName, "mcp-llm-score", 2, 2, 10, Path.of("target/llm-e2e"), "run-id",
                runtimeMode, "llama.cpp", "apache/shardingsphere-mcp-llm-runtime:local", "ghcr.io/ggml-org/llama.cpp:server", "test-base-server-image-digest", MODEL_METADATA);
    }
    
    private HttpServer startModelServer(final String modelName) throws IOException {
        HttpServer result = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        result.createContext("/v1/models", exchange -> writeResponse(exchange, String.format("{\"data\":[{\"id\":\"%s\"}]}", modelName)));
        result.createContext("/v1/chat/completions", exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            writeResponse(exchange, createCompletionResponse(requestBody));
        });
        result.start();
        return result;
    }
    
    private String createCompletionResponse(final String requestBody) {
        if (requestBody.contains("\"tool_choice\":\"required\"")) {
            return createToolCallResponse();
        }
        if (requestBody.contains("\"response_format\"")) {
            return "{\"choices\":[{\"message\":{\"content\":\"{\\\"status\\\":\\\"ok\\\"}\"}}]}";
        }
        return "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}";
    }
    
    private String createToolCallResponse() {
        return "{\"choices\":[{\"message\":{\"content\":\"\",\"tool_calls\":[{\"id\":\"call_1\",\"type\":\"function\","
                + "\"function\":{\"name\":\"mcp_read_resource\",\"arguments\":\"{\\\"uri\\\":\\\"mcp://readiness\\\"}\"}}]}}]}";
    }
    
    private void writeResponse(final HttpExchange exchange, final String responseBody) throws IOException {
        byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }
    
    private String createBaseUrl(final HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    }
}
