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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPResourceLinkContract {
    
    static ResourceLinks createResourceLinks(final Map<String, Object> payload, final int limit) {
        MCPResourceLinkCandidateCollector.ResourceLinkCandidates candidates = new MCPResourceLinkCandidateCollector(limit).collect(payload);
        List<McpSchema.ResourceLink> links = new LinkedList<>();
        for (MCPResourceLinkCandidateCollector.ResourceLinkCandidate each : candidates.candidates()) {
            links.add(createResourceLink(each));
        }
        return new ResourceLinks(links, candidates.totalCount());
    }
    
    private static McpSchema.ResourceLink createResourceLink(final MCPResourceLinkCandidateCollector.ResourceLinkCandidate candidate) {
        return McpSchema.ResourceLink.builder()
                .name(resolveResourceLinkName(candidate.uri()))
                .title(candidate.title())
                .uri(candidate.uri())
                .description(candidate.description())
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
    
    private static Map<String, Object> createResourceLinkMeta(final MCPResourceLinkCandidateCollector.ResourceLinkCandidate candidate) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put(MCPShardingSphereMetadataKeys.RESOURCE_KIND, candidate.resourceKind());
        result.put(MCPShardingSphereMetadataKeys.PURPOSE, candidate.purpose());
        result.put(MCPShardingSphereMetadataKeys.SOURCE_FIELD, candidate.sourceField());
        return result;
    }
    
    record ResourceLinks(List<McpSchema.ResourceLink> links, int totalCount) {

        int omittedCount() {
            return Math.max(0, totalCount - links.size());
        }
    }
}
