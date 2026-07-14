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

package org.apache.shardingsphere.mcp.support.protocol;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * MCP completion action.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public final class MCPCompletionAction {
    
    @Builder.Default
    private final String referenceType = "";
    
    @Builder.Default
    private final String reference = "";
    
    @Builder.Default
    private final String argumentName = "";
    
    @Builder.Default
    private final String argumentPrefix = "";
    
    @Builder.Default
    private final Map<String, ?> contextArguments = Map.of();
    
    @Builder.Default
    private final Collection<String> missingContextArguments = List.of();
    
    @Builder.Default
    private final String resumeTargetType = "";
    
    @Builder.Default
    private final String resumeTarget = "";
    
    @Builder.Default
    private final Map<String, ?> resumeArguments = Map.of();
    
    @Builder.Default
    private final String reason = "";
}
