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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher.RuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher.RuntimeConfiguration.ServerConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * MCP configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPConfigurationLoader {
    
    private static final String DEFAULT_BIND_HOST = "127.0.0.1";
    
    private static final int DEFAULT_PORT = 18088;
    
    private static final String DEFAULT_ENDPOINT_PATH = "/mcp";
    
    /**
     * Load one MCP runtime configuration from YAML.
     *
     * @param configPath configuration path
     * @return runtime configuration
     * @throws IOException when the file cannot be read
     */
    public static RuntimeConfiguration load(final String configPath) throws IOException {
        File configFile = resolveConfigurationFile(configPath);
        String yamlContent = Files.readString(configFile.toPath());
        YamlMCPConfiguration yamlConfig = YamlEngine.unmarshal(yamlContent, YamlMCPConfiguration.class, true);
        Optional<Properties> runtimeProps = resolveRuntimeProps(yamlConfig);
        return runtimeProps.map(optional -> new RuntimeConfiguration(resolveServerConfiguration(yamlConfig), resolveHttpEnabled(yamlConfig), resolveStdioEnabled(yamlConfig), optional))
                .orElseGet(() -> new RuntimeConfiguration(resolveServerConfiguration(yamlConfig), resolveHttpEnabled(yamlConfig), resolveStdioEnabled(yamlConfig)));
    }
    
    private static File resolveConfigurationFile(final String configPath) throws FileNotFoundException {
        String actualConfigPath = Objects.requireNonNull(configPath, "configPath cannot be null").trim();
        ShardingSpherePreconditions.checkState(!actualConfigPath.isEmpty(), () -> new FileNotFoundException("MCP configuration path cannot be blank."));
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
    
    private static boolean resolveHttpEnabled(final YamlMCPConfiguration yamlConfig) {
        return null == yamlConfig.getTransport().getHttp().getEnabled() || yamlConfig.getTransport().getHttp().getEnabled();
    }
    
    private static boolean resolveStdioEnabled(final YamlMCPConfiguration yamlConfig) {
        return null == yamlConfig.getTransport().getStdio().getEnabled() || yamlConfig.getTransport().getStdio().getEnabled();
    }
    
    private static ServerConfiguration resolveServerConfiguration(final YamlMCPConfiguration yamlConfig) {
        String bindHost = normalizeText(yamlConfig.getServer().getBindHost());
        String endpointPath = normalizeText(yamlConfig.getServer().getEndpointPath());
        return new ServerConfiguration(bindHost.isEmpty() ? DEFAULT_BIND_HOST : bindHost, resolvePort(yamlConfig), endpointPath.isEmpty() ? DEFAULT_ENDPOINT_PATH : normalizePath(endpointPath));
    }
    
    private static Optional<Properties> resolveRuntimeProps(final YamlMCPConfiguration yamlConfig) {
        YamlRuntimeConfiguration runtimeConfiguration = yamlConfig.getRuntime();
        Properties props = new Properties();
        for (Entry<String, String> entry : runtimeConfiguration.getProps().entrySet()) {
            props.setProperty(entry.getKey(), null == entry.getValue() ? "" : entry.getValue());
        }
        return props.isEmpty() ? Optional.empty() : Optional.of(props);
    }
    
    private static int resolvePort(final YamlMCPConfiguration yamlConfig) {
        Integer result = yamlConfig.getServer().getPort();
        if (null == result) {
            return DEFAULT_PORT;
        }
        if (0 > result) {
            throw new IllegalArgumentException("MCP server port cannot be negative.");
        }
        return result;
    }
    
    private static String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
    
    private static String normalizePath(final String endpointPath) {
        return endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
    }
    
    /**
     * YAML MCP root configuration.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static final class YamlMCPConfiguration {
        
        private YamlServerConfiguration server = new YamlServerConfiguration();
        
        private YamlTransportConfiguration transport = new YamlTransportConfiguration();
        
        private YamlRuntimeConfiguration runtime = new YamlRuntimeConfiguration();
    }
    
    /**
     * YAML server configuration.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static final class YamlServerConfiguration {
        
        private String bindHost;
        
        private Integer port;
        
        private String endpointPath;
    }
    
    /**
     * YAML transport configuration.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static final class YamlTransportConfiguration {
        
        private YamlTransportSwitch http = new YamlTransportSwitch();
        
        private YamlTransportSwitch stdio = new YamlTransportSwitch();
    }
    
    /**
     * YAML transport switch configuration.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static final class YamlTransportSwitch {
        
        private Boolean enabled;
    }
    
    /**
     * YAML runtime configuration.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static final class YamlRuntimeConfiguration {
        
        private Map<String, String> props = new LinkedHashMap<>();
    }
}
