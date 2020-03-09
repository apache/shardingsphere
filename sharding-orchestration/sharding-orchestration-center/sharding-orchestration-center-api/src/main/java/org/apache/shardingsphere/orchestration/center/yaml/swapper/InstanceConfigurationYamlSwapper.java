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

package org.apache.shardingsphere.orchestration.center.yaml.swapper;

import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlInstanceConfiguration;

/**
 * Orchestration instance configuration YAML swapper.
 */
public final class InstanceConfigurationYamlSwapper implements YamlSwapper<YamlInstanceConfiguration, InstanceConfiguration> {
    
    /**
     * Swap from InstanceConfiguration to YamlInstanceConfiguration.
     *
     * @param data data to be swapped
     * @return YAML instance configuration
     */
    @Override
    public YamlInstanceConfiguration swap(final InstanceConfiguration data) {
        YamlInstanceConfiguration yamlInstanceConfiguration = new YamlInstanceConfiguration();
        yamlInstanceConfiguration.setOrchestrationType(data.getOrchestrationType());
        yamlInstanceConfiguration.setInstanceType(data.getType());
        yamlInstanceConfiguration.setServerLists(data.getServerLists());
        yamlInstanceConfiguration.setNamespace(data.getNamespace());
        yamlInstanceConfiguration.setProps(data.getProperties());
        return yamlInstanceConfiguration;
    }
    
    /**
     * Swap from YamlInstanceConfiguration to InstanceConfiguration.
     *
     * @param yamlConfiguration YAML instance configuration
     * @return swapped object
     */
    @Override
    public InstanceConfiguration swap(final YamlInstanceConfiguration yamlConfiguration) {
        InstanceConfiguration instanceConfiguration = new InstanceConfiguration(yamlConfiguration.getInstanceType(), yamlConfiguration.getProps());
        instanceConfiguration.setOrchestrationType(yamlConfiguration.getOrchestrationType());
        instanceConfiguration.setServerLists(yamlConfiguration.getServerLists());
        instanceConfiguration.setNamespace(yamlConfiguration.getNamespace());
        return instanceConfiguration;
    }
}
