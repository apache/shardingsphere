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

package org.apache.shardingsphere.agent.core.config.yaml.swapper;

import org.apache.shardingsphere.agent.core.config.ZipkinPluginConfiguration;
import org.apache.shardingsphere.agent.core.config.yaml.YamlZipkinPluginConfiguration;
import org.apache.shardingsphere.agent.core.constant.AgentConstant;
import org.apache.shardingsphere.agent.core.yaml.swapper.YamlPluginConfigurationSwapper;

/**
 * Zipkin plugin configuration YAML swapper.
 */
public final class ZipkinPluginConfigurationYamlSwapper implements YamlPluginConfigurationSwapper<YamlZipkinPluginConfiguration, ZipkinPluginConfiguration> {
    
    @Override
    public ZipkinPluginConfiguration swapToObject(final YamlZipkinPluginConfiguration yamlConfig) {
        ZipkinPluginConfiguration result = new ZipkinPluginConfiguration();
        result.setHost(yamlConfig.getHost());
        result.setPort(yamlConfig.getPort());
        result.setExtra(yamlConfig.getExtra());
        return result;
    }
    
    @Override
    public String getPluginTagName() {
        return "ZIPKIN";
    }
    
    @Override
    public String getType() {
        return AgentConstant.PLUGIN_NAME_ZIPKIN;
    }
}
