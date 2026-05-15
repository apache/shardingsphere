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

package org.apache.shardingsphere.mcp.api.tool.descriptor;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Map;

/**
 * MCP tool descriptor.
 */
@Getter
public final class MCPToolDescriptor {
    
    private final String name;
    
    private final String title;
    
    private final String description;
    
    private final Map<String, Object> inputSchema;
    
    private final Map<String, Object> outputSchema;
    
    private final MCPToolAnnotations annotations;
    
    private final Map<String, Object> meta;
    
    public MCPToolDescriptor(final String name, final String title, final String description, final Map<String, Object> inputSchema,
                             final Map<String, Object> outputSchema, final MCPToolAnnotations annotations, final Map<String, Object> meta) {
        ShardingSpherePreconditions.checkNotNull(annotations, () -> new IllegalArgumentException(String.format("Tool `%s` MCP annotations are required.", name)));
        this.name = name;
        this.title = title;
        this.description = description;
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
        this.annotations = annotations;
        this.meta = meta;
    }
}
