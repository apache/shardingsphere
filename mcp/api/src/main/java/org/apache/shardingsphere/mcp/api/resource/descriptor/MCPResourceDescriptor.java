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

package org.apache.shardingsphere.mcp.api.resource.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPIcon;

import java.util.List;
import java.util.Map;

/**
 * MCP resource descriptor.
 */
@RequiredArgsConstructor
@Getter
public final class MCPResourceDescriptor {
    
    private final String uriTemplate;
    
    private final String name;
    
    private final String title;
    
    private final String description;
    
    private final List<MCPIcon> icons;
    
    private final String mimeType;
    
    private final MCPAnnotations annotations;
    
    private final Map<String, Object> meta;
    
    /**
     * Judge whether the resource is a URI template.
     *
     * @return true if the resource is a URI template
     */
    public boolean isTemplated() {
        return null != uriTemplate && uriTemplate.contains("{");
    }
}
