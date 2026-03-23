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

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpServerConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * YAML HTTP transport configuration swapper.
 */
public final class YamlHttpTransportConfigurationSwapper implements YamlConfigurationSwapper<YamlHttpTransportConfiguration, HttpTransportConfiguration> {
    
    private final YamlHttpServerConfigurationSwapper serverConfigSwapper = new YamlHttpServerConfigurationSwapper();
    
    @Override
    public YamlHttpTransportConfiguration swapToYamlConfiguration(final HttpTransportConfiguration data) {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setEnabled(data.isEnabled());
        result.setServer(serverConfigSwapper.swapToYamlConfiguration(data.getServer()));
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
        HttpServerConfiguration server = enabled
                ? serverConfigSwapper.swapToObject(actualYamlConfig.getServer(), getConfiguredFieldNames(actualConfiguredSection.get("server")))
                : serverConfigSwapper.swapToObject(null);
        return new HttpTransportConfiguration(enabled, server);
    }
    
    private boolean resolveEnabled(final boolean enabled, final boolean configured) {
        return configured ? enabled : true;
    }
    
    private Collection<String> getConfiguredFieldNames(final Object section) {
        return section instanceof Map ? new LinkedHashSet<>(((Map<?, ?>) section).keySet().stream().map(String::valueOf).toList()) : Collections.emptySet();
    }
}
