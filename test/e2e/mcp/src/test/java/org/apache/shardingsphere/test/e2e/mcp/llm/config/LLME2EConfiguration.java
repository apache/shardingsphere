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

import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

/**
 * LLM E2E configuration.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
public final class LLME2EConfiguration {
    
    private static final DateTimeFormatter RUN_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
    
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080/v1";
    
    private static final String DEFAULT_MODEL_NAME = "ggml-org/Qwen3-1.7B-GGUF:Q4_K_M";
    
    private static final String DEFAULT_API_KEY = "mcp-llm-score";
    
    private static final String DEFAULT_SERVER_IMAGE = "apache/shardingsphere-mcp-llm-runtime:local";
    
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
    
    private final String baseServerImage;
    
    private final String baseServerImageDigest;
    
    private final ModelMetadata modelMetadata;
    
    /**
     * Load LLM E2E configuration.
     *
     * @return LLM E2E configuration
     */
    public static LLME2EConfiguration load() {
        Properties props = EnvironmentPropertiesLoader.loadProperties();
        RuntimeMode runtimeMode = RuntimeMode.from(readString(props, "mcp.llm.runtime-mode", RuntimeMode.DOCKER.getValue()));
        ModelMetadata modelMetadata = readModelMetadata(props);
        return LLME2EConfiguration.builder()
                .baseUrl(normalizeBaseUrl(readString(props, "mcp.llm.base-url", DEFAULT_BASE_URL)))
                .modelProvider(readString(props, "mcp.llm.provider", "openai-compatible"))
                .modelName(readString(props, "mcp.llm.model", DEFAULT_MODEL_NAME))
                .apiKey(readString(props, "mcp.llm.api-key", DEFAULT_API_KEY))
                .readyTimeoutSeconds(readInteger(props, "mcp.llm.ready-timeout-seconds", 600))
                .requestTimeoutSeconds(readInteger(props, "mcp.llm.request-timeout-seconds", 240))
                .maxTurns(readInteger(props, "mcp.llm.max-turns", 10))
                .artifactRoot(Paths.get(readString(props, "mcp.llm.artifact-root", "target/llm-e2e")))
                .runId(readString(props, "mcp.llm.run-id", createDefaultRunId()))
                .runtimeMode(runtimeMode)
                .serverImage(readString(props, "mcp.llm.server-image", DEFAULT_SERVER_IMAGE))
                .baseServerImage(readString(props, "mcp.llm.base-server-image", ""))
                .baseServerImageDigest(readString(props, "mcp.llm.base-server-image-digest", ""))
                .modelMetadata(modelMetadata)
                .build();
    }
    
    /**
     * Create artifact directory.
     *
     * @param scenarioId scenario ID
     * @return path
     * @throws IOException IO exception
     */
    public Path createArtifactDirectory(final String scenarioId) throws IOException {
        Path result = artifactRoot.resolve(runId).resolve(scenarioId);
        Files.createDirectories(result);
        return result;
    }
    
    /**
     * Create a copy with another model endpoint and API key.
     *
     * @param baseUrl model endpoint base URL
     * @param apiKey API key
     * @return copied configuration
     */
    public LLME2EConfiguration withModelEndpoint(final String baseUrl, final String apiKey) {
        return toBuilder().baseUrl(normalizeBaseUrl(baseUrl)).apiKey(apiKey).build();
    }
    
    /**
     * Create a copy with shorter readiness probe timeouts.
     *
     * @param readyTimeoutSeconds ready timeout seconds
     * @param requestTimeoutSeconds request timeout seconds
     * @return copied configuration
     */
    public LLME2EConfiguration withReadinessTimeouts(final int readyTimeoutSeconds, final int requestTimeoutSeconds) {
        return toBuilder().readyTimeoutSeconds(readyTimeoutSeconds).requestTimeoutSeconds(requestTimeoutSeconds).build();
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
    
    /**
     * Get model SHA-256 checksum.
     *
     * @return model SHA-256 checksum
     */
    public String getModelSha256() {
        return modelMetadata.getSha256();
    }
    
    private static String readString(final Properties props, final String propertyName, final String defaultValue) {
        String result = props.getProperty(propertyName);
        return null == result || result.trim().isEmpty() ? defaultValue : result.trim();
    }
    
    private static String readRequiredString(final Properties props, final String propertyName) {
        String result = readString(props, propertyName, "");
        if (result.isEmpty()) {
            throw new IllegalStateException(String.format("MCP LLM E2E property `%s` is required.", propertyName));
        }
        return result;
    }
    
    private static int readInteger(final Properties props, final String propertyName, final int defaultValue) {
        String result = readString(props, propertyName, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }
    
    private static ModelMetadata readModelMetadata(final Properties props) {
        return new ModelMetadata(
                readRequiredString(props, "mcp.llm.model-repository"),
                readRequiredString(props, "mcp.llm.model-file-name"),
                readRequiredString(props, "mcp.llm.model-quantization"),
                readRequiredString(props, "mcp.llm.model-revision"),
                readRequiredString(props, "mcp.llm.model-sha256"));
    }
    
    private static String normalizeBaseUrl(final String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
    
    private static String createDefaultRunId() {
        return RUN_ID_FORMATTER.format(LocalDateTime.now()) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * LLM model metadata.
     */
    @RequiredArgsConstructor
    @EqualsAndHashCode
    @Getter
    public static final class ModelMetadata {
        
        private final String repository;
        
        private final String fileName;
        
        private final String quantization;
        
        private final String revision;
        
        private final String sha256;
        
        /**
         * Get model path inside the runtime container.
         *
         * @return model path
         */
        public String getContainerPath() {
            return "/models/" + fileName;
        }
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
