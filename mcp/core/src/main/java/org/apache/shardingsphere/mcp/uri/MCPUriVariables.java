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

package org.apache.shardingsphere.mcp.uri;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Map;

/**
 * MCP URI variables.
 */
@RequiredArgsConstructor
@Getter
public final class MCPUriVariables {
    
    private final Map<String, String> variables;
    
    /**
     * Get one URI variable.
     *
     * @param variableName variable name
     * @return variable value
     */
    public String getRequired(final String variableName) {
        String result = variables.get(variableName);
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException(String.format("Missing URI variable `%s`.", variableName)));
        return result;
    }
}
