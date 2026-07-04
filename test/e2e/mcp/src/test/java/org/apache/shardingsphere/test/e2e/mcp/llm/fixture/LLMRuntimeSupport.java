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
    
    private static final String REQUIRED_PROVIDER = "openai-compatible";
    
    private static final int SERVER_PORT = 8080;
    
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
        validateSupportedProvider(config);
        if (RuntimeMode.EXTERNAL_DEBUG == config.getRuntimeMode()) {
            return prepareExternalDebugRuntime(config);
        }
        if (null != sharedContainerRuntime && sharedContainerRuntime.isReusable(config)) {
            return sharedContainerRuntime;
        }
        stopSharedRuntime();
        requireDockerAvailable();
        String serverImageId = requireScoreImageAvailable(config.getServerImage());
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
        sharedContainerRuntime = ModelRuntime.container(actualConfig, container, serverImageId);
        registerShutdownHook(sharedContainerRuntime);
        return sharedContainerRuntime;
    }
    
    private static ModelRuntime prepareExternalDebugRuntime(final LLME2EConfiguration config) throws InterruptedException {
        if (!isModelReady(config)) {
            throw new IllegalStateException("MCP LLM external-debug mode requires a ready OpenAI-compatible endpoint.");
        }
        return ModelRuntime.externalDebug(config);
    }
    
    private static void validateSupportedProvider(final LLME2EConfiguration config) {
        if (!REQUIRED_PROVIDER.equals(config.getModelProvider())) {
            throw new IllegalStateException("MCP LLM E2E requires provider openai-compatible.");
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
    
    private static String requireScoreImageAvailable(final String serverImage) {
        try {
            return DockerClientFactory.instance().client().inspectImageCmd(serverImage).exec().getId();
        } catch (final NotFoundException ex) {
            throw new IllegalStateException(String.format(
                    "MCP LLM Docker score mode requires prebuilt local image `%s`. Build it before running Maven LLM tests.", serverImage), ex);
        }
    }
    
    private static GenericContainer<?> createContainer(final LLME2EConfiguration config) {
        return new GenericContainer<>(DockerImageName.parse(config.getServerImage()))
                .withImagePullPolicy(imageName -> false)
                .withExposedPorts(SERVER_PORT)
                .withCommand("--host", "0.0.0.0", "--port", String.valueOf(SERVER_PORT), "-m", config.getModelMetadata().getContainerPath(), "--alias", config.getModelName(),
                        "--jinja", "--reasoning", "off", "--reasoning-budget", "0", "--chat-template-kwargs", "{\"enable_thinking\":false}",
                        "--api-key", config.getApiKey(), "--no-ui", "-n", "512", "--parallel", "1", "-c", "2048", "-b", "256", "-ub", "128", "--cache-ram", "0", "--no-cache-prompt")
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(5));
    }
    
    private static LLME2EConfiguration createDockerRuntimeConfiguration(final LLME2EConfiguration config, final GenericContainer<?> container) {
        return config.withModelEndpoint(String.format("http://%s:%d/v1", container.getHost(), container.getMappedPort(SERVER_PORT)), config.getApiKey());
    }
    
    private static void registerShutdownHook(final ModelRuntime runtime) {
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::stop, "mcp-llm-runtime-shutdown"));
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
        
        private static ModelRuntime container(final LLME2EConfiguration config, final GenericContainer<?> container, final String serverImageId) {
            return new ModelRuntime(config, container, createScoreClosingEvidence(config, serverImageId));
        }
        
        private static Map<String, Object> createExternalDebugEvidence(final LLME2EConfiguration config) {
            Map<String, Object> result = new LinkedHashMap<>(8, 1F);
            result.put("runtimeMode", config.getRuntimeMode().getValue());
            result.put("dockerOwned", false);
            result.put("provider", config.getModelProvider());
            result.put("serverRuntime", "external-openai-compatible");
            result.put("modelReference", config.getModelName());
            result.put("servedModelId", config.getModelName());
            result.put("modelPackaging", "external");
            result.put("baseUrlOwnedByTest", false);
            result.put("scoreClosing", false);
            return result;
        }
        
        private static Map<String, Object> createScoreClosingEvidence(final LLME2EConfiguration config, final String serverImageId) {
            Map<String, Object> result = new LinkedHashMap<>(17, 1F);
            result.put("runtimeMode", config.getRuntimeMode().getValue());
            result.put("dockerOwned", true);
            result.put("provider", config.getModelProvider());
            result.put("serverRuntime", "llama.cpp");
            result.put("serverImage", config.getServerImage());
            result.put("serverImageId", serverImageId);
            result.put("baseServerImage", config.getBaseServerImage());
            result.put("baseServerImageDigest", config.getBaseServerImageDigest());
            result.put("modelReference", config.getModelName());
            result.put("servedModelId", config.getModelName());
            result.put("modelQuantization", config.getModelMetadata().getQuantization());
            result.put("modelRevision", config.getModelMetadata().getRevision());
            result.put("modelFileName", config.getModelMetadata().getFileName());
            result.put("modelSha256", config.getModelSha256());
            result.put("modelPackaging", "prepackaged");
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
