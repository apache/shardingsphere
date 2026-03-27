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

package org.apache.shardingsphere.mcp.bootstrap.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MCP transport configuration.
 */
@RequiredArgsConstructor
@Getter
public final class MCPTransportConfiguration {
    
    private final HttpTransportConfiguration http;
    
    private final StdioTransportConfiguration stdio;
    
    /**
     * Validate transport configuration.
     *
     * @throws IllegalArgumentException when the configuration enables both transports or disables both transports
     */
    public void validate() {
        if (http.isEnabled() && stdio.isEnabled()) {
            throw new IllegalArgumentException("HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport.");
        }
        if (!http.isEnabled() && !stdio.isEnabled()) {
            throw new IllegalArgumentException("Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true.");
        }
    }
}
