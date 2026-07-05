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

package org.apache.shardingsphere.mcp.support.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShardingSphere MCP resource metadata descriptor.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingSphereMCPResourceMetadata {
    
    private final String uriTemplate;
    
    private final Collection<MCPUriVariableDescriptor> uriVariables;
    
    private final String resourceKind;
    
    private final String objectScope;
    
    private final String feature;
    
    private final Collection<String> relatedTools;
    
    private final Collection<String> relatedResources;
    
    private final Collection<String> useBefore;
    
    /**
     * Convert to protocol-facing MCP resource metadata.
     *
     * @return protocol-facing MCP resource metadata
     */
    public Map<String, Object> toMeta() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        putIfPresent(result, MCPShardingSphereMetadataKeys.RESOURCE_KIND, resourceKind);
        putIfPresent(result, MCPShardingSphereMetadataKeys.OBJECT_SCOPE, objectScope);
        putIfPresent(result, MCPShardingSphereMetadataKeys.FEATURE, feature);
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.RELATED_TOOLS, relatedTools);
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, relatedResources);
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.USE_BEFORE, useBefore);
        if (!uriVariables.isEmpty()) {
            result.put(MCPShardingSphereMetadataKeys.URI_VARIABLES, uriVariables.stream().map(this::toUriVariableMeta).toList());
        }
        return result;
    }
    
    private void putIfPresent(final Map<String, Object> target, final String key, final Object value) {
        if (null != value) {
            target.put(key, value);
        }
    }
    
    private void putIfNotEmpty(final Map<String, Object> target, final String key, final Collection<?> values) {
        if (!values.isEmpty()) {
            target.put(key, values);
        }
    }
    
    private Map<String, Object> toUriVariableMeta(final MCPUriVariableDescriptor uriVariable) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("name", uriVariable.getName());
        result.put("title", uriVariable.getTitle());
        result.put("description", uriVariable.getDescription());
        result.put("required", uriVariable.isRequired());
        result.put("scope", uriVariable.getScope());
        return result;
    }
}
