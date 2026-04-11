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

package org.apache.shardingsphere.test.e2e.mcp.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

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

/**
 * OpenAI-compatible LLM chat client.
 */
public final class LLMChatModelClient implements LLMChatClient {
    
    private final LLME2EConfiguration config;
    
    private final HttpClient httpClient;
    
    public LLMChatModelClient(final LLME2EConfiguration config, final HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }
    
    @Override
    public void waitUntilReady() throws InterruptedException {
        long deadline = System.currentTimeMillis() + config.readyTimeoutSeconds() * 1000L;
        IOException lastException = null;
        IllegalStateException lastStateException = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpResponse<String> response = sendModelListRequest();
                if (200 == response.statusCode() && containsModel(response.body())) {
                    return;
                }
            } catch (final IOException ex) {
                lastException = ex;
            } catch (final IllegalStateException ex) {
                lastStateException = ex;
            }
            Thread.sleep(2000L);
        }
        IllegalStateException result = new IllegalStateException(String.format("Model service is not ready for `%s`.", config.modelName()));
        if (null != lastException) {
            result.initCause(lastException);
        } else if (null != lastStateException) {
            result.initCause(lastStateException);
        }
        throw result;
    }
    
    @Override
    public LLMChatCompletion complete(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                      final String toolChoice, final boolean jsonResponse) throws IOException, InterruptedException {
        Map<String, Object> requestPayload = new LinkedHashMap<>(16, 1F);
        requestPayload.put("model", config.modelName());
        requestPayload.put("messages", createMessages(messages));
        requestPayload.put("stream", false);
        requestPayload.put("temperature", 0);
        requestPayload.put("seed", 1);
        requestPayload.put("reasoning_effort", "none");
        requestPayload.put("max_tokens", 512);
        if (!tools.isEmpty()) {
            requestPayload.put("tools", tools);
        }
        if (!toolChoice.isEmpty()) {
            requestPayload.put("tool_choice", toolChoice);
        }
        if (jsonResponse) {
            requestPayload.put("response_format", Map.of("type", "json_object"));
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.getChatCompletionsUrl()))
                .timeout(Duration.ofSeconds(config.requestTimeoutSeconds()))
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(requestPayload)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (200 != response.statusCode()) {
            throw new IllegalStateException("Model completion request failed with status " + response.statusCode() + ".");
        }
        Map<String, Object> payload = parseJsonObject(response.body(), "Failed to parse model completion response.");
        List<Map<String, Object>> choices = castToList(payload.get("choices"));
        if (choices.isEmpty()) {
            throw new IllegalStateException("Model completion response does not contain choices.");
        }
        Map<String, Object> message = castToMap(choices.get(0).get("message"));
        if (message.isEmpty()) {
            throw new IllegalStateException("Model completion response does not contain one assistant message.");
        }
        return new LLMChatCompletion(Objects.toString(message.get("content"), "").trim(), createToolCalls(message.get("tool_calls")), response.body());
    }
    
    private HttpResponse<String> sendModelListRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.getModelsUrl()))
                .timeout(Duration.ofSeconds(Math.min(config.requestTimeoutSeconds(), 30)))
                .header("Authorization", "Bearer " + config.apiKey())
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    private boolean containsModel(final String responseBody) {
        Map<String, Object> payload = parseJsonObject(responseBody, "Failed to parse model-list response.");
        List<Map<String, Object>> data = castToList(payload.get("data"));
        for (Map<String, Object> each : data) {
            if (config.modelName().equals(Objects.toString(each.get("id"), "").trim())) {
                return true;
            }
        }
        return false;
    }
    
    private List<Map<String, Object>> createMessages(final List<LLMChatMessage> messages) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (LLMChatMessage each : messages) {
            Map<String, Object> message = new LinkedHashMap<>(8, 1F);
            message.put("role", each.role());
            if (!each.toolCallId().isEmpty()) {
                message.put("tool_call_id", each.toolCallId());
            }
            if (!each.content().isEmpty()) {
                message.put("content", each.content());
            } else if (each.toolCalls().isEmpty()) {
                message.put("content", "");
            }
            if (!each.toolCalls().isEmpty()) {
                message.put("tool_calls", createToolCalls(each.toolCalls()));
            }
            result.add(message);
        }
        return result;
    }
    
    private List<Map<String, Object>> createToolCalls(final List<LLMToolCall> toolCalls) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (LLMToolCall each : toolCalls) {
            result.add(Map.of("id", each.id(), "type", "function",
                    "function", Map.of("name", each.name(), "arguments", each.argumentsJson())));
        }
        return result;
    }
    
    private List<LLMToolCall> createToolCalls(final Object rawValue) {
        List<LLMToolCall> result = new LinkedList<>();
        for (Map<String, Object> each : castToList(rawValue)) {
            Map<String, Object> function = castToMap(each.get("function"));
            result.add(new LLMToolCall(Objects.toString(each.get("id"), "").trim(), Objects.toString(function.get("name"), "").trim(),
                    Objects.toString(function.get("arguments"), "").trim()));
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
        } catch (final Exception ex) {
            throw new IllegalStateException(errorMessage, ex);
        }
    }
}
