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

import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.center.configuration.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlOrchestrationConfiguration;

/**
 * Orchestration instance configuration YAML swapper.
 *
 * @author zhangliang
 * @author dongzonglei
 * @author wangguangyuan
 * @author sunbufu
 */
public final class OrchestrationConfigurationYamlSwapper implements YamlSwapper<YamlOrchestrationConfiguration, OrchestrationConfiguration> {
    
    /**
     * Swap from InstanceConfiguration to YamlInstanceConfiguration.
     *
     * @param data data to be swapped
     * @return YAML instance configuration
     */
    @Override
    public YamlOrchestrationConfiguration swap(final OrchestrationConfiguration data) {
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setInstanceConfigurationMap(data.getInstanceConfigurationMap());
        return result;
    }
    
    /**
     * Swap from YamlInstanceConfiguration to InstanceConfiguration.
     *
     * @param yamlConfiguration YAML instance configuration
     * @return swapped object
     */
    @Override
    public OrchestrationConfiguration swap(final YamlOrchestrationConfiguration yamlConfiguration) {
        OrchestrationConfiguration result = new OrchestrationConfiguration(yamlConfiguration.getInstanceConfigurationMap());
        return result;
    }
}
