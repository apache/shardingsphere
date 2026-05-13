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

package org.apache.shardingsphere.mcp.support.completion;

import lombok.Getter;

import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP completion provider result.
 */
@Getter
public final class MCPCompletionProviderResult {
    
    private final List<MCPCompletionCandidate> candidates;
    
    private final Map<String, Object> inferredContextArguments;
    
    public MCPCompletionProviderResult(final Collection<MCPCompletionCandidate> candidates) {
        this(candidates, Map.of());
    }
    
    public MCPCompletionProviderResult(final Collection<MCPCompletionCandidate> candidates, final Map<String, Object> inferredContextArguments) {
        this.candidates = null == candidates ? List.of() : List.copyOf(candidates);
        this.inferredContextArguments = null == inferredContextArguments ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(inferredContextArguments));
    }
    
    /**
     * Create empty completion provider result.
     *
     * @return empty completion provider result
     */
    public static MCPCompletionProviderResult empty() {
        return new MCPCompletionProviderResult(List.of());
    }
}
