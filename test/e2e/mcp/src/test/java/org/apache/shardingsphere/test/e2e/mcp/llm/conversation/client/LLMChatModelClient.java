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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * LLM chat client.
 */
@RequiredArgsConstructor
public final class LLMChatModelClient {
    
    private static final long INITIAL_READINESS_INTERVAL_MILLIS = 250L;
    
    private static final long MAX_READINESS_INTERVAL_MILLIS = 2000L;
    
    private static final int COMPLETION_MAX_TOKENS = 512;
    
    private static final int READINESS_MAX_TOKENS = 64;
    
    private final LLME2EConfiguration config;
    
    private final HttpClient httpClient;
    
    /**
     * Wait until ready.
     *
     * @throws InterruptedException interrupted exception
     * @throws IllegalStateException model service is not ready
     */
    public void waitUntilReady() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long deadline = startTime + config.getReadyTimeoutSeconds() * 1000L;
        long intervalMillis = INITIAL_READINESS_INTERVAL_MILLIS;
        int attemptCount = 0;
        Exception lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            attemptCount++;
            try {
                if (isReadinessContractReady()) {
                    return;
                }
            } catch (final IOException ex) {
                lastFailure = ex;
            } catch (final IllegalStateException ex) {
                lastFailure = ex;
                if (isNonRetryableReadinessFailure(ex)) {
                    throw createReadinessException(lastFailure, attemptCount, startTime);
                }
            }
            intervalMillis = waitForNextReadinessAttempt(deadline, intervalMillis);
        }
        throw createReadinessException(lastFailure, attemptCount, startTime);
    }
    
    private long waitForNextReadinessAttempt(final long deadline, final long intervalMillis) throws InterruptedException {
        long remainingMillis = deadline - System.currentTimeMillis();
        if (0L >= remainingMillis) {
            return intervalMillis;
        }
        Thread.sleep(Math.min(intervalMillis, remainingMillis));
        return Math.min(MAX_READINESS_INTERVAL_MILLIS, intervalMillis * 2L);
    }
    
    private boolean isNonRetryableReadinessFailure(final Exception cause) {
        String message = Objects.toString(cause.getMessage(), "");
        return message.contains("HTTP 400") || message.contains("HTTP 401");
    }
    
    private IllegalStateException createReadinessException(final Exception cause, final int attemptCount, final long startTimeMillis) {
        IllegalStateException result = new IllegalStateException(createReadinessFailureMessage(cause, attemptCount, System.currentTimeMillis() - startTimeMillis));
        if (null != cause) {
            result.initCause(cause);
        }
        return result;
    }
    
    private String createReadinessFailureMessage(final Exception cause, final int attemptCount, final long elapsedMillis) {
        String result = String.format("Model service is not ready for `%s` after %d readiness attempt(s), elapsedMillis=%d, timeoutSeconds=%d.",
                config.getModelName(), attemptCount, elapsedMillis, config.getReadyTimeoutSeconds());
        return null == cause || null == cause.getMessage() || cause.getMessage().isBlank()
                ? result
                : result + " Last readiness failure: " + cause.getMessage();
    }
    
    private boolean isModelListReady() throws IOException, InterruptedException {
        HttpResponse<String> response = sendModelListRequest();
        if (200 != response.statusCode()) {
            throw new IllegalStateException(createHttpReadinessFailure("model-list", response));
        }
        return containsModel(response.body());
    }
    
    private boolean isReadinessContractReady() throws IOException, InterruptedException {
        return isModelListReady() && isCompletionProbeReady() && isRequiredToolProbeReady() && isAutoToolProbeReady() && isFinalAnswerProbeReady();
    }
    
    private boolean isCompletionProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = sendReadinessCompletionRequest(List.of(LLMChatMessage.user("Return ok.")), List.of(), "", false);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(createHttpReadinessFailure("completion", response));
        }
        return hasCompletionChoice(response.body());
    }
    
    private boolean isRequiredToolProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = sendReadinessCompletionRequest(List.of(LLMChatMessage.user("Call mcp_read_resource with uri mcp://readiness.")),
                createReadinessTools(), "required", false);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(createHttpReadinessFailure("tool-choice-required", response));
        }
        return hasToolCallChoice(response.body());
    }
    
    private boolean isAutoToolProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = sendReadinessCompletionRequest(List.of(LLMChatMessage.user("Return ok without calling tools.")), createReadinessTools(), "auto", false);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(createHttpReadinessFailure("tool-choice-auto", response));
        }
        return hasCompletionChoice(response.body());
    }
    
    private boolean isFinalAnswerProbeReady() throws IOException, InterruptedException {
        HttpResponse<String> response = sendReadinessCompletionRequest(List.of(LLMChatMessage.user(
                "Respond with exactly {\"status\":\"ok\"} and no Markdown fences.")), List.of(), "none", true);
        if (200 != response.statusCode()) {
            throw new IllegalStateException(createHttpReadinessFailure("tool-choice-none-json", response));
        }
        return hasJsonCompletionChoice(response.body());
    }
    
    private String createHttpReadinessFailure(final String requestKind, final HttpResponse<String> response) {
        return String.format("%s readiness request returned HTTP %d%s.", requestKind, response.statusCode(), createErrorCodeSuffix(response.body()));
    }
    
    private String createErrorCodeSuffix(final String responseBody) {
        try {
            Map<String, Object> payload = parseJsonObject(responseBody, "Failed to parse readiness error response.");
            String errorCode = Objects.toString(castToMap(payload.get("error")).get("code"), "").trim();
            return errorCode.isEmpty() ? "" : String.format(" with error code `%s`", errorCode);
        } catch (final IllegalStateException ignored) {
            return "";
        }
    }
    
    /**
     * Complete.
     *
     * @param messages messages
     * @param tools tools
     * @param toolChoice tool choice
     * @param jsonResponse json response
     * @return LLM chat completion
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     * @throws IllegalStateException model completion response is invalid
     */
    public LLMChatCompletion complete(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                      final String toolChoice, final boolean jsonResponse) throws IOException, InterruptedException {
        HttpResponse<String> response = sendCompletionRequest(createCompletionRequestPayload(messages, tools, toolChoice, jsonResponse, COMPLETION_MAX_TOKENS), config.getRequestTimeoutSeconds());
        if (200 != response.statusCode()) {
            throw new IllegalStateException(String.format("Model completion request failed with status %d%s.", response.statusCode(), createErrorCodeSuffix(response.body())));
        }
        Map<String, Object> payload = parseJsonObject(response.body(), "Failed to parse model completion response.");
        List<Map<String, Object>> choices = castToList(payload.get("choices"));
        Preconditions.checkState(!choices.isEmpty(), "Model completion response does not contain choices.");
        Map<String, Object> message = castToMap(choices.get(0).get("message"));
        Preconditions.checkState(!message.isEmpty(), "Model completion response does not contain one assistant message.");
        return new LLMChatCompletion(Objects.toString(message.get("content"), "").trim(), createToolCalls(message.get("tool_calls")), response.body());
    }
    
    private Map<String, Object> createCompletionRequestPayload(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                                               final String toolChoice, final boolean jsonResponse, final int maxTokens) {
        Map<String, Object> requestPayload = new LinkedHashMap<>(16, 1F);
        requestPayload.put("model", config.getModelName());
        requestPayload.put("messages", createMessages(messages));
        requestPayload.put("stream", false);
        requestPayload.put("temperature", 0);
        requestPayload.put("seed", 1);
        requestPayload.put("reasoning_effort", "none");
        requestPayload.put("max_tokens", maxTokens);
        if (!tools.isEmpty()) {
            requestPayload.put("tools", tools);
        }
        if (!toolChoice.isEmpty()) {
            requestPayload.put("tool_choice", toolChoice);
        }
        if (jsonResponse) {
            requestPayload.put("response_format", Map.of("type", "json_object"));
        }
        return requestPayload;
    }
    
    private HttpResponse<String> sendCompletionRequest(final Map<String, Object> requestPayload, final int timeoutSeconds) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.getChatCompletionsUrl()))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(requestPayload)))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpResponse<String> sendModelListRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.getModelsUrl()))
                .timeout(Duration.ofSeconds(Math.min(config.getRequestTimeoutSeconds(), 30)))
                .header("Authorization", "Bearer " + config.getApiKey())
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    private HttpResponse<String> sendReadinessCompletionRequest(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                                                final String toolChoice, final boolean jsonResponse) throws IOException, InterruptedException {
        return sendCompletionRequest(createCompletionRequestPayload(messages, tools, toolChoice, jsonResponse, READINESS_MAX_TOKENS), Math.min(config.getRequestTimeoutSeconds(), 30));
    }
    
    private boolean containsModel(final String responseBody) {
        Map<String, Object> payload = parseJsonObject(responseBody, "Failed to parse model-list response.");
        return castToList(payload.get("data")).stream().anyMatch(each -> config.getModelName().equals(Objects.toString(each.get("id"), "").trim()));
    }
    
    private boolean hasCompletionChoice(final String responseBody) {
        return !castToList(parseJsonObject(responseBody, "Failed to parse readiness completion response.").get("choices")).isEmpty();
    }
    
    private boolean hasToolCallChoice(final String responseBody) {
        return !createToolCalls(getFirstAssistantMessage(responseBody, "Failed to parse readiness tool-call response.").get("tool_calls")).isEmpty();
    }
    
    private boolean hasJsonCompletionChoice(final String responseBody) {
        String content = Objects.toString(getFirstAssistantMessage(responseBody, "Failed to parse readiness JSON response.").get("content"), "").trim();
        if (content.isEmpty()) {
            return false;
        }
        parseJsonObject(content, "Failed to parse readiness JSON completion content.");
        return true;
    }
    
    private Map<String, Object> getFirstAssistantMessage(final String responseBody, final String errorMessage) {
        List<Map<String, Object>> choices = castToList(parseJsonObject(responseBody, errorMessage).get("choices"));
        return choices.isEmpty() ? Map.of() : castToMap(choices.get(0).get("message"));
    }
    
    private List<Map<String, Object>> createReadinessTools() {
        return List.of(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "mcp_read_resource",
                        "description", "Read one MCP resource.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of("uri", Map.of("type", "string")),
                                "required", List.of("uri")))));
    }
    
    private List<Map<String, Object>> createMessages(final List<LLMChatMessage> messages) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (LLMChatMessage each : messages) {
            Map<String, Object> message = new LinkedHashMap<>(8, 1F);
            message.put("role", each.getRole());
            if (!each.getToolCallId().isEmpty()) {
                message.put("tool_call_id", each.getToolCallId());
            }
            if (!each.getContent().isEmpty()) {
                message.put("content", each.getContent());
            } else if (each.getToolCalls().isEmpty()) {
                message.put("content", "");
            }
            if (!each.getToolCalls().isEmpty()) {
                message.put("tool_calls", createToolCalls(each.getToolCalls()));
            }
            result.add(message);
        }
        return result;
    }
    
    private List<Map<String, Object>> createToolCalls(final List<LLMToolCall> toolCalls) {
        return toolCalls.stream()
                .map(each -> Map.of("id", each.getId(), "type", "function", "function", Map.of("name", each.getName(), "arguments", each.getArgumentsJson()))).collect(Collectors.toList());
    }
    
    private List<LLMToolCall> createToolCalls(final Object rawValue) {
        List<LLMToolCall> result = new LinkedList<>();
        for (Map<String, Object> each : castToList(rawValue)) {
            Map<String, Object> function = castToMap(each.get("function"));
            result.add(new LLMToolCall(
                    Objects.toString(each.get("id"), "").trim(), Objects.toString(function.get("name"), "").trim(), Objects.toString(function.get("arguments"), "").trim()));
        }
        return result;
    }
    
    private List<Map<String, Object>> castToList(final Object value) {
        if (null == value) {
            return List.of();
        }
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> castToMap(final Object value) {
        if (null == value) {
            return Map.of();
        }
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> parseJsonObject(final String responseBody, final String errorMessage) {
        try {
            return JsonUtils.fromJsonString(responseBody, new TypeReference<>() {
            });
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new IllegalStateException(errorMessage, ex);
        }
    }
}
