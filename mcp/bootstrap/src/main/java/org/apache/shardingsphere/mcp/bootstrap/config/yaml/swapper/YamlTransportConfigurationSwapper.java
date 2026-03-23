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

import java.util.Collection;
import java.util.Collections;

/**
 * YAML transport configuration swapper.
 */
public final class YamlTransportConfigurationSwapper implements YamlConfigurationSwapper<YamlTransportConfiguration, TransportConfiguration> {
    
    @Override
    public YamlTransportConfiguration swapToYamlConfiguration(final TransportConfiguration data) {
        YamlTransportConfiguration result = new YamlTransportConfiguration();
        result.setHttpEnabled(data.isHttpEnabled());
        result.setStdioEnabled(data.isStdioEnabled());
        return result;
    }
    
    @Override
    public TransportConfiguration swapToObject(final YamlTransportConfiguration yamlConfig) {
        return swapToObject(yamlConfig, Collections.emptySet());
    }
    
    TransportConfiguration swapToObject(final YamlTransportConfiguration yamlConfig, final Collection<String> configuredFields) {
        YamlTransportConfiguration actualYamlConfig = null == yamlConfig ? new YamlTransportConfiguration() : yamlConfig;
        return new TransportConfiguration(resolveEnabled(actualYamlConfig.isHttpEnabled(), configuredFields.contains("httpEnabled") || actualYamlConfig.isHttpEnabled()),
                resolveEnabled(actualYamlConfig.isStdioEnabled(), configuredFields.contains("stdioEnabled") || actualYamlConfig.isStdioEnabled()));
    }
    
    private boolean resolveEnabled(final boolean enabled, final boolean configured) {
        return configured ? enabled : true;
    }
}
