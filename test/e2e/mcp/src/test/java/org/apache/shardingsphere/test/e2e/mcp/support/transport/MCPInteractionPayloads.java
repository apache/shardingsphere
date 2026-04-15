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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON-RPC payload helpers for MCP E2E tests.
 */
public final class MCPInteractionPayloads {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private MCPInteractionPayloads() {
    }
    
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
     * Get the JSON-RPC result object.
     *
     * @param payload JSON-RPC payload
     * @return result object, or empty map if absent
     */
    public static Map<String, Object> getJsonRpcResult(final Map<String, Object> payload) {
        return payload.containsKey("result") ? castToMap(payload.get("result")) : Map.of();
    }
    
    /**
     * Get the MCP content list from one JSON-RPC payload.
     *
     * @param payload JSON-RPC payload
     * @return content list, or empty list if absent
     */
    public static List<Map<String, Object>> getResultContents(final Map<String, Object> payload) {
        List<Map<String, Object>> result = castToList(getJsonRpcResult(payload).get("content"));
        return null == result ? List.of() : result;
    }
    
    /**
     * Get the normalized payload for one resources/list response.
     *
     * @param payload JSON-RPC payload
     * @return normalized result or error payload
     */
    public static Map<String, Object> getListResourcesPayload(final Map<String, Object> payload) {
        return hasJsonRpcError(payload) ? getJsonRpcErrorPayload(payload) : getJsonRpcResult(payload);
    }
    
    /**
     * Get one structured content payload from a tools/call response.
     *
     * @param payload JSON-RPC payload
     * @return structured content or normalized error payload
     */
    public static Map<String, Object> getStructuredContent(final Map<String, Object> payload) {
        if (hasJsonRpcError(payload)) {
            return getJsonRpcErrorPayload(payload);
        }
        Map<String, Object> result = getJsonRpcResult(payload);
        if (result.containsKey("structuredContent")) {
            return castToMap(result.get("structuredContent"));
        }
        List<Map<String, Object>> contents = getResultContents(payload);
        return contents.isEmpty() ? Map.of() : parseJsonText(contents.get(0).get("text"));
    }
    
    /**
     * Get the first parsed resource payload from a resources/read response.
     *
     * @param payload JSON-RPC payload
     * @return first parsed resource payload or normalized error payload
     */
    public static Map<String, Object> getFirstResourcePayload(final Map<String, Object> payload) {
        if (hasJsonRpcError(payload)) {
            return getJsonRpcErrorPayload(payload);
        }
        List<Map<String, Object>> contents = castToList(getJsonRpcResult(payload).get("contents"));
        return null == contents || contents.isEmpty() ? Map.of() : parseJsonText(contents.get(0).get("text"));
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
     * Cast one value to a string-object map.
     *
     * @param value raw value
     * @return converted map
     */
    public static Map<String, Object> castToMap(final Object value) {
        return OBJECT_MAPPER.convertValue(value, new TypeReference<>() {
        });
    }
    
    /**
     * Cast one value to a list of string-object maps.
     *
     * @param value raw value
     * @return converted list
     */
    public static List<Map<String, Object>> castToList(final Object value) {
        return OBJECT_MAPPER.convertValue(value, new TypeReference<>() {
        });
    }
    
    private static Map<String, Object> parseJsonText(final Object value) {
        try {
            return OBJECT_MAPPER.readValue(String.valueOf(value), new TypeReference<>() {
            });
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to parse MCP JSON text payload.", ex);
        }
    }
    
    private static Map<String, Object> createJsonRpcErrorPayload(final Object rawError) {
        Map<String, Object> error = castToMap(rawError);
        return Map.of(
                "error_code", "json_rpc_error",
                "message", String.valueOf(error.getOrDefault("message", "Unknown JSON-RPC error.")));
    }
    
    private static String normalizeJsonBody(final String responseBody) {
        String result = responseBody.trim();
        if (result.startsWith("{") || result.startsWith("[")) {
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
