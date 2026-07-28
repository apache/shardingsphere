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

import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Docker-owned LLM runtime support for MCP LLM E2E.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LLMRuntimeSupport {
    
    private static final int CONTEXT_WINDOW_TOKENS = 8192;
    
    private static final int SERVER_PORT = 8080;
    
    private static final String LABEL_PREFIX = "org.apache.shardingsphere.mcp.llm.";
    
    private static final String RUNTIME_LABEL = LABEL_PREFIX + "runtime";
    
    private static final String BASE_SERVER_IMAGE_LABEL = LABEL_PREFIX + "base-server-image";
    
    private static final String BASE_SERVER_IMAGE_DIGEST_LABEL = LABEL_PREFIX + "base-server-image-digest";
    
    private static final String MODEL_REPOSITORY_LABEL = LABEL_PREFIX + "model-repository";
    
    private static final String MODEL_QUANTIZATION_LABEL = LABEL_PREFIX + "model-quantization";
    
    private static final String MODEL_REFERENCE_LABEL = LABEL_PREFIX + "model-reference";
    
    private static final String MODEL_REVISION_LABEL = LABEL_PREFIX + "model-revision";
    
    private static final String MODEL_FILE_NAME_LABEL = LABEL_PREFIX + "model-file-name";
    
    private static final String MODEL_SHA256_LABEL = LABEL_PREFIX + "model-sha256";
    
    private static ModelRuntime sharedContainerRuntime;
    
    /**
     * Prepare the required local LLM runtime.
     *
     * @param config LLM E2E configuration
     * @return prepared LLM runtime
     * @throws IllegalStateException when the runtime cannot be prepared
     * @throws InterruptedException interrupted exception
     */
    public static synchronized ModelRuntime prepare(final LLME2EConfiguration config) throws InterruptedException {
        if (RuntimeMode.EXTERNAL_DEBUG == config.getRuntimeMode()) {
            return prepareExternalDebugRuntime(config);
        }
        if (null != sharedContainerRuntime && sharedContainerRuntime.isReusable(config)) {
            return sharedContainerRuntime;
        }
        stopSharedRuntime();
        requireDockerAvailable();
        ScoreImage scoreImage = requireScoreImageAvailable(config);
        GenericContainer<?> container = createContainer(config);
        try {
            container.start();
        } catch (final ContainerLaunchException ex) {
            String containerLogs;
            try {
                containerLogs = container.getLogs();
            } catch (final IllegalStateException logException) {
                containerLogs = "<unavailable: " + logException.getMessage() + ">";
            }
            containerLogs = containerLogs.isBlank() ? "<empty>" : containerLogs;
            throw new IllegalStateException(String.format(
                    "MCP LLM Docker runtime container failed to start. serverImage=`%s`, baseServerImage=`%s`, baseServerImageDigest=`%s`, javaOs=`%s`, javaArch=`%s`, containerLogs=%s",
                    config.getServerImage(), config.getBaseServerImage(), config.getBaseServerImageDigest(), System.getProperty("os.name"), System.getProperty("os.arch"),
                    containerLogs), ex);
        }
        LLME2EConfiguration actualConfig = createDockerRuntimeConfiguration(config, container);
        new LLMChatModelClient(actualConfig, HttpClient.newHttpClient()).waitUntilReady();
        sharedContainerRuntime = ModelRuntime.container(actualConfig, container, scoreImage);
        registerShutdownHook(sharedContainerRuntime);
        return sharedContainerRuntime;
    }
    
    private static ModelRuntime prepareExternalDebugRuntime(final LLME2EConfiguration config) throws InterruptedException {
        if (!isModelReady(config)) {
            throw new IllegalStateException("MCP LLM external-debug mode requires a ready OpenAI-compatible endpoint.");
        }
        return ModelRuntime.externalDebug(config);
    }
    
    private static boolean isModelReady(final LLME2EConfiguration config) throws InterruptedException {
        try {
            new LLMChatModelClient(config.withReadinessTimeouts(2, 2), HttpClient.newHttpClient()).waitUntilReady();
            return true;
        } catch (final IllegalStateException ignored) {
            return false;
        }
    }
    
    private static void stopSharedRuntime() {
        if (null != sharedContainerRuntime) {
            sharedContainerRuntime.stop();
            sharedContainerRuntime = null;
        }
    }
    
    private static void requireDockerAvailable() {
        if (!MySQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException(MySQLRuntimeTestSupport.createDockerRequiredMessage(
                    "Docker is required to start the prepackaged llama.cpp server for MCP LLM E2E."));
        }
    }
    
    private static ScoreImage requireScoreImageAvailable(final LLME2EConfiguration config) {
        try {
            InspectImageResponse image = DockerClientFactory.instance().client().inspectImageCmd(config.getServerImage()).exec();
            Map<String, String> labels = null == image.getConfig() ? Map.of() : image.getConfig().getLabels();
            labels = null == labels ? Map.of() : labels;
            validateImageLabel(labels, RUNTIME_LABEL, "llama.cpp");
            validateImageLabel(labels, BASE_SERVER_IMAGE_LABEL, config.getBaseServerImage());
            validateImageLabel(labels, BASE_SERVER_IMAGE_DIGEST_LABEL, config.getBaseServerImageDigest());
            validateImageLabel(labels, MODEL_REPOSITORY_LABEL, config.getModelMetadata().getRepository());
            validateImageLabel(labels, MODEL_QUANTIZATION_LABEL, config.getModelMetadata().getQuantization());
            validateImageLabel(labels, MODEL_REFERENCE_LABEL, config.getModelName());
            validateImageLabel(labels, MODEL_REVISION_LABEL, config.getModelMetadata().getRevision());
            validateImageLabel(labels, MODEL_FILE_NAME_LABEL, config.getModelMetadata().getFileName());
            validateImageLabel(labels, MODEL_SHA256_LABEL, config.getModelSha256());
            if (null == image.getId() || image.getId().isBlank()) {
                throw new IllegalStateException(String.format("MCP LLM Docker score image `%s` has no immutable image ID.", config.getServerImage()));
            }
            return new ScoreImage(image.getId(), labels);
        } catch (final NotFoundException ex) {
            throw new IllegalStateException(String.format(
                    "MCP LLM Docker score mode requires prebuilt local image `%s`. Build it before running Maven LLM tests.", config.getServerImage()), ex);
        }
    }
    
    private static void validateImageLabel(final Map<String, String> labels, final String labelName, final String expectedValue) {
        String actualValue = labels.get(labelName);
        if (expectedValue.isBlank() || !expectedValue.equals(actualValue)) {
            throw new IllegalStateException(String.format(
                    "MCP LLM Docker score image label `%s` must match non-empty configuration. expected=`%s`, actual=`%s`.", labelName, expectedValue, actualValue));
        }
    }
    
    private static GenericContainer<?> createContainer(final LLME2EConfiguration config) {
        return new GenericContainer<>(DockerImageName.parse(config.getServerImage()))
                .withImagePullPolicy(imageName -> false)
                .withExposedPorts(SERVER_PORT)
                .withCommand("--host", "0.0.0.0", "--port", String.valueOf(SERVER_PORT), "-m", config.getModelMetadata().getContainerPath(), "--alias", config.getModelName(),
                        "--jinja", "--reasoning", "auto", "--reasoning-format", "deepseek", "--reasoning-budget", "0", "--chat-template-kwargs", "{\"enable_thinking\":false}",
                        "--api-key", config.getApiKey(), "--no-ui", "-n", "512", "--parallel", "1", "-c", String.valueOf(CONTEXT_WINDOW_TOKENS),
                        "-b", "256", "-ub", "128", "--cache-ram", "0", "--no-cache-prompt")
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(5));
    }
    
    private static LLME2EConfiguration createDockerRuntimeConfiguration(final LLME2EConfiguration config, final GenericContainer<?> container) {
        return config.withModelEndpoint(String.format("http://%s:%d/v1", container.getHost(), container.getMappedPort(SERVER_PORT)), config.getApiKey());
    }
    
    private static void registerShutdownHook(final ModelRuntime runtime) {
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::stop, "mcp-llm-runtime-shutdown"));
    }
    
    private record ScoreImage(String id, Map<String, String> labels) {
    }
    
    /**
     * Prepared model runtime.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class ModelRuntime implements AutoCloseable {
        
        private final LLME2EConfiguration configuration;
        
        private final GenericContainer<?> container;
        
        private final Map<String, Object> evidence;
        
        private static ModelRuntime externalDebug(final LLME2EConfiguration config) {
            return new ModelRuntime(config, null, createExternalDebugEvidence(config));
        }
        
        private static ModelRuntime container(final LLME2EConfiguration config, final GenericContainer<?> container, final ScoreImage scoreImage) {
            return new ModelRuntime(config, container, createScoreClosingEvidence(config, scoreImage));
        }
        
        private static Map<String, Object> createExternalDebugEvidence(final LLME2EConfiguration config) {
            Map<String, Object> result = new LinkedHashMap<>(8, 1F);
            result.put("runtimeMode", config.getRuntimeMode().getValue());
            result.put("dockerOwned", false);
            result.put("provider", LLME2EConfiguration.MODEL_PROVIDER);
            result.put("serverRuntime", "external-openai-compatible");
            result.put("modelReference", config.getModelName());
            result.put("servedModelId", config.getModelName());
            result.put("modelPackaging", "external");
            result.put("baseUrlOwnedByTest", false);
            result.put("scoreClosing", false);
            return result;
        }
        
        private static Map<String, Object> createScoreClosingEvidence(final LLME2EConfiguration config, final ScoreImage scoreImage) {
            Map<String, String> labels = scoreImage.labels();
            Map<String, Object> result = new LinkedHashMap<>(20, 1F);
            result.put("runtimeMode", config.getRuntimeMode().getValue());
            result.put("dockerOwned", true);
            result.put("provider", LLME2EConfiguration.MODEL_PROVIDER);
            result.put("serverRuntime", labels.get(RUNTIME_LABEL));
            result.put("serverImage", config.getServerImage());
            result.put("serverImageId", scoreImage.id());
            result.put("baseServerImage", labels.get(BASE_SERVER_IMAGE_LABEL));
            result.put("baseServerImageDigest", labels.get(BASE_SERVER_IMAGE_DIGEST_LABEL));
            result.put("modelRepository", labels.get(MODEL_REPOSITORY_LABEL));
            result.put("modelReference", labels.get(MODEL_REFERENCE_LABEL));
            result.put("servedModelId", config.getModelName());
            result.put("modelQuantization", labels.get(MODEL_QUANTIZATION_LABEL));
            result.put("modelRevision", labels.get(MODEL_REVISION_LABEL));
            result.put("modelFileName", labels.get(MODEL_FILE_NAME_LABEL));
            result.put("modelSha256", labels.get(MODEL_SHA256_LABEL));
            result.put("modelPackaging", "prepackaged");
            result.put("contextWindowTokens", CONTEXT_WINDOW_TOKENS);
            result.put("baseUrlOwnedByTest", true);
            result.put("scoreClosing", true);
            return result;
        }
        
        @Override
        public void close() {
            if (this == sharedContainerRuntime) {
                return;
            }
            stop();
        }
        
        private boolean isReusable(final LLME2EConfiguration config) {
            return null != container && container.isRunning()
                    && configuration.getRuntimeMode() == config.getRuntimeMode()
                    && configuration.getModelName().equals(config.getModelName())
                    && configuration.getApiKey().equals(config.getApiKey())
                    && configuration.getServerImage().equals(config.getServerImage())
                    && configuration.getBaseServerImage().equals(config.getBaseServerImage())
                    && configuration.getBaseServerImageDigest().equals(config.getBaseServerImageDigest())
                    && configuration.getModelMetadata().equals(config.getModelMetadata());
        }
        
        private void stop() {
            if (null != container) {
                container.stop();
            }
        }
    }
}
