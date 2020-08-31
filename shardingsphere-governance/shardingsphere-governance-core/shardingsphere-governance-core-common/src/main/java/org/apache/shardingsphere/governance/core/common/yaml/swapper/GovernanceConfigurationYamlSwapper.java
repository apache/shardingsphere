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

package org.apache.shardingsphere.governance.core.common.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.governance.core.common.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;

/**
 * Governance configuration YAML swapper.
 */
public final class GovernanceConfigurationYamlSwapper implements YamlSwapper<YamlGovernanceConfiguration, GovernanceConfiguration> {
    
    private final GovernanceCenterConfigurationYamlSwapper governanceCenterConfigurationSwapper = new GovernanceCenterConfigurationYamlSwapper();
    
    @Override
    public YamlGovernanceConfiguration swapToYamlConfiguration(final GovernanceConfiguration data) {
        YamlGovernanceConfiguration result = new YamlGovernanceConfiguration();
        result.setName(data.getName());
        result.setRegistryCenter(governanceCenterConfigurationSwapper.swapToYamlConfiguration(data.getRegistryCenterConfiguration()));
        if (data.getAdditionalConfigCenterConfiguration().isPresent()) {
            result.setAdditionalConfigCenter(governanceCenterConfigurationSwapper.swapToYamlConfiguration(data.getAdditionalConfigCenterConfiguration().get()));
        }
        return result;
    }
    
    @Override
    public GovernanceConfiguration swapToObject(final YamlGovernanceConfiguration yamlConfig) {
        GovernanceCenterConfiguration registryCenter = governanceCenterConfigurationSwapper.swapToObject(yamlConfig.getRegistryCenter());
        if (null == yamlConfig.getAdditionalConfigCenter()) {
            return new GovernanceConfiguration(yamlConfig.getName(), registryCenter, yamlConfig.isOverwrite());
        }
        GovernanceCenterConfiguration additionalConfigCenter = governanceCenterConfigurationSwapper.swapToObject(yamlConfig.getAdditionalConfigCenter());
        return new GovernanceConfiguration(yamlConfig.getName(), registryCenter, additionalConfigCenter, yamlConfig.isOverwrite());
    }
}
