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
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlTransportConfiguration;

import java.util.Collections;
import java.util.Map;

/**
 * YAML transport configuration swapper.
 */
public final class YamlTransportConfigurationSwapper implements YamlConfigurationSwapper<YamlTransportConfiguration, TransportConfiguration> {
    
    private final YamlHttpTransportConfigurationSwapper httpConfigSwapper = new YamlHttpTransportConfigurationSwapper();
    
    private final YamlStdioTransportConfigurationSwapper stdioConfigSwapper = new YamlStdioTransportConfigurationSwapper();
    
    @Override
    public YamlTransportConfiguration swapToYamlConfiguration(final TransportConfiguration data) {
        YamlTransportConfiguration result = new YamlTransportConfiguration();
        result.setHttp(httpConfigSwapper.swapToYamlConfiguration(data.getHttp()));
        result.setStdio(stdioConfigSwapper.swapToYamlConfiguration(data.getStdio()));
        return result;
    }
    
    @Override
    public TransportConfiguration swapToObject(final YamlTransportConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptyMap());
    }
    
    TransportConfiguration swapToObject(final YamlTransportConfiguration yamlConfig, final Map<String, Object> configuredSections) {
        YamlTransportConfiguration actualYamlConfig = null == yamlConfig ? new YamlTransportConfiguration() : yamlConfig;
        Map<String, Object> actualConfiguredSections = null == configuredSections ? Collections.emptyMap() : configuredSections;
        return new TransportConfiguration(httpConfigSwapper.swapToObject(actualYamlConfig.getHttp(), getConfiguredSection(actualConfiguredSections, "http")),
                stdioConfigSwapper.swapToObject(actualYamlConfig.getStdio(), getConfiguredSection(actualConfiguredSections, "stdio")));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getConfiguredSection(final Map<String, Object> configuredSections, final String sectionName) {
        Object section = configuredSections.get(sectionName);
        return section instanceof Map ? (Map<String, Object>) section : Collections.emptyMap();
    }
}
