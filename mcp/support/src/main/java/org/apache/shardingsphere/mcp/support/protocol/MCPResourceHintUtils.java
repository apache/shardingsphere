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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP resource hint utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPResourceHintUtils {
    
    /**
     * Create a typed resource hint.
     *
     * @param uri resource URI
     * @param resourceKind resource kind
     * @param purpose hint purpose
     * @param reason hint reason
     * @param sourceField source payload field
     * @return typed resource hint
     */
    public static Map<String, Object> create(final String uri, final String resourceKind, final String purpose, final String reason, final String sourceField) {
        final Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("uri", uri);
        result.put("resource_kind", resourceKind);
        result.put("purpose", purpose);
        result.put("reason", reason);
        result.put("source_field", sourceField);
        return result;
    }
}
