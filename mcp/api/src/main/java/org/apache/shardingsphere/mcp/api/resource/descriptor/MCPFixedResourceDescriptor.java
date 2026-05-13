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
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPIcon;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MCP fixed resource descriptor.
 */
@Getter
public final class MCPFixedResourceDescriptor implements MCPResourceDescriptor {
    
    private final String uri;
    
    private final String name;
    
    private final String title;
    
    private final String description;
    
    private final List<MCPIcon> icons;
    
    private final String mimeType;
    
    private final MCPAnnotations annotations;
    
    private final Long size;
    
    private final Map<String, Object> meta;
    
    public MCPFixedResourceDescriptor(final String uri, final String name, final String title, final String description, final List<MCPIcon> icons,
                                      final String mimeType, final MCPAnnotations annotations, final Long size, final Map<String, Object> meta) {
        this.uri = uri;
        this.name = name;
        this.title = title;
        this.description = description;
        this.icons = null == icons ? Collections.emptyList() : icons;
        this.mimeType = mimeType;
        this.annotations = null == annotations ? MCPAnnotations.EMPTY : annotations;
        this.size = size;
        this.meta = null == meta ? Collections.emptyMap() : meta;
    }
    
}
