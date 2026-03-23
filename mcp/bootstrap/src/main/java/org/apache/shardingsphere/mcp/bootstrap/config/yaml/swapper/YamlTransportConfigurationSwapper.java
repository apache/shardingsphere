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
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlTransportSwitch;

/**
 * YAML transport configuration swapper.
 */
public final class YamlTransportConfigurationSwapper implements YamlConfigurationSwapper<YamlTransportConfiguration, TransportConfiguration> {
    
    @Override
    public YamlTransportConfiguration swapToYamlConfiguration(final TransportConfiguration data) {
        YamlTransportConfiguration result = new YamlTransportConfiguration();
        result.setHttp(createSwitch(data.isHttpEnabled()));
        result.setStdio(createSwitch(data.isStdioEnabled()));
        return result;
    }
    
    @Override
    public TransportConfiguration swapToObject(final YamlTransportConfiguration yamlConfig) {
        YamlTransportConfiguration actualYamlConfig = null == yamlConfig ? new YamlTransportConfiguration() : yamlConfig;
        return new TransportConfiguration(resolveEnabled(null == actualYamlConfig.getHttp() ? null : actualYamlConfig.getHttp().getEnabled()),
                resolveEnabled(null == actualYamlConfig.getStdio() ? null : actualYamlConfig.getStdio().getEnabled()));
    }
    
    private YamlTransportSwitch createSwitch(final boolean enabled) {
        YamlTransportSwitch result = new YamlTransportSwitch();
        result.setEnabled(enabled);
        return result;
    }
    
    private boolean resolveEnabled(final Boolean enabled) {
        return null == enabled || enabled;
    }
}
