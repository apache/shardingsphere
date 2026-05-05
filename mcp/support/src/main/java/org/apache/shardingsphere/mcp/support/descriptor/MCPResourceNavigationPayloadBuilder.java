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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
     * Create navigation payload from a resource descriptor and an explicit public parent pattern.
     *
     * @param descriptor resource descriptor
     * @param uriVariables URI variables
     * @param parentUriPattern public parent URI pattern
     * @return navigation payload
     */
    public static Map<String, Object> create(final MCPResourceDescriptor descriptor, final MCPUriVariables uriVariables, final String parentUriPattern) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        Map<String, String> variables = null == uriVariables || null == uriVariables.getVariables() ? Map.of() : uriVariables.getVariables();
        String selfUri = createConcreteUri(descriptor.getUriPattern(), variables);
        if (!selfUri.isEmpty()) {
            result.put("self_uri", selfUri);
        }
        String parentUri = createConcreteUri(parentUriPattern, variables);
        if (!parentUri.isEmpty()) {
            result.put("parent_uri", parentUri);
        }
        return result;
    }
    
    private static String createConcreteUri(final String uriPattern, final Map<String, String> variables) {
        String result = uriPattern;
        for (Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result.contains("{") ? "" : result;
    }
}
