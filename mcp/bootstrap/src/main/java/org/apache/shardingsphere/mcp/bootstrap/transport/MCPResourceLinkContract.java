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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPResourceLinkContract {
    
    static ResourceLinks createResourceLinks(final Map<String, Object> payload, final int limit) {
        List<ResourceLinkCandidate> candidates = new LinkedList<>();
        collectResourceLinkCandidates(payload, candidates);
        candidates.sort(MCPResourceLinkContract::compareResourceLinkCandidates);
        Map<String, ResourceLinkCandidate> uniqueCandidates = new LinkedHashMap<>(candidates.size(), 1F);
        for (ResourceLinkCandidate each : candidates) {
            uniqueCandidates.putIfAbsent(each.uri(), each);
        }
        List<McpSchema.ResourceLink> links = new LinkedList<>();
        for (ResourceLinkCandidate each : uniqueCandidates.values()) {
            if (links.size() >= limit) {
                break;
            }
            links.add(createResourceLink(each));
        }
        return new ResourceLinks(links, uniqueCandidates.size());
    }
    
    private static void collectResourceLinkCandidates(final Map<?, ?> payload, final List<ResourceLinkCandidate> result) {
        collectResourceLinkFields(payload, result);
        collectRecoveryResourceLinkFields(payload, result);
        collectItemResourceLinkFields(payload, result);
    }
    
    private static void collectRecoveryResourceLinkFields(final Map<?, ?> payload, final List<ResourceLinkCandidate> result) {
        Object recovery = payload.get("recovery");
        if (recovery instanceof Map) {
            collectResourceLinkFields((Map<?, ?>) recovery, result);
        }
    }
    
    private static void collectItemResourceLinkFields(final Map<?, ?> payload, final List<ResourceLinkCandidate> result) {
        Object items = payload.get("items");
        if (!(items instanceof Iterable<?>)) {
            return;
        }
        for (Object each : (Iterable<?>) items) {
            if (each instanceof Map) {
                collectResourceLinkFields((Map<?, ?>) each, result);
            }
        }
    }
    
    private static void collectResourceLinkFields(final Map<?, ?> value, final List<ResourceLinkCandidate> result) {
        collectResourceLinkValue(value.get("resources_to_read"), "resources_to_read", result);
        collectResourceLinkValue(value.get("resource"), "resource", result);
        collectResourceLinkValue(value.get("parent_resource"), "parent_resource", result);
        collectResourceLinkValue(value.get("next_resources"), "next_resources", result);
    }
    
    private static void collectResourceLinkValue(final Object value, final String sourceField, final List<ResourceLinkCandidate> result) {
        if (value instanceof Map) {
            collectResourceLinkHint((Map<?, ?>) value, sourceField, result);
        } else if (value instanceof Iterable<?>) {
            for (Object each : (Iterable<?>) value) {
                if (each instanceof Map) {
                    collectResourceLinkHint((Map<?, ?>) each, sourceField, result);
                }
            }
        }
    }
    
    private static void collectResourceLinkHint(final Map<?, ?> value, final String sourceField, final List<ResourceLinkCandidate> result) {
        if (!isResourceHint(value)) {
            return;
        }
        result.add(new ResourceLinkCandidate(value, Objects.toString(value.get("uri"), ""), resolveSourceField(value, sourceField), result.size()));
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
                .mimeType(MCPTransportPayloadUtils.JSON_CONTENT_TYPE)
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
        result.put(MCPShardingSphereMetadataKeys.RESOURCE_KIND, Objects.toString(candidate.hint().get("resource_kind"), ""));
        result.put(MCPShardingSphereMetadataKeys.PURPOSE, Objects.toString(candidate.hint().get("purpose"), ""));
        result.put(MCPShardingSphereMetadataKeys.SOURCE_FIELD, candidate.sourceField());
        return result;
    }
    
    record ResourceLinks(List<McpSchema.ResourceLink> links, int totalCount) {

        int omittedCount() {
            return Math.max(0, totalCount - links.size());
        }
    }
    
    private record ResourceLinkCandidate(Map<?, ?> hint, String uri, String sourceField, int order) {
    }
}
