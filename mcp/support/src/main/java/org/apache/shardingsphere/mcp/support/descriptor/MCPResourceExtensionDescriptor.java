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

import java.util.List;

/**
 * ShardingSphere resource extension descriptor.
 */
@Getter
public final class MCPResourceExtensionDescriptor {
    
    private final String uriOrTemplate;
    
    private final List<MCPUriVariableDescriptor> uriVariables;
    
    private final String resourceKind;
    
    private final String objectScope;
    
    private final String feature;
    
    private final List<String> relatedTools;
    
    private final List<String> relatedResources;
    
    private final List<String> useBefore;
    
    public MCPResourceExtensionDescriptor(final String uriOrTemplate, final List<MCPUriVariableDescriptor> uriVariables, final String resourceKind, final String objectScope,
                                          final String feature, final List<String> relatedTools, final List<String> relatedResources, final List<String> useBefore) {
        this.uriOrTemplate = uriOrTemplate;
        this.uriVariables = uriVariables;
        this.resourceKind = resourceKind;
        this.objectScope = objectScope;
        this.feature = feature;
        this.relatedTools = relatedTools;
        this.relatedResources = relatedResources;
        this.useBefore = useBefore;
    }
}
