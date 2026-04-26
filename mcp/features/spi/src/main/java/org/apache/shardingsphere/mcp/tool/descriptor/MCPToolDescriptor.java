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

package org.apache.shardingsphere.mcp.tool.descriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP tool descriptor.
 */
@RequiredArgsConstructor
@Getter
public final class MCPToolDescriptor {
    
    private final String name;
    
    private final List<MCPToolFieldDefinition> fields;
    
    /**
     * Get title.
     *
     * @return title
     */
    public String getTitle() {
        return Arrays.stream(name.split("_")).filter(each -> !each.isEmpty()).map(each -> Character.toUpperCase(each.charAt(0)) + each.substring(1)).collect(Collectors.joining(" "));
    }
    
    /**
     * Get description.
     *
     * @return description
     */
    public String getDescription() {
        return "ShardingSphere MCP tool: " + name;
    }
}
