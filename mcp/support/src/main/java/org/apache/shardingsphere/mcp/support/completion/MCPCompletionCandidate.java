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

import java.time.Instant;
import java.util.Objects;

/**
 * MCP completion candidate.
 */
@Getter
public final class MCPCompletionCandidate {
    
    private final String value;
    
    private final String label;
    
    private final String source;
    
    private final Instant updateTime;
    
    private final String rankingReason;
    
    public MCPCompletionCandidate(final String value, final String label, final String source) {
        this(value, label, source, null, "");
    }
    
    public MCPCompletionCandidate(final String value, final String label, final String source, final Instant updateTime) {
        this(value, label, source, updateTime, "");
    }
    
    public MCPCompletionCandidate(final String value, final String label, final String source, final Instant updateTime, final String rankingReason) {
        this.value = value;
        this.label = label;
        this.source = source;
        this.updateTime = updateTime;
        this.rankingReason = Objects.toString(rankingReason, "");
    }
}
