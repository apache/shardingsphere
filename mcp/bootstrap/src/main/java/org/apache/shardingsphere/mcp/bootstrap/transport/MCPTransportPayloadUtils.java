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
import java.util.Map.Entry;
import java.util.Objects;

/**
 * MCP transport payload utility methods.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPTransportPayloadUtils {

    public static final String JSON_CONTENT_TYPE = "application/json";

    private static final int RESOURCE_LINK_LIMIT = 24;

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
        ResourceLinks resourceLinks = createResourceLinks(payload);
        for (McpSchema.ResourceLink each : resourceLinks.links()) {
            result.addContent(each);
        }
        if (0 < resourceLinks.totalCount()) {
            result.meta(Map.of("resource_links_emitted", resourceLinks.links().size(), "resource_links_omitted", resourceLinks.omittedCount(), "resource_link_limit", RESOURCE_LINK_LIMIT));
        }
        return result.build();
    }

    private static ResourceLinks createResourceLinks(final Map<String, Object> payload) {
        List<ResourceLinkCandidate> candidates = new LinkedList<>();
        collectResourceLinkCandidates(payload, "", candidates);
        candidates.sort(MCPTransportPayloadUtils::compareResourceLinkCandidates);
        Map<String, ResourceLinkCandidate> uniqueCandidates = new LinkedHashMap<>(candidates.size(), 1F);
        for (ResourceLinkCandidate each : candidates) {
            uniqueCandidates.putIfAbsent(each.uri(), each);
        }
        List<McpSchema.ResourceLink> links = new LinkedList<>();
        for (ResourceLinkCandidate each : uniqueCandidates.values()) {
            if (links.size() >= RESOURCE_LINK_LIMIT) {
                break;
            }
            links.add(createResourceLink(each));
        }
        return new ResourceLinks(links, uniqueCandidates.size());
    }

    private static void collectResourceLinkCandidates(final Object value, final String sourceField, final List<ResourceLinkCandidate> result) {
        if (value instanceof Map<?, ?>) {
            collectResourceLinksFromMap((Map<?, ?>) value, sourceField, result);
        } else if (value instanceof Iterable<?>) {
            for (Object each : (Iterable<?>) value) {
                collectResourceLinkCandidates(each, sourceField, result);
            }
        }
    }

    private static void collectResourceLinksFromMap(final Map<?, ?> value, final String sourceField, final List<ResourceLinkCandidate> result) {
        if (isResourceHint(value)) {
            String uri = Objects.toString(value.get("uri"), "");
            result.add(new ResourceLinkCandidate(value, uri, resolveSourceField(value, sourceField), result.size()));
        }
        for (Entry<?, ?> entry : value.entrySet()) {
            collectResourceLinkCandidates(entry.getValue(), Objects.toString(entry.getKey(), sourceField), result);
        }
    }

    private static String resolveSourceField(final Map<?, ?> value, final String sourceField) {
        String result = Objects.toString(value.get("source_field"), "");
        return result.isEmpty() ? sourceField : result;
    }

    private static int compareResourceLinkCandidates(final ResourceLinkCandidate left, final ResourceLinkCandidate right) {
        int result = Integer.compare(resolveResourceLinkPriority(left.sourceField()), resolveResourceLinkPriority(right.sourceField()));
        return 0 == result ? Integer.compare(left.order(), right.order()) : result;
    }

    private static int resolveResourceLinkPriority(final String sourceField) {
        if ("resources_to_read".equals(sourceField)) {
            return 0;
        }
        if ("resource".equals(sourceField)) {
            return 1;
        }
        if ("parent_resource".equals(sourceField)) {
            return 2;
        }
        if ("next_resources".equals(sourceField)) {
            return 3;
        }
        return 4;
    }

    private static boolean isResourceHint(final Map<?, ?> value) {
        return value.containsKey("uri") && value.containsKey("resource_kind") && !Objects.toString(value.get("uri"), "").isBlank();
    }

    private static McpSchema.ResourceLink createResourceLink(final ResourceLinkCandidate candidate) {
        return McpSchema.ResourceLink.builder()
                .name(resolveResourceLinkName(candidate.uri()))
                .title(Objects.toString(candidate.hint().get("resource_kind"), "resource"))
                .uri(candidate.uri())
                .description(Objects.toString(candidate.hint().get("reason"), "Read this ShardingSphere MCP resource."))
                .mimeType(JSON_CONTENT_TYPE)
                .meta(createResourceLinkMeta(candidate))
                .build();
    }

    private static String resolveResourceLinkName(final String uri) {
        int separatorIndex = uri.lastIndexOf('/');
        if (separatorIndex < 0 || separatorIndex == uri.length() - 1) {
            return uri;
        }
        return uri.substring(separatorIndex + 1);
    }

    private static Map<String, Object> createResourceLinkMeta(final ResourceLinkCandidate candidate) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("resource_kind", Objects.toString(candidate.hint().get("resource_kind"), ""));
        result.put("purpose", Objects.toString(candidate.hint().get("purpose"), ""));
        result.put("source_field", candidate.sourceField());
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

    private record ResourceLinkCandidate(Map<?, ?> hint, String uri, String sourceField, int order) {
    }

    private record ResourceLinks(List<McpSchema.ResourceLink> links, int totalCount) {

        private int omittedCount() {
            return Math.max(0, totalCount - links.size());
        }
    }
}
