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

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPListResponsePayloadAdapter {
    
    static Map<String, Object> adaptResources(final McpJsonMapper jsonMapper, final McpSchema.ListResourcesResult listResult,
                                              final Map<String, Map<String, Object>> extraFields) throws IOException {
        return adapt(jsonMapper, listResult, "resources", "uri", extraFields);
    }
    
    static Map<String, Object> adaptResourceTemplates(final McpJsonMapper jsonMapper, final McpSchema.ListResourceTemplatesResult listResult,
                                                      final Map<String, Map<String, Object>> extraFields) throws IOException {
        return adapt(jsonMapper, listResult, "resourceTemplates", "uriTemplate", extraFields);
    }
    
    static Map<String, Object> adaptTools(final McpJsonMapper jsonMapper, final McpSchema.ListToolsResult listResult,
                                          final Map<String, Map<String, Object>> extraFields) throws IOException {
        return adapt(jsonMapper, listResult, "tools", "name", extraFields);
    }
    
    static Map<String, Object> adaptPrompts(final McpJsonMapper jsonMapper, final McpSchema.ListPromptsResult listResult,
                                            final Map<String, Map<String, Object>> extraFields) throws IOException {
        return adapt(jsonMapper, listResult, "prompts", "name", extraFields);
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> adapt(final McpJsonMapper jsonMapper, final Object listResult, final String itemsFieldName, final String identifierFieldName,
                                             final Map<String, Map<String, Object>> extraFields) throws IOException {
        Map<String, Object> resultPayload = jsonMapper.readValue(jsonMapper.writeValueAsString(listResult), Map.class);
        Object items = resultPayload.get(itemsFieldName);
        if (!(items instanceof List)) {
            return resultPayload;
        }
        for (Object each : (List<?>) items) {
            if (each instanceof Map) {
                appendExtraFields((Map<String, Object>) each, identifierFieldName, extraFields);
            }
        }
        return resultPayload;
    }
    
    private static void appendExtraFields(final Map<String, Object> item, final String identifierFieldName, final Map<String, Map<String, Object>> extraFields) {
        Object identifier = item.get(identifierFieldName);
        if (null == identifier || !extraFields.containsKey(identifier.toString())) {
            return;
        }
        item.putAll(extraFields.get(identifier.toString()));
    }
}
