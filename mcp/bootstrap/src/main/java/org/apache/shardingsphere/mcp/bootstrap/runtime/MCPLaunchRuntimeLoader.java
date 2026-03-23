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

import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;

import java.util.Objects;
import java.util.Properties;

/**
 * Load runtime dependencies from MCP launch configuration for the default MCP launch path.
 */
public final class MCPLaunchRuntimeLoader {
    
    private final MCPRuntimeProvider runtimeProvider = new MCPRuntimeProvider();
    
    /**
     * Load one runtime projection from launch configuration.
     *
     * @param launchConfiguration launch configuration
     * @return loaded runtime projection
     */
    public LoadedRuntime load(final MCPLaunchConfiguration launchConfiguration) {
        MCPLaunchConfiguration actualLaunchConfiguration = Objects.requireNonNull(launchConfiguration, "launchConfiguration cannot be null");
        return loadRuntime(actualLaunchConfiguration);
    }
    
    private LoadedRuntime loadRuntime(final MCPLaunchConfiguration launchConfiguration) {
        return launchConfiguration.getRuntimeDatabases().isEmpty()
                ? runtimeProvider.load(getRequiredRuntimeProps(launchConfiguration))
                : runtimeProvider.load(launchConfiguration.getRuntimeDatabases());
    }
    
    private Properties getRequiredRuntimeProps(final MCPLaunchConfiguration launchConfiguration) {
        Properties result = launchConfiguration.getRuntimeProps();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("MCP runtime properties or runtime databases must be configured for the default launch path.");
        }
        return result;
    }
}
