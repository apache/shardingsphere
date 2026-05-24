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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * LLM E2E configuration.
 */
@RequiredArgsConstructor
@Getter
public final class LLME2EConfiguration {
    
    private static final DateTimeFormatter RUN_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
    
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080/v1";
    
    private static final String DEFAULT_MODEL_NAME = "ggml-org/Qwen3-1.7B-GGUF:Q4_K_M";
    
    private static final String DEFAULT_API_KEY = "mcp-llm-score";
    
    private static final String DEFAULT_SERVER_IMAGE = "apache/shardingsphere-mcp-llm-runtime:local";
    
    private static final String BASE_SERVER_IMAGE_DIGEST_AMD64 = "sha256:988d2695631987e28a29d98970aaf0e979e23b843a26824abb790ac4245d1d57";
    
    private static final String BASE_SERVER_IMAGE_DIGEST_ARM64 = "sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca";
    
    private final String baseUrl;
    
    private final String modelProvider;
    
    private final String modelName;
    
    private final String apiKey;
    
    private final int readyTimeoutSeconds;
    
    private final int requestTimeoutSeconds;
    
    private final int maxTurns;
    
    private final Path artifactRoot;
    
    private final String runId;
    
    private final RuntimeMode runtimeMode;
    
    private final String serverImage;
    
    private final String baseServerImageDigest;
    
    /**
     * Load LLM E2E configuration.
     *
     * @return LLM E2E configuration
     */
    public static LLME2EConfiguration load() {
        final RuntimeMode runtimeMode = RuntimeMode.from(readString("mcp.llm.runtime-mode", "MCP_LLM_RUNTIME_MODE", RuntimeMode.DOCKER.getValue()));
        return new LLME2EConfiguration(
                normalizeBaseUrl(readString("mcp.llm.base-url", "MCP_LLM_BASE_URL", DEFAULT_BASE_URL)),
                readString("mcp.llm.provider", "MCP_LLM_PROVIDER", "openai-compatible"),
                readString("mcp.llm.model", "MCP_LLM_MODEL", DEFAULT_MODEL_NAME),
                readString("mcp.llm.api-key", "MCP_LLM_API_KEY", DEFAULT_API_KEY),
                readInteger("mcp.llm.ready-timeout-seconds", "MCP_LLM_READY_TIMEOUT_SECONDS", 600),
                readInteger("mcp.llm.request-timeout-seconds", "MCP_LLM_REQUEST_TIMEOUT_SECONDS", 240),
                readInteger("mcp.llm.max-turns", "MCP_LLM_MAX_TURNS", 10),
                Paths.get(readString("mcp.llm.artifact-root", "MCP_LLM_ARTIFACT_ROOT", "target/llm-e2e")),
                readString("mcp.llm.run-id", "MCP_LLM_RUN_ID", createDefaultRunId()),
                runtimeMode,
                readString("mcp.llm.server-image", "MCP_LLM_SERVER_IMAGE", DEFAULT_SERVER_IMAGE),
                readString("mcp.llm.base-server-image-digest", "MCP_LLM_BASE_SERVER_IMAGE_DIGEST", getDefaultBaseServerImageDigest(runtimeMode)));
    }
    
    /**
     * Create artifact directory.
     *
     * @param scenarioId scenario ID
     * @return path
     * @throws IOException IO exception
     */
    public Path createArtifactDirectory(final String scenarioId) throws IOException {
        final Path result = artifactRoot.resolve(runId).resolve(scenarioId);
        Files.createDirectories(result);
        return result;
    }
    
    /**
     * Create a copy with another model endpoint.
     *
     * @param baseUrl model endpoint base URL
     * @return copied configuration
     */
    public LLME2EConfiguration withBaseUrl(final String baseUrl) {
        return new LLME2EConfiguration(normalizeBaseUrl(baseUrl), modelProvider, modelName, apiKey, readyTimeoutSeconds, requestTimeoutSeconds, maxTurns, artifactRoot, runId,
                runtimeMode, serverImage, baseServerImageDigest);
    }
    
    /**
     * Create a copy with another model endpoint and API key.
     *
     * @param baseUrl model endpoint base URL
     * @param apiKey API key
     * @return copied configuration
     */
    public LLME2EConfiguration withModelEndpoint(final String baseUrl, final String apiKey) {
        return new LLME2EConfiguration(normalizeBaseUrl(baseUrl), modelProvider, modelName, apiKey, readyTimeoutSeconds, requestTimeoutSeconds, maxTurns, artifactRoot, runId,
                runtimeMode, serverImage, baseServerImageDigest);
    }
    
    /**
     * Create a copy with shorter readiness probe timeouts.
     *
     * @param readyTimeoutSeconds ready timeout seconds
     * @param requestTimeoutSeconds request timeout seconds
     * @return copied configuration
     */
    public LLME2EConfiguration withReadinessTimeouts(final int readyTimeoutSeconds, final int requestTimeoutSeconds) {
        return new LLME2EConfiguration(baseUrl, modelProvider, modelName, apiKey, readyTimeoutSeconds, requestTimeoutSeconds, maxTurns, artifactRoot, runId, runtimeMode,
                serverImage, baseServerImageDigest);
    }
    
    /**
     * Get chat completions URL.
     *
     * @return chat completions URL
     */
    public String getChatCompletionsUrl() {
        return baseUrl + "/chat/completions";
    }
    
    /**
     * Get models URL.
     *
     * @return models URL
     */
    public String getModelsUrl() {
        return baseUrl + "/models";
    }
    
    private static String readString(final String propertyName, final String environmentName, final String defaultValue) {
        String result = System.getProperty(propertyName);
        if (null != result && !result.trim().isEmpty()) {
            return result.trim();
        }
        result = System.getenv(environmentName);
        return null == result || result.trim().isEmpty() ? defaultValue : result.trim();
    }
    
    private static int readInteger(final String propertyName, final String environmentName, final int defaultValue) {
        final String result = readString(propertyName, environmentName, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }
    
    private static String normalizeBaseUrl(final String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
    
    private static String createDefaultRunId() {
        return RUN_ID_FORMATTER.format(LocalDateTime.now()) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private static String getDefaultBaseServerImageDigest(final RuntimeMode runtimeMode) {
        return RuntimeMode.DOCKER == runtimeMode ? getDefaultBaseServerImageDigest(System.getProperty("os.arch", "")) : "";
    }
    
    static String getDefaultBaseServerImageDigest(final String architecture) {
        if ("amd64".equals(architecture) || "x86_64".equals(architecture)) {
            return BASE_SERVER_IMAGE_DIGEST_AMD64;
        }
        if ("aarch64".equals(architecture) || "arm64".equals(architecture)) {
            return BASE_SERVER_IMAGE_DIGEST_ARM64;
        }
        throw new IllegalStateException(String.format("Unsupported local architecture for MCP LLM Docker score mode: %s", architecture));
    }
    
    /**
     * LLM runtime mode.
     */
    @RequiredArgsConstructor
    @Getter
    public enum RuntimeMode {
        
        DOCKER("docker"),
        
        EXTERNAL_DEBUG("external-debug");
        
        private final String value;
        
        private static RuntimeMode from(final String value) {
            for (RuntimeMode each : values()) {
                if (each.value.equals(value)) {
                    return each;
                }
            }
            throw new IllegalStateException(String.format("Unsupported MCP LLM runtime mode `%s`.", value));
        }
    }
}
