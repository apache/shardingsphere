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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import org.apache.shardingsphere.governance.core.constant.GovernanceOrder;
import org.apache.shardingsphere.governance.core.yaml.pojo.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper;

/**
 * Governance configuration YAML swapper.
 */
public final class GovernanceConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlGovernanceConfiguration, GovernanceConfiguration> {
    
    private final RegistryCenterConfigurationYamlSwapper registryCenterConfigurationYamlSwapper = new RegistryCenterConfigurationYamlSwapper();
    
    @Override
    public YamlGovernanceConfiguration swapToYamlConfiguration(final GovernanceConfiguration data) {
        YamlGovernanceConfiguration result = new YamlGovernanceConfiguration();
        result.setRegistryCenter(registryCenterConfigurationYamlSwapper.swapToYamlConfiguration(data.getRegistryCenterConfiguration()));
        result.setOverwrite(data.isOverwrite());
        return result;
    }
    
    @Override
    public GovernanceConfiguration swapToObject(final YamlGovernanceConfiguration yamlConfig) {
        RegistryCenterConfiguration registryCenterConfig = registryCenterConfigurationYamlSwapper.swapToObject(yamlConfig.getRegistryCenter());
        return new GovernanceConfiguration(registryCenterConfig, yamlConfig.isOverwrite());
    }
    
    @Override
    public int getOrder() {
        return GovernanceOrder.ORDER;
    }
    
    @Override
    public Class<GovernanceConfiguration> getTypeClass() {
        return GovernanceConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "GOVERNANCE";
    }
}
