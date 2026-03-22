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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher.RuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.runtime.MCPRuntimeProvider.LoadedRuntime;

import java.util.Objects;
import java.util.Properties;

/**
 * Load production runtime dependencies for the default MCP launch path.
 */
public final class ProductionRuntimeLoader {
    
    private final MCPRuntimeProvider runtimeProvider = new MCPRuntimeProvider();
    
    /**
     * Load one production runtime projection from runtime configuration.
     *
     * @param runtimeConfiguration runtime configuration
     * @return loaded runtime projection
     * @throws IllegalStateException when runtime properties are missing or invalid
     */
    public LoadedRuntime load(final RuntimeConfiguration runtimeConfiguration) {
        Properties runtimeProps = getRuntimeProps(Objects.requireNonNull(runtimeConfiguration, "runtimeConfiguration cannot be null"));
        try {
            return runtimeProvider.load(runtimeProps);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalStateException("Failed to initialize MCP runtime from configured properties.", ex);
        }
    }
    
    private Properties getRuntimeProps(final RuntimeConfiguration runtimeConfiguration) {
        return runtimeConfiguration.getRuntimeProps()
                .orElseThrow(() -> new IllegalStateException("MCP runtime properties are required for the default launch path."));
    }
}
