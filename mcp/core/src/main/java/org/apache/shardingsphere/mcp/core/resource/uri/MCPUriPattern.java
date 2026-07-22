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

package org.apache.shardingsphere.mcp.core.resource.uri;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;

import java.util.Optional;

/**
 * MCP URI pattern.
 */
public final class MCPUriPattern {
    
    @Getter
    private final String pattern;
    
    private final MCPUriTemplate uriTemplate;
    
    public MCPUriPattern(final String pattern) {
        this.pattern = pattern;
        uriTemplate = new MCPUriTemplate(pattern);
    }
    
    /**
     * Parse URI.
     *
     * @param uri URI
     * @return parsed variables when present
     */
    public Optional<MCPResourceURIVariables> parse(final String uri) {
        return uriTemplate.parse(uri);
    }
    
    /**
     * Determine whether current pattern overlaps with another pattern.
     *
     * @param other other pattern
     * @return overlap or not
     */
    public boolean overlaps(final MCPUriPattern other) {
        return uriTemplate.overlaps(other.uriTemplate);
    }
}
