/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.config.yaml.internel.ms;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Master-slave rule builder from yaml.
 *
 * @author caohao
 */
@RequiredArgsConstructor
public final class MasterSlaveRuleBuilder {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final YamlMasterSlaveConfig yamlMasterSlaveConfig;
    
    /**
     * Build master-slave rule from yaml.
     * 
     * @return master-slave rule from yaml
     */
    public MasterSlaveRule build() {
        MasterSlaveRuleConfiguration result = new MasterSlaveRuleConfiguration();
        result.setName(yamlMasterSlaveConfig.getName());
        result.setMasterDataSourceName(yamlMasterSlaveConfig.getMasterDataSourceName());
        result.setSlaveDataSourceNames(yamlMasterSlaveConfig.getSlaveDataSourceNames());
        result.setLoadBalanceAlgorithmType(yamlMasterSlaveConfig.getLoadBalanceAlgorithmType());
        result.setLoadBalanceAlgorithmClassName(yamlMasterSlaveConfig.getLoadBalanceAlgorithmClassName());
        return result.build(dataSourceMap.isEmpty() ? yamlMasterSlaveConfig.getDataSources() : dataSourceMap);
    }
}
