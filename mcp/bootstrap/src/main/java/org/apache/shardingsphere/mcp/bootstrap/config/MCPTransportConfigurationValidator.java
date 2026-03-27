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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * MCP transport configuration validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPTransportConfigurationValidator {
    
    public static final String NO_ENABLED_TRANSPORT_ERROR_MESSAGE = "Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true.";
    
    public static final String MULTIPLE_ENABLED_TRANSPORTS_ERROR_MESSAGE = "HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport.";
    
    /**
     * Validate transport configuration.
     *
     * @param transportConfig transport configuration
     * @throws IllegalArgumentException when zero or multiple transports are enabled
     */
    public static void validate(final MCPTransportConfiguration transportConfig) {
        boolean httpEnabled = transportConfig.getHttp().isEnabled();
        boolean stdioEnabled = transportConfig.getStdio().isEnabled();
        if (!httpEnabled && !stdioEnabled) {
            throw new IllegalArgumentException(NO_ENABLED_TRANSPORT_ERROR_MESSAGE);
        }
        if (httpEnabled && stdioEnabled) {
            throw new IllegalArgumentException(MULTIPLE_ENABLED_TRANSPORTS_ERROR_MESSAGE);
        }
    }
}
