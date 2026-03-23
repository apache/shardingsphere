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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpServerConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpServerConfiguration;

import java.util.Collection;
import java.util.Collections;

/**
 * YAML HTTP server configuration swapper.
 */
public final class YamlHttpServerConfigurationSwapper implements YamlConfigurationSwapper<YamlHttpServerConfiguration, HttpServerConfiguration> {
    
    private static final String DEFAULT_BIND_HOST = "127.0.0.1";
    
    private static final int DEFAULT_PORT = 18088;
    
    private static final String DEFAULT_ENDPOINT_PATH = "/mcp";
    
    @Override
    public YamlHttpServerConfiguration swapToYamlConfiguration(final HttpServerConfiguration data) {
        YamlHttpServerConfiguration result = new YamlHttpServerConfiguration();
        result.setBindHost(data.getBindHost());
        result.setPort(data.getPort());
        result.setEndpointPath(data.getEndpointPath());
        return result;
    }
    
    @Override
    public HttpServerConfiguration swapToObject(final YamlHttpServerConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptySet());
    }
    
    HttpServerConfiguration swapToObject(final YamlHttpServerConfiguration yamlConfig, final Collection<String> configuredFields) {
        YamlHttpServerConfiguration actualYamlConfig = null == yamlConfig ? new YamlHttpServerConfiguration() : yamlConfig;
        String bindHost = normalizeText(actualYamlConfig.getBindHost());
        String endpointPath = normalizeText(actualYamlConfig.getEndpointPath());
        boolean portConfigured = configuredFields.contains("port") || 0 != actualYamlConfig.getPort();
        return new HttpServerConfiguration(bindHost.isEmpty() ? DEFAULT_BIND_HOST : bindHost, resolvePort(actualYamlConfig.getPort(), portConfigured),
                endpointPath.isEmpty() ? DEFAULT_ENDPOINT_PATH : normalizePath(endpointPath));
    }
    
    private int resolvePort(final int port, final boolean configured) {
        if (!configured) {
            return DEFAULT_PORT;
        }
        ShardingSpherePreconditions.checkState(port >= 0, () -> new IllegalArgumentException("MCP server port cannot be negative."));
        return port;
    }
    
    private String normalizePath(final String endpointPath) {
        return endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
    }
    
    private String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
}
