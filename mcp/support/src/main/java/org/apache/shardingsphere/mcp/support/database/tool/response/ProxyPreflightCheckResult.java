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

package org.apache.shardingsphere.mcp.support.database.tool.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Proxy preflight check result.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ProxyPreflightCheckResult {
    
    private static final String STATUS_PASSED = "passed";
    
    private static final String STATUS_FAILED = "failed";
    
    private static final String STATUS_SKIPPED = "skipped";
    
    private final String name;
    
    private final String status;
    
    private final String category;
    
    private final String message;
    
    /**
     * Create a passed check result.
     *
     * @param name check name
     * @param message check message
     * @return check result
     */
    public static ProxyPreflightCheckResult passed(final String name, final String message) {
        return new ProxyPreflightCheckResult(name, STATUS_PASSED, "ready", message);
    }
    
    /**
     * Create a failed check result.
     *
     * @param name check name
     * @param category failure category
     * @param message check message
     * @return check result
     */
    public static ProxyPreflightCheckResult failed(final String name, final String category, final String message) {
        return new ProxyPreflightCheckResult(name, STATUS_FAILED, category, message);
    }
    
    /**
     * Create a skipped check result.
     *
     * @param name check name
     * @param message check message
     * @return check result
     */
    public static ProxyPreflightCheckResult skipped(final String name, final String message) {
        return new ProxyPreflightCheckResult(name, STATUS_SKIPPED, "skipped", message);
    }
    
    /**
     * Convert to payload.
     *
     * @return payload
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("name", name);
        result.put("status", status);
        result.put("category", category);
        result.put("message", message);
        return result;
    }
}
