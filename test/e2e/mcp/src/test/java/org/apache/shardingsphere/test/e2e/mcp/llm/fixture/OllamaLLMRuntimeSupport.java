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
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
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
    
    private static final String OLLAMA_IMAGE = "ollama/ollama:0.23.1";
    
    private static final String OLLAMA_IMAGE_DIGEST_AMD64 = "sha256:133a0539e836688c7cb88e318e31232f344a84cff7aab0cf6ac90476bc99c8ed";
    
    private static final String OLLAMA_IMAGE_DIGEST_ARM64 = "sha256:fcaa568338a6b0993c82f259a5072f46814d6de276cf3dea5b91e281b7f9d149";
    
    private static final String REQUIRED_PROVIDER = "openai-compatible";
    
    private static final String REQUIRED_MODEL = "qwen3:1.7b";
    
    private static final String OLLAMA_API_KEY = "ollama";
    
    private static final int OLLAMA_PORT = 11434;
    
    private static ModelRuntime sharedContainerRuntime;
    
    /**
     * Prepare the required local LLM runtime.
     *
     * @param config LLM E2E configuration
     * @return prepared LLM runtime
     * @throws InterruptedException interrupted exception
     */
    public static synchronized ModelRuntime prepare(final LLME2EConfiguration config) throws InterruptedException {
        validateSupportedProvider(config);
        if (RuntimeMode.EXTERNAL_DEBUG == config.getRuntimeMode()) {
            return prepareExternalDebugRuntime(config);
        }
        validateRequiredModel(config);
        if (null != sharedContainerRuntime && sharedContainerRuntime.isReusable()) {
            return sharedContainerRuntime;
        }
        requireDockerAvailable();
        final GenericContainer<?> container = createContainer();
        container.start();
        pullModel(container, config.getModelName());
        final LLME2EConfiguration actualConfig = createDockerRuntimeConfiguration(config, container);
        new LLMChatModelClient(actualConfig, HttpClient.newHttpClient()).waitUntilReady();
        sharedContainerRuntime = ModelRuntime.container(actualConfig, container);
        registerShutdownHook(sharedContainerRuntime);
        return sharedContainerRuntime;
    }
    
    /**
     * Get score-closing Ollama image.
     *
     * @return score-closing Ollama image
     */
    static String getScoreClosingImage() {
        return OLLAMA_IMAGE;
    }
    
    static String getScoreClosingDockerImage() {
        return String.format("%s@%s", OLLAMA_IMAGE, getScoreClosingImageDigest(System.getProperty("os.arch", "")));
    }
    
    static String getScoreClosingImageDigest() {
        return getScoreClosingImageDigest(System.getProperty("os.arch", ""));
    }
    
    private static String getScoreClosingImageDigest(final String architecture) {
        if ("amd64".equals(architecture) || "x86_64".equals(architecture)) {
            return OLLAMA_IMAGE_DIGEST_AMD64;
        }
        if ("aarch64".equals(architecture) || "arm64".equals(architecture)) {
            return OLLAMA_IMAGE_DIGEST_ARM64;
        }
        throw new IllegalStateException(String.format("Unsupported local architecture for MCP LLM Docker score mode: %s", architecture));
    }
    
    private static ModelRuntime prepareExternalDebugRuntime(final LLME2EConfiguration config) throws InterruptedException {
        if (!isModelReady(config)) {
            throw new IllegalStateException("MCP LLM external-debug mode requires a ready OpenAI-compatible endpoint.");
        }
        return ModelRuntime.externalDebug(config);
    }
    
    private static void requireDockerAvailable() {
        if (!MySQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException(MySQLRuntimeTestSupport.createDockerRequiredMessage(
                    "Docker is required to start Ollama and pull qwen3:1.7b for MCP LLM E2E."));
        }
    }
    
    private static void validateSupportedProvider(final LLME2EConfiguration config) {
        if (!REQUIRED_PROVIDER.equals(config.getModelProvider())) {
            throw new IllegalStateException("MCP LLM E2E requires provider openai-compatible.");
        }
    }
    
    private static void validateRequiredModel(final LLME2EConfiguration config) {
        if (!REQUIRED_MODEL.equals(config.getModelName())) {
            throw new IllegalStateException("MCP LLM Docker score mode requires model qwen3:1.7b.");
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
    
    private static LLME2EConfiguration createDockerRuntimeConfiguration(final LLME2EConfiguration config, final GenericContainer<?> container) {
        return new LLME2EConfiguration(
                String.format("http://%s:%d/v1", container.getHost(), container.getMappedPort(OLLAMA_PORT)),
                config.getModelProvider(), config.getModelName(), OLLAMA_API_KEY, config.getReadyTimeoutSeconds(), config.getRequestTimeoutSeconds(),
                config.getMaxTurns(), config.getArtifactRoot(), config.getRunId(), config.getRuntimeMode());
    }
    
    private static GenericContainer<?> createContainer() {
        return new GenericContainer<>(DockerImageName.parse(getScoreClosingDockerImage()))
                .withExposedPorts(OLLAMA_PORT)
                .waitingFor(Wait.forHttp("/api/tags").forPort(OLLAMA_PORT).forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(3));
    }
    
    private static void pullModel(final GenericContainer<?> container, final String modelName) {
        try {
            final Container.ExecResult actual = container.execInContainer("ollama", "pull", modelName);
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
    
    private static void registerShutdownHook(final ModelRuntime runtime) {
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::stop, "mcp-llm-ollama-shutdown"));
    }
    
    /**
     * Prepared model runtime.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class ModelRuntime implements AutoCloseable {
        
        private final LLME2EConfiguration configuration;
        
        private final GenericContainer<?> container;
        
        private final RuntimeMode runtimeMode;
        
        private final String imageName;
        
        private final String imageDigest;
        
        private static ModelRuntime externalDebug(final LLME2EConfiguration config) {
            return new ModelRuntime(config, null, RuntimeMode.EXTERNAL_DEBUG, "", "");
        }
        
        private static ModelRuntime container(final LLME2EConfiguration config, final GenericContainer<?> container) {
            return new ModelRuntime(config, container, RuntimeMode.DOCKER, getScoreClosingImage(), getScoreClosingImageDigest());
        }
        
        @Override
        public void close() {
            if (this == sharedContainerRuntime) {
                return;
            }
            stop();
        }
        
        private boolean isReusable() {
            return null != container && container.isRunning();
        }
        
        private void stop() {
            if (null != container) {
                container.stop();
            }
        }
    }
}
