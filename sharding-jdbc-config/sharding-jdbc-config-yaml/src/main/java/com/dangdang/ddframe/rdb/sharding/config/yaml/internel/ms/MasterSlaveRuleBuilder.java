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

package com.dangdang.ddframe.rdb.sharding.config.yaml.internel.ms;

import com.dangdang.ddframe.rdb.sharding.api.config.MasterSlaveRuleConfig;
import com.dangdang.ddframe.rdb.sharding.rule.MasterSlaveRule;
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
        MasterSlaveRuleConfig result = new MasterSlaveRuleConfig();
        result.setName(yamlMasterSlaveConfig.getName());
        result.setMasterDataSourceName(yamlMasterSlaveConfig.getMasterDataSourceName());
        result.setSlaveDataSourceNames(yamlMasterSlaveConfig.getSlaveDataSourceNames());
        result.setMasterSlaveLoadBalanceStrategyClassName(yamlMasterSlaveConfig.getMasterSlaveLoadBalanceStrategyClassName());
        return result.build(dataSourceMap.isEmpty() ? yamlMasterSlaveConfig.getDataSources() : dataSourceMap);
    }
}
