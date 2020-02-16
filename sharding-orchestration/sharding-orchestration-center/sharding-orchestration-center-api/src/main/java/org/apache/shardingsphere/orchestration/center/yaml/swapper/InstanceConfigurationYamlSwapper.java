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
        YamlInstanceConfiguration result = new YamlInstanceConfiguration();
        result.setOrchestrationType(data.getOrchestrationType());
        result.setInstanceType(data.getType());
        result.setServerLists(data.getServerLists());
        result.setNamespace(data.getNamespace());
        result.setProps(data.getProperties());
        return result;
    }
    
    /**
     * Swap from YamlInstanceConfiguration to InstanceConfiguration.
     *
     * @param yamlConfiguration YAML instance configuration
     * @return swapped object
     */
    @Override
    public InstanceConfiguration swap(final YamlInstanceConfiguration yamlConfiguration) {
        InstanceConfiguration result = new InstanceConfiguration(yamlConfiguration.getInstanceType(), yamlConfiguration.getProps());
        result.setOrchestrationType(yamlConfiguration.getOrchestrationType());
        result.setServerLists(yamlConfiguration.getServerLists());
        result.setNamespace(yamlConfiguration.getNamespace());
        return result;
    }
}
