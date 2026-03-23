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
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;

import java.util.Collections;
import java.util.Map;

/**
 * YAML HTTP transport configuration swapper.
 */
public final class YamlHttpTransportConfigurationSwapper implements YamlConfigurationSwapper<YamlHttpTransportConfiguration, HttpTransportConfiguration> {
    
    private static final String DEFAULT_BIND_HOST = "127.0.0.1";
    
    private static final int DEFAULT_PORT = 18088;
    
    private static final String DEFAULT_ENDPOINT_PATH = "/mcp";
    
    @Override
    public YamlHttpTransportConfiguration swapToYamlConfiguration(final HttpTransportConfiguration data) {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setEnabled(data.isEnabled());
        result.setBindHost(data.getBindHost());
        result.setPort(data.getPort());
        result.setEndpointPath(data.getEndpointPath());
        return result;
    }
    
    @Override
    public HttpTransportConfiguration swapToObject(final YamlHttpTransportConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptyMap());
    }
    
    HttpTransportConfiguration swapToObject(final YamlHttpTransportConfiguration yamlConfig, final Map<String, Object> configuredSection) {
        YamlHttpTransportConfiguration actualYamlConfig = null == yamlConfig ? new YamlHttpTransportConfiguration() : yamlConfig;
        Map<String, Object> actualConfiguredSection = null == configuredSection ? Collections.emptyMap() : configuredSection;
        boolean enabled = resolveEnabled(actualYamlConfig.isEnabled(), actualConfiguredSection.containsKey("enabled") || actualYamlConfig.isEnabled());
        return enabled ? new HttpTransportConfiguration(true, resolveBindHost(actualYamlConfig.getBindHost()),
                resolvePort(actualYamlConfig.getPort(), actualConfiguredSection.containsKey("port") || 0 != actualYamlConfig.getPort()), resolveEndpointPath(actualYamlConfig.getEndpointPath()))
                : new HttpTransportConfiguration(false, DEFAULT_BIND_HOST, DEFAULT_PORT, DEFAULT_ENDPOINT_PATH);
    }
    
    private boolean resolveEnabled(final boolean enabled, final boolean configured) {
        return configured ? enabled : true;
    }
    
    private String resolveBindHost(final String bindHost) {
        String result = normalizeText(bindHost);
        return result.isEmpty() ? DEFAULT_BIND_HOST : result;
    }
    
    private int resolvePort(final int port, final boolean configured) {
        if (!configured) {
            return DEFAULT_PORT;
        }
        ShardingSpherePreconditions.checkState(port >= 0, () -> new IllegalArgumentException("MCP server port cannot be negative."));
        return port;
    }
    
    private String resolveEndpointPath(final String endpointPath) {
        String result = normalizeText(endpointPath);
        return result.isEmpty() ? DEFAULT_ENDPOINT_PATH : normalizePath(result);
    }
    
    private String normalizePath(final String endpointPath) {
        return endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
    }
    
    private String normalizeText(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
}
