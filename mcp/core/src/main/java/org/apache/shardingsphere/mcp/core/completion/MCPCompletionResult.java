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

package org.apache.shardingsphere.mcp.core.completion;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP completion result.
 */
@Getter
public final class MCPCompletionResult {

    private final List<String> values;

    private final int total;

    private final boolean hasMore;

    private final Map<String, Object> meta;

    public MCPCompletionResult(final List<String> values, final int total, final boolean hasMore, final Map<String, Object> meta) {
        this.values = List.copyOf(values);
        this.total = total;
        this.hasMore = hasMore;
        this.meta = Collections.unmodifiableMap(new LinkedHashMap<>(meta));
    }
}
