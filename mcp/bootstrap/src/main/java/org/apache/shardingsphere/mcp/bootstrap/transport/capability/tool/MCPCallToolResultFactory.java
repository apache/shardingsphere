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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator.ValidationResponse;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorPayload;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;

import java.util.Map;
import java.util.Objects;

final class MCPCallToolResultFactory {
    
    private static final int RESOURCE_LINK_LIMIT = 24;
    
    private final JsonSchemaValidator outputSchemaValidator = new DefaultJsonSchemaValidator();
    
    CallToolResult create(final MCPToolDescriptor descriptor, final MCPSuccessPayload successPayload) {
        Map<String, Object> payload = successPayload.toPayload();
        if (descriptor.getOutputSchema().isEmpty()) {
            return createSuccess(payload);
        }
        ValidationResponse validation = outputSchemaValidator.validate(descriptor.getOutputSchema(), payload);
        return validation.valid()
                ? createSuccess(payload)
                : create(new MCPErrorPayload(String.format("Tool `%s` structuredContent does not match declared outputSchema: %s",
                        descriptor.getName(), Objects.toString(validation.errorMessage(), "validation failed"))));
    }
    
    CallToolResult create(final MCPErrorPayload errorPayload) {
        Map<String, Object> payload = errorPayload.toPayload();
        CallToolResult.Builder result = CallToolResult.builder().addTextContent(JsonUtils.toJsonString(payload)).isError(true);
        appendResourceLinks(payload, result);
        return result.build();
    }
    
    private CallToolResult createSuccess(final Map<String, Object> payload) {
        CallToolResult.Builder result = CallToolResult.builder().structuredContent(payload).addTextContent(JsonUtils.toJsonString(payload)).isError(false);
        appendResourceLinks(payload, result);
        return result.build();
    }
    
    private void appendResourceLinks(final Map<String, Object> payload, final CallToolResult.Builder builder) {
        MCPResourceLinkCandidateCollector.ResourceLinkCandidates candidates = new MCPResourceLinkCandidateCollector(RESOURCE_LINK_LIMIT).collect(payload);
        int emittedCount = 0;
        for (MCPResourceLinkCandidateCollector.ResourceLinkCandidate each : candidates.candidates()) {
            builder.addContent(MCPResourceLinkContract.createResourceLink(each));
            emittedCount++;
        }
        if (0 < candidates.totalCount()) {
            builder.meta(createResourceLinksMeta(emittedCount, candidates.totalCount()));
        }
    }
    
    private Map<String, Object> createResourceLinksMeta(final int emittedCount, final int totalCount) {
        return Map.of(
                MCPShardingSphereMetadataKeys.RESOURCE_LINKS_EMITTED, emittedCount,
                MCPShardingSphereMetadataKeys.RESOURCE_LINKS_OMITTED, Math.max(0, totalCount - emittedCount),
                MCPShardingSphereMetadataKeys.RESOURCE_LINK_LIMIT, RESOURCE_LINK_LIMIT);
    }
}
