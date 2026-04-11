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
    
    private final boolean enabled;
    
    private final String baseUrl;
    
    private final String modelName;
    
    private final String apiKey;
    
    private final int readyTimeoutSeconds;
    
    private final int requestTimeoutSeconds;
    
    private final int maxTurns;
    
    private final Path artifactRoot;
    
    private final String runId;
    
    /**
     * Load LLM E2E configuration.
     *
     * @return LLM E2E configuration
     */
    public static LLME2EConfiguration load() {
        return new LLME2EConfiguration(
                readBoolean("mcp.llm.e2e.enabled", "MCP_LLM_E2E_ENABLED", false),
                normalizeBaseUrl(readString("mcp.llm.base-url", "MCP_LLM_BASE_URL", "http://127.0.0.1:11434/v1")),
                readString("mcp.llm.model", "MCP_LLM_MODEL", "qwen3:1.7b"),
                readString("mcp.llm.api-key", "MCP_LLM_API_KEY", "ollama"),
                readInteger("mcp.llm.ready-timeout-seconds", "MCP_LLM_READY_TIMEOUT_SECONDS", 600),
                readInteger("mcp.llm.request-timeout-seconds", "MCP_LLM_REQUEST_TIMEOUT_SECONDS", 240),
                readInteger("mcp.llm.max-turns", "MCP_LLM_MAX_TURNS", 6),
                Paths.get(readString("mcp.llm.artifact-root", "MCP_LLM_ARTIFACT_ROOT", "target/llm-e2e")),
                readString("mcp.llm.run-id", "MCP_LLM_RUN_ID", createDefaultRunId()));
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
    
    private static boolean readBoolean(final String propertyName, final String environmentName, final boolean defaultValue) {
        return Boolean.parseBoolean(readString(propertyName, environmentName, String.valueOf(defaultValue)));
    }
    
    private static String normalizeBaseUrl(final String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
    
    private static String createDefaultRunId() {
        return RUN_ID_FORMATTER.format(LocalDateTime.now()) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
