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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

/**
 * MCP transport configuration validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPTransportConfigurationValidator {
    
    /**
     * Validate transport configuration.
     *
     * @param transportConfig transport configuration
     */
    public static void validate(final MCPTransportConfiguration transportConfig) {
        ShardingSpherePreconditions.checkState(isTransportConfiguration(transportConfig.getHttp().isEnabled(), transportConfig.getStdio().isEnabled()),
                () -> new IllegalArgumentException("HTTP and STDIO transport should enable one."));
    }
    
    private static boolean isTransportConfiguration(final boolean httpEnabled, final boolean stdioEnabled) {
        return httpEnabled && !stdioEnabled || !httpEnabled && stdioEnabled;
    }
}
