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

package org.apache.shardingsphere.orchestration.repository.common.configuration.swapper;

import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationRepositoryConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Orchestration instance configuration YAML swapper.
 */
public final class OrchestrationConfigurationYamlSwapper implements YamlSwapper<YamlOrchestrationConfiguration, OrchestrationConfiguration> {
    
    private final OrchestrationRepositoryConfigurationYamlSwapper swapper = new OrchestrationRepositoryConfigurationYamlSwapper();
    
    @Override
    public YamlOrchestrationConfiguration swapToYamlConfiguration(final OrchestrationConfiguration configuration) {
        Map<String, YamlOrchestrationRepositoryConfiguration> configurations = new HashMap<>(2, 1);
        configurations.put(configuration.getRegistryCenterName(), swapper.swapToYamlConfiguration(configuration.getRegistryRepositoryConfiguration()));
        if (configuration.getAdditionalConfigCenterName().isPresent() && configuration.getAdditionalConfigurationRepositoryConfiguration().isPresent()) {
            configurations.put(configuration.getAdditionalConfigCenterName().get(), swapper.swapToYamlConfiguration(configuration.getAdditionalConfigurationRepositoryConfiguration().get()));
        }
        return new YamlOrchestrationConfiguration(configurations);
    }
    
    @Override
    public OrchestrationConfiguration swapToObject(final YamlOrchestrationConfiguration configuration) {
        String registryCenterName = null;
        OrchestrationRepositoryConfiguration registryRepositoryConfiguration = null;
        String additionalConfigCenterName = null;
        OrchestrationRepositoryConfiguration additionalConfigurationRepositoryConfiguration = null;
        for (Entry<String, YamlOrchestrationRepositoryConfiguration> entry : configuration.getOrchestrationRepositoryConfigurationMap().entrySet()) {
            if ("registry_center".equals(entry.getValue().getOrchestrationType())) {
                registryCenterName = entry.getKey();
                registryRepositoryConfiguration = swapper.swapToObject(entry.getValue());
            } else if ("config_center".equals(entry.getValue().getOrchestrationType())) {
                additionalConfigCenterName = entry.getKey();
                additionalConfigurationRepositoryConfiguration = swapper.swapToObject(entry.getValue());
            }
        }
        return new OrchestrationConfiguration(registryCenterName, registryRepositoryConfiguration, additionalConfigCenterName, additionalConfigurationRepositoryConfiguration);
    }
}
