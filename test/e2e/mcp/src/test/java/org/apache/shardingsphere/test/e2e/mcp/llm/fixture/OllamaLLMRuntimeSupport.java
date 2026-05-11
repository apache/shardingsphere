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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Dockerized Ollama runtime support for MCP LLM E2E.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OllamaLLMRuntimeSupport {
    
    private static final String OLLAMA_IMAGE = "ollama/ollama:latest";
    
    private static final String REQUIRED_PROVIDER = "openai-compatible";
    
    private static final String REQUIRED_MODEL = "qwen3:1.7b";
    
    private static final int OLLAMA_PORT = 11434;
    
    /**
     * Prepare the required local LLM runtime.
     *
     * @param config LLM E2E configuration
     * @return prepared LLM runtime
     * @throws InterruptedException interrupted exception
     */
    public static ModelRuntime prepare(final LLME2EConfiguration config) throws InterruptedException {
        validateRequiredModel(config);
        if (isModelReady(config)) {
            return ModelRuntime.external(config);
        }
        requireDockerAvailable();
        GenericContainer<?> container = createContainer();
        container.start();
        pullModel(container, config.getModelName());
        LLME2EConfiguration actualConfig = config.withBaseUrl(String.format("http://%s:%d/v1", container.getHost(), container.getMappedPort(OLLAMA_PORT)));
        new LLMChatModelClient(actualConfig, HttpClient.newHttpClient()).waitUntilReady();
        return ModelRuntime.container(actualConfig, container);
    }
    
    private static void requireDockerAvailable() {
        if (!MySQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException("Docker is required to start Ollama and pull qwen3:1.7b for MCP LLM E2E.");
        }
    }
    
    private static void validateRequiredModel(final LLME2EConfiguration config) {
        if (!REQUIRED_PROVIDER.equals(config.getModelProvider()) || !REQUIRED_MODEL.equals(config.getModelName())) {
            throw new IllegalStateException("MCP LLM E2E requires provider openai-compatible and model qwen3:1.7b.");
        }
    }
    
    private static boolean isModelReady(final LLME2EConfiguration config) throws InterruptedException {
        try {
            new LLMChatModelClient(config.withReadinessTimeouts(2, 2), HttpClient.newHttpClient()).waitUntilReady();
            return true;
        } catch (final IllegalStateException ignored) {
            return false;
        }
    }
    
    private static GenericContainer<?> createContainer() {
        return new GenericContainer<>(DockerImageName.parse(OLLAMA_IMAGE))
                .withExposedPorts(OLLAMA_PORT)
                .waitingFor(Wait.forHttp("/api/tags").forPort(OLLAMA_PORT).forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(3));
    }
    
    private static void pullModel(final GenericContainer<?> container, final String modelName) {
        try {
            Container.ExecResult actual = container.execInContainer("ollama", "pull", modelName);
            if (0 != actual.getExitCode()) {
                throw new IllegalStateException(String.format("Ollama model pull failed for `%s`: %s%s", modelName, actual.getStdout(), actual.getStderr()));
            }
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to pull Ollama model `qwen3:1.7b`.", ex);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while pulling Ollama model `qwen3:1.7b`.", ex);
        }
    }
    
    /**
     * Prepared model runtime.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class ModelRuntime implements AutoCloseable {
        
        private final LLME2EConfiguration configuration;
        
        private final GenericContainer<?> container;
        
        private static ModelRuntime external(final LLME2EConfiguration config) {
            return new ModelRuntime(config, null);
        }
        
        private static ModelRuntime container(final LLME2EConfiguration config, final GenericContainer<?> container) {
            return new ModelRuntime(config, container);
        }
        
        @Override
        public void close() {
            if (null != container) {
                container.stop();
            }
        }
    }
}
