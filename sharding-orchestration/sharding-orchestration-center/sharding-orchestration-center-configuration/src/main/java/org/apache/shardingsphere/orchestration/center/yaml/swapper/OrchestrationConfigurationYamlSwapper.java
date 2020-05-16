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

import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Orchestration instance configuration YAML swapper.
 */
public final class OrchestrationConfigurationYamlSwapper implements YamlSwapper<YamlOrchestrationConfiguration, OrchestrationConfiguration> {
    
    @Override
    public YamlOrchestrationConfiguration swap(final OrchestrationConfiguration configuration) {
        Map<String, YamlCenterRepositoryConfiguration> yamlInstanceConfigurationMap = new HashMap<>();
        Map<String, CenterConfiguration> instanceConfigurationMap = configuration.getInstanceConfigurationMap();
        CenterRepositoryConfigurationYamlSwapper swapper = new CenterRepositoryConfigurationYamlSwapper();
        for (Entry<String, CenterConfiguration> each : instanceConfigurationMap.entrySet()) {
            yamlInstanceConfigurationMap.put(each.getKey(), swapper.swap(each.getValue()));
        }
        return new YamlOrchestrationConfiguration(yamlInstanceConfigurationMap);
    }
    
    @Override
    public OrchestrationConfiguration swap(final YamlOrchestrationConfiguration yamlConfiguration) {
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<>();
        Map<String, YamlCenterRepositoryConfiguration> yamlInstanceConfigurationMap = yamlConfiguration.getCenterRepositoryConfigurationMap();
        CenterRepositoryConfigurationYamlSwapper swapper = new CenterRepositoryConfigurationYamlSwapper();
        for (Entry<String, YamlCenterRepositoryConfiguration> each : yamlInstanceConfigurationMap.entrySet()) {
            instanceConfigurationMap.put(each.getKey(), swapper.swap(each.getValue()));
        }
        return new OrchestrationConfiguration(instanceConfigurationMap);
    }
}
