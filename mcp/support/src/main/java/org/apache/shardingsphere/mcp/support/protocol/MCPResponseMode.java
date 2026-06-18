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
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Shared response mode vocabulary for model-facing MCP payloads.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPResponseMode {
    
    public static final String CATALOG = "catalog";
    
    public static final String RUNTIME = "runtime";
    
    public static final String LIST = "list";
    
    public static final String DETAIL = "detail";
    
    public static final String SEARCH = "search";
    
    public static final String QUERY = "query";
    
    public static final String PREVIEW = "preview";
    
    public static final String EXECUTED = "executed";
    
    public static final String PLANNING = "planning";
    
    public static final String MANUAL_ONLY = "manual_only";
    
    public static final String VALIDATION = "validation";
    
    public static final String RECOVERY = "recovery";
    
    public static final String TERMINAL = "terminal";
    
    private static final List<String> ALLOWED_MODES = List.of(
            CATALOG, RUNTIME, LIST, DETAIL, SEARCH, QUERY, PREVIEW, EXECUTED, PLANNING, MANUAL_ONLY, VALIDATION, RECOVERY, TERMINAL);
    
    /**
     * Judge whether a response mode is in the public contract.
     *
     * @param responseMode response mode
     * @return whether response mode is allowed
     */
    public static boolean isAllowed(final String responseMode) {
        return ALLOWED_MODES.contains(responseMode);
    }
}
