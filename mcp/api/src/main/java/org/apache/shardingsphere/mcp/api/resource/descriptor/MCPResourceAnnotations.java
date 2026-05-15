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
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * MCP resource annotations.
 */
@RequiredArgsConstructor
@Getter
public final class MCPResourceAnnotations {
    
    public static final MCPResourceAnnotations EMPTY = new MCPResourceAnnotations(Collections.emptyList(), 0D, false, null);
    
    private final List<String> audience;
    
    private final double priority;
    
    private final boolean priorityPresent;
    
    private final String lastModified;
    
    /**
     * Judge whether annotations are empty.
     *
     * @return true if annotations are empty
     */
    public boolean isEmpty() {
        return audience.isEmpty() && !priorityPresent && (null == lastModified || lastModified.isBlank());
    }
}
