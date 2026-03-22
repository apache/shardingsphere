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

package org.apache.shardingsphere.mcp.bootstrap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher;

import java.io.IOException;

/**
 * MCP bootstrap entrance.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPBootstrap {
    
    private static final String DEFAULT_CONFIG_PATH = "conf/mcp.yaml";
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IllegalStateException when the runtime configuration cannot be loaded
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        String configPath = resolveConfigurationPath(args);
        try {
            new MCPRuntimeLauncher().launch(MCPConfigurationLoader.load(configPath));
        } catch (final IOException ex) {
            throw new IllegalStateException(String.format("Failed to load MCP runtime configuration from `%s`.", configPath), ex);
        }
    }
    
    private static String resolveConfigurationPath(final String[] args) {
        if (0 == args.length || null == args[0]) {
            return DEFAULT_CONFIG_PATH;
        }
        String result = args[0].trim();
        return result.isEmpty() ? DEFAULT_CONFIG_PATH : result;
    }
}
