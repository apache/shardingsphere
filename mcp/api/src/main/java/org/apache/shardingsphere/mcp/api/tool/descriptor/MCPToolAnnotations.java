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

/**
 * MCP tool annotations.
 */
@Getter
public final class MCPToolAnnotations {
    
    public static final MCPToolAnnotations EMPTY = new MCPToolAnnotations(null, null, null, null, null, null);
    
    private final String title;
    
    private final Boolean readOnlyHint;
    
    private final Boolean destructiveHint;
    
    private final Boolean idempotentHint;
    
    private final Boolean openWorldHint;
    
    private final Boolean returnDirect;
    
    public MCPToolAnnotations(final String title, final Boolean readOnlyHint, final Boolean destructiveHint, final Boolean idempotentHint,
                              final Boolean openWorldHint, final Boolean returnDirect) {
        this.title = title;
        this.readOnlyHint = readOnlyHint;
        this.destructiveHint = destructiveHint;
        this.idempotentHint = idempotentHint;
        this.openWorldHint = openWorldHint;
        this.returnDirect = returnDirect;
    }
    
    /**
     * Judge whether annotations are empty.
     *
     * @return true if annotations are empty
     */
    public boolean isEmpty() {
        return (null == title || title.isBlank()) && null == readOnlyHint && null == destructiveHint && null == idempotentHint && null == openWorldHint && null == returnDirect;
    }
}
