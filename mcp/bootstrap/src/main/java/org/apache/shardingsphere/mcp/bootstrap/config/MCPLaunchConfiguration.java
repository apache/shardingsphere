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
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * MCP launch configuration.
 */
@Getter
public final class MCPLaunchConfiguration {
    
    private final HttpServerConfiguration httpServerConfiguration;
    
    private final boolean httpEnabled;
    
    private final boolean stdioEnabled;
    
    @Getter(AccessLevel.NONE)
    private final Properties runtimeProps;
    
    private final RuntimeTopologyConfiguration runtimeTopologyConfiguration;
    
    /**
     * Construct one MCP launch configuration.
     *
     * @param httpServerConfiguration HTTP server configuration
     * @param httpEnabled HTTP enablement
     * @param stdioEnabled STDIO enablement
     * @param runtimeProps runtime properties
     * @param runtimeTopologyConfiguration runtime topology configuration
     */
    public MCPLaunchConfiguration(final HttpServerConfiguration httpServerConfiguration, final boolean httpEnabled, final boolean stdioEnabled,
                                  final Properties runtimeProps, final RuntimeTopologyConfiguration runtimeTopologyConfiguration) {
        this.httpServerConfiguration = Objects.requireNonNull(httpServerConfiguration, "httpServerConfiguration cannot be null");
        this.httpEnabled = httpEnabled;
        this.stdioEnabled = stdioEnabled;
        this.runtimeProps = Objects.requireNonNull(runtimeProps, "runtimeProps cannot be null");
        this.runtimeTopologyConfiguration = Objects.requireNonNull(runtimeTopologyConfiguration, "runtimeTopologyConfiguration cannot be null");
        if (!runtimeProps.isEmpty() && runtimeTopologyConfiguration.isConfigured()) {
            throw new IllegalArgumentException("MCP runtime properties and runtime databases cannot be configured together.");
        }
    }
    
    /**
     * Get runtime properties.
     *
     * @return runtime properties
     */
    public Optional<Properties> getRuntimeProps() {
        return runtimeProps.isEmpty() ? Optional.empty() : Optional.of(runtimeProps);
    }
    
    /**
     * Get runtime topology configuration.
     *
     * @return runtime topology configuration when present
     */
    public Optional<RuntimeTopologyConfiguration> getRuntimeTopologyConfiguration() {
        return runtimeTopologyConfiguration.isConfigured() ? Optional.of(runtimeTopologyConfiguration) : Optional.empty();
    }
}
