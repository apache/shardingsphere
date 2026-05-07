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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MCP transport payload utility methods.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPTransportPayloadUtils {

    public static final String JSON_CONTENT_TYPE = "application/json";

    /**
     * Create MCP tool result for a structured payload.
     *
     * @param response MCP response
     * @return MCP tool result
     */
    public static McpSchema.CallToolResult createCallToolResult(final MCPResponse response) {
        return createCallToolResult(response.toPayload(), response instanceof MCPErrorResponse);
    }

    /**
     * Create MCP tool result for a structured payload.
     *
     * @param payload MCP payload
     * @return MCP tool result
     */
    public static McpSchema.CallToolResult createCallToolResult(final Map<String, Object> payload) {
        return createCallToolResult(payload, false);
    }

    private static McpSchema.CallToolResult createCallToolResult(final Map<String, Object> payload, final boolean error) {
        CallToolResult.Builder result = CallToolResult.builder().structuredContent(payload).addTextContent(JsonUtils.toJsonString(payload)).isError(error);
        for (McpSchema.ResourceLink each : createResourceLinks(payload)) {
            result.addContent(each);
        }
        return result.build();
    }

    private static List<McpSchema.ResourceLink> createResourceLinks(final Map<String, Object> payload) {
        Map<String, McpSchema.ResourceLink> result = new LinkedHashMap<>();
        collectResourceLinks(payload, result);
        return new LinkedList<>(result.values());
    }

    private static void collectResourceLinks(final Object value, final Map<String, McpSchema.ResourceLink> result) {
        if (value instanceof Map<?, ?>) {
            collectResourceLinksFromMap((Map<?, ?>) value, result);
        } else if (value instanceof Iterable<?>) {
            for (Object each : (Iterable<?>) value) {
                collectResourceLinks(each, result);
            }
        }
    }

    private static void collectResourceLinksFromMap(final Map<?, ?> value, final Map<String, McpSchema.ResourceLink> result) {
        if (isResourceHint(value)) {
            String uri = Objects.toString(value.get("uri"), "");
            result.putIfAbsent(uri, createResourceLink(value, uri));
        }
        for (Object each : value.values()) {
            collectResourceLinks(each, result);
        }
    }

    private static boolean isResourceHint(final Map<?, ?> value) {
        return value.containsKey("uri") && value.containsKey("resource_kind") && !Objects.toString(value.get("uri"), "").isBlank();
    }

    private static McpSchema.ResourceLink createResourceLink(final Map<?, ?> value, final String uri) {
        return McpSchema.ResourceLink.builder()
                .name(resolveResourceLinkName(uri))
                .title(Objects.toString(value.get("resource_kind"), "resource"))
                .uri(uri)
                .description(Objects.toString(value.get("reason"), "Read this ShardingSphere MCP resource."))
                .mimeType(JSON_CONTENT_TYPE)
                .meta(createResourceLinkMeta(value))
                .build();
    }

    private static String resolveResourceLinkName(final String uri) {
        int separatorIndex = uri.lastIndexOf('/');
        if (separatorIndex < 0 || separatorIndex == uri.length() - 1) {
            return uri;
        }
        return uri.substring(separatorIndex + 1);
    }

    private static Map<String, Object> createResourceLinkMeta(final Map<?, ?> value) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("resource_kind", Objects.toString(value.get("resource_kind"), ""));
        result.put("purpose", Objects.toString(value.get("purpose"), ""));
        result.put("source_field", Objects.toString(value.get("source_field"), ""));
        return result;
    }

    /**
     * Create MCP resource result for a structured payload.
     *
     * @param uri resource URI
     * @param payload MCP payload
     * @return MCP resource result
     */
    public static McpSchema.ReadResourceResult createReadResourceResult(final String uri, final Map<String, Object> payload) {
        return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(uri, JSON_CONTENT_TYPE, JsonUtils.toJsonString(payload))));
    }
}
