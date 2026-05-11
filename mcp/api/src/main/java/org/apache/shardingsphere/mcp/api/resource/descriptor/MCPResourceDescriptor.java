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
import org.apache.shardingsphere.mcp.api.resource.MCPUriTemplateUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MCP resource descriptor.
 */
@Getter
public final class MCPResourceDescriptor {
    
    private final String uriTemplate;
    
    private final String name;
    
    private final String title;
    
    private final String description;
    
    private final String mimeType;
    
    private final List<MCPResourceParameterDescriptor> parameters;
    
    private final MCPResourceAnnotations annotations;
    
    private final String resourceKind;
    
    private final String objectScope;
    
    private final String feature;
    
    private final List<String> relatedTools;
    
    private final List<String> relatedResources;
    
    private final List<String> useBefore;
    
    private final Map<String, Object> meta;
    
    public MCPResourceDescriptor(final String uriTemplate, final String name, final String title, final String description, final String mimeType) {
        this(uriTemplate, name, title, description, mimeType, Collections.emptyList(), MCPResourceAnnotations.EMPTY, Collections.emptyMap());
    }
    
    public MCPResourceDescriptor(final String uriTemplate, final String name, final String title, final String description, final String mimeType,
                                 final List<MCPResourceParameterDescriptor> parameters, final MCPResourceAnnotations annotations, final Map<String, Object> meta) {
        this(uriTemplate, name, title, description, mimeType, parameters, annotations, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), meta);
    }
    
    public MCPResourceDescriptor(final String uriTemplate, final String name, final String title, final String description, final String mimeType,
                                 final List<MCPResourceParameterDescriptor> parameters, final MCPResourceAnnotations annotations, final String resourceKind,
                                 final String objectScope, final String feature, final List<String> relatedTools, final List<String> relatedResources,
                                 final List<String> useBefore, final Map<String, Object> meta) {
        this.uriTemplate = uriTemplate;
        this.name = name;
        this.title = title;
        this.description = description;
        this.mimeType = mimeType;
        this.parameters = null == parameters ? Collections.emptyList() : parameters;
        this.annotations = null == annotations ? MCPResourceAnnotations.EMPTY : annotations;
        this.resourceKind = resourceKind;
        this.objectScope = objectScope;
        this.feature = feature;
        this.relatedTools = null == relatedTools ? Collections.emptyList() : relatedTools;
        this.relatedResources = null == relatedResources ? Collections.emptyList() : relatedResources;
        this.useBefore = null == useBefore ? Collections.emptyList() : useBefore;
        this.meta = null == meta ? Collections.emptyMap() : meta;
    }
    
    /**
     * Judge whether the resource is a URI template.
     *
     * @return true if the resource is a URI template
     */
    public boolean isTemplated() {
        return MCPUriTemplateUtils.isTemplated(uriTemplate);
    }
}
