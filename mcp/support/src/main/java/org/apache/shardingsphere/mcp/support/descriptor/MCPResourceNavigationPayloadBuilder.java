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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MCP resource navigation payload builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPResourceNavigationPayloadBuilder {
    
    /**
     * Create navigation payload from a resource descriptor.
     *
     * @param descriptor resource descriptor
     * @param uriVariables URI variables
     * @return navigation payload
     */
    public static Map<String, Object> create(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables) {
        return create(descriptor, uriVariables, "");
    }
    
    /**
     * Create navigation payload from a resource descriptor and an explicit public parent URI template.
     *
     * @param descriptor resource descriptor
     * @param uriVariables URI variables
     * @param parentUriTemplate public parent URI template
     * @return navigation payload
     */
    public static Map<String, Object> create(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables, final String parentUriTemplate) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        new MCPUriTemplate(descriptor.getUriTemplate()).expandIfComplete(uriVariables).ifPresent(optional -> result.put("self_uri", optional));
        Optional<String> parentUri = new MCPUriTemplate(parentUriTemplate).expandIfComplete(uriVariables);
        parentUri.filter(optional -> !optional.isEmpty()).ifPresent(optional -> result.put(MCPPayloadFieldNames.PARENT_RESOURCE, createParentResourceHint(optional)));
        return result;
    }
    
    private static Map<String, Object> createParentResourceHint(final String uri) {
        return MCPResourceHintUtils.create(uri, resolveResourceKind(uri), "inspect_parent", "Read the parent resource.", MCPPayloadFieldNames.PARENT_RESOURCE);
    }
    
    private static String resolveResourceKind(final String uri) {
        if (uri.contains("/rules")) {
            return "rule";
        }
        if (uri.contains("/algorithms")) {
            return "algorithm";
        }
        if (uri.contains("/columns")) {
            return "column";
        }
        if (uri.contains("/indexes")) {
            return "index";
        }
        return "resource";
    }
}
