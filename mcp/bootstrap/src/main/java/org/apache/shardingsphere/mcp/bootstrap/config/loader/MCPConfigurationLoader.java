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

package org.apache.shardingsphere.mcp.bootstrap.config.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper.YamlMCPLaunchConfigurationSwapper;
import org.apache.shardingsphere.mcp.runtime.MCPRuntimeProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * MCP configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPConfigurationLoader {
    
    /**
     * Load one MCP launch configuration from YAML.
     *
     * @param <T> runtime configuration type
     * @param configPath configuration path
     * @return launch configuration
     * @throws IOException when the file cannot be read
     */
    public static <T> MCPLaunchConfiguration<T> load(final String configPath) throws IOException {
        String yamlContent = Files.readString(resolveConfigurationFile(configPath).toPath());
        return castLaunchConfiguration(new YamlMCPLaunchConfigurationSwapper(TypedSPILoader.getService(MCPRuntimeProvider.class, null))
                .swapToObject(YamlEngine.unmarshal(yamlContent, YamlMCPLaunchConfiguration.class)));
    }
    
    @SuppressWarnings("unchecked")
    private static <T> MCPLaunchConfiguration<T> castLaunchConfiguration(final MCPLaunchConfiguration<?> launchConfiguration) {
        return (MCPLaunchConfiguration<T>) launchConfiguration;
    }
    
    private static File resolveConfigurationFile(final String configPath) throws FileNotFoundException {
        String actualConfigPath = configPath.trim();
        ShardingSpherePreconditions.checkNotEmpty(actualConfigPath, () -> new FileNotFoundException("MCP configuration path cannot be blank."));
        Path directPath = Paths.get(actualConfigPath).normalize();
        if (Files.exists(directPath)) {
            return directPath.toFile();
        }
        Path currentPath = Paths.get("").toAbsolutePath();
        while (null != currentPath) {
            Path candidatePath = currentPath.resolve(actualConfigPath).normalize();
            if (Files.exists(candidatePath)) {
                return candidatePath.toFile();
            }
            currentPath = currentPath.getParent();
        }
        throw new FileNotFoundException(String.format("MCP configuration file `%s` does not exist.", actualConfigPath));
    }
}
