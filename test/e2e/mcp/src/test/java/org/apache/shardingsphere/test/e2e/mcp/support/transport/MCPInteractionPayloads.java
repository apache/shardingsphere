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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-RPC payload helpers for MCP E2E tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPInteractionPayloads {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * Parse one MCP HTTP or STDIO JSON payload.
     *
     * @param responseBody raw response body
     * @return parsed payload
     * @throws IllegalStateException if the payload cannot be parsed as JSON
     */
    public static Map<String, Object> parseJsonPayload(final String responseBody) {
        try {
            return OBJECT_MAPPER.readValue(normalizeJsonBody(responseBody), new TypeReference<>() {
            });
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to parse MCP response body.", ex);
        }
    }
    
    /**
     * Check whether one JSON-RPC payload contains an error object.
     *
     * @param payload JSON-RPC payload
     * @return whether an error exists
     */
    public static boolean hasJsonRpcError(final Map<String, Object> payload) {
        return payload.containsKey("error");
    }
    
    /**
     * Get the required JSON-RPC result object.
     *
     * @param payload JSON-RPC payload
     * @return result object
     * @throws IllegalStateException when the payload contains an error, or result is absent or is not an object
     */
    public static Map<String, Object> getRequiredJsonRpcResult(final Map<String, Object> payload) {
        if (hasJsonRpcError(payload)) {
            throw new IllegalStateException("MCP JSON-RPC request failed: " + getJsonRpcErrorPayload(payload).get("message"));
        }
        return getRequiredObject(payload, "result");
    }
    
    /**
     * Get the normalized payload for one resources/list response.
     *
     * @param payload JSON-RPC payload
     * @return normalized result or error payload
     */
    public static Map<String, Object> getListResourcesPayload(final Map<String, Object> payload) {
        if (hasJsonRpcError(payload)) {
            return getJsonRpcErrorPayload(payload);
        }
        Map<String, Object> result = getRequiredJsonRpcResult(payload);
        getRequiredObjectList(result, "resources");
        return result;
    }
    
    /**
     * Get one structured content payload from a tools/call response.
     *
     * @param payload JSON-RPC payload
     * @return structured content or normalized error payload
     * @throws IllegalStateException when tools/call response omits required content
     */
    public static Map<String, Object> getToolCallPayload(final Map<String, Object> payload) {
        if (hasJsonRpcError(payload)) {
            return getJsonRpcErrorPayload(payload);
        }
        Map<String, Object> result = getRequiredJsonRpcResult(payload);
        List<Map<String, Object>> contents = getRequiredObjectList(result, "content");
        if (Boolean.TRUE.equals(result.get("isError"))) {
            if (contents.isEmpty() || !"text".equals(contents.getFirst().get("type"))) {
                throw new IllegalStateException("MCP tool error must include JSON text content.");
            }
            return parseJsonText(getRequiredString(contents.getFirst(), "text"));
        }
        return getRequiredObject(result, "structuredContent");
    }
    
    /**
     * Get the first parsed resource payload from a resources/read response.
     *
     * @param payload JSON-RPC payload
     * @return first parsed resource payload or normalized error payload
     * @throws IllegalStateException when the resource content is absent or malformed
     */
    public static Map<String, Object> getFirstResourcePayload(final Map<String, Object> payload) {
        if (hasJsonRpcError(payload)) {
            return getJsonRpcErrorPayload(payload);
        }
        List<Map<String, Object>> contents = getRequiredObjectList(getRequiredJsonRpcResult(payload), "contents");
        if (contents.isEmpty()) {
            throw new IllegalStateException("MCP payload field `contents` must include at least one resource.");
        }
        return parseJsonText(getRequiredString(contents.getFirst(), "text"));
    }
    
    /**
     * Get a normalized error payload from one JSON-RPC response.
     *
     * @param payload JSON-RPC payload
     * @return normalized error payload, or empty map if absent
     */
    public static Map<String, Object> getJsonRpcErrorPayload(final Map<String, Object> payload) {
        return hasJsonRpcError(payload) ? createJsonRpcErrorPayload(payload.get("error")) : Map.of();
    }
    
    /**
     * Get a required object field.
     *
     * @param payload parent payload
     * @param fieldName field name
     * @return object field
     * @throws IllegalStateException when the field is absent or is not an object
     */
    public static Map<String, Object> getRequiredObject(final Map<String, Object> payload, final String fieldName) {
        return getRequiredObjectValue(payload.get(fieldName), fieldName);
    }
    
    /**
     * Get a required object value.
     *
     * @param value raw value
     * @param fieldPath field path for diagnostics
     * @return object value
     * @throws IllegalStateException when the value is not an object
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getRequiredObjectValue(final Object value, final String fieldPath) {
        if (!(value instanceof Map)) {
            throw new IllegalStateException(String.format("MCP payload field `%s` must be an object.", fieldPath));
        }
        return (Map<String, Object>) value;
    }
    
    /**
     * Get a required object-list field.
     *
     * @param payload parent payload
     * @param fieldName field name
     * @return object-list field
     * @throws IllegalStateException when the field is absent, is not a list, or contains a non-object value
     */
    public static List<Map<String, Object>> getRequiredObjectList(final Map<String, Object> payload, final String fieldName) {
        return getRequiredObjectList(payload.get(fieldName), fieldName);
    }
    
    /**
     * Get a required object-list value.
     *
     * @param value raw value
     * @param fieldPath field path for diagnostics
     * @return object-list value
     * @throws IllegalStateException when the value is not a list or contains a non-object value
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getRequiredObjectList(final Object value, final String fieldPath) {
        if (!(value instanceof List)) {
            throw new IllegalStateException(String.format("MCP payload field `%s` must be a list.", fieldPath));
        }
        List<?> values = (List<?>) value;
        for (int index = 0; index < values.size(); index++) {
            getRequiredObjectValue(values.get(index), fieldPath + "[" + index + "]");
        }
        return (List<Map<String, Object>>) values;
    }
    
    /**
     * Get an optional object-list field.
     *
     * @param payload parent payload
     * @param fieldName field name
     * @return object-list field, or an empty list when absent
     * @throws IllegalStateException when the present field is not a list or contains a non-object value
     */
    public static List<Map<String, Object>> getOptionalObjectList(final Map<String, Object> payload, final String fieldName) {
        return payload.containsKey(fieldName) ? getRequiredObjectList(payload, fieldName) : List.of();
    }
    
    private static Map<String, Object> parseJsonText(final String value) {
        try {
            return OBJECT_MAPPER.readValue(value, new TypeReference<>() {
            });
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to parse MCP JSON text payload.", ex);
        }
    }
    
    private static Map<String, Object> createJsonRpcErrorPayload(final Object rawError) {
        Map<String, Object> error = getRequiredObjectValue(rawError, "error");
        Object rawData = error.get("data");
        Map<String, Object> data = rawData instanceof Map ? getRequiredObjectValue(rawData, "error.data") : Map.of();
        Map<String, Object> result = new LinkedHashMap<>(data.size() + 2, 1F);
        result.putAll(data);
        result.put("error_code", "json_rpc_error");
        result.put("message", String.valueOf(error.getOrDefault("message", "Unknown JSON-RPC error.")));
        return result;
    }
    
    private static String getRequiredString(final Map<String, Object> payload, final String fieldName) {
        Object value = payload.get(fieldName);
        if (!(value instanceof String)) {
            throw new IllegalStateException(String.format("MCP payload field `%s` must be a string.", fieldName));
        }
        return (String) value;
    }
    
    private static String normalizeJsonBody(final String responseBody) {
        String result = responseBody.trim();
        if (result.startsWith("{")) {
            return result;
        }
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasDataLine = false;
        for (String each : result.split("\\R")) {
            String line = each.trim();
            if (!line.startsWith("data:")) {
                continue;
            }
            if (hasDataLine) {
                stringBuilder.append(System.lineSeparator());
            }
            stringBuilder.append(line.substring("data:".length()).trim());
            hasDataLine = true;
        }
        return hasDataLine ? stringBuilder.toString() : result;
    }
}
