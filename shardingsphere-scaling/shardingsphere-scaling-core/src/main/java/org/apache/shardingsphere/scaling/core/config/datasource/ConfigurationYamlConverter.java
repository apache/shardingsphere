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

package org.apache.shardingsphere.scaling.core.config.datasource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationYamlSwapper;

import java.util.Map;
import java.util.Optional;

/**
 * YAML converter for configuration.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationYamlConverter {
    
    /**
     * Load data source configurations.
     *
     * @param data data
     * @return data source configurations
     */
    public static Map<String, DataSourceConfiguration> loadDataSourceConfigs(final String data) {
        YamlDataSourceConfigurationWrap result = YamlEngine.unmarshal(data, YamlDataSourceConfigurationWrap.class);
        Preconditions.checkState(!result.getDataSources().isEmpty(), "No available data sources to load for governance.");
        return Maps.transformValues(result.getDataSources(), new DataSourceConfigurationYamlSwapper()::swapToObject);
    }
    
    /**
     * Serialize data source configurations.
     *
     * @param dataSourceConfigMap data source configurations
     * @return data
     */
    public static String serializeDataSourceConfigs(final Map<String, DataSourceConfiguration> dataSourceConfigMap) {
        YamlDataSourceConfigurationWrap yamlDataSourceConfigurationWrap = new YamlDataSourceConfigurationWrap();
        yamlDataSourceConfigurationWrap.setDataSources(Maps.transformValues(dataSourceConfigMap, new DataSourceConfigurationYamlSwapper()::swapToYamlConfiguration));
        return YamlEngine.marshal(yamlDataSourceConfigurationWrap);
    }
    
    /**
     * Load sharding rule configuration.
     *
     * @param data data
     * @return sharding rule configuration
     */
    public static ShardingRuleConfiguration loadShardingRuleConfig(final String data) {
        YamlRootRuleConfigurations rootRuleConfigs = YamlEngine.unmarshal(data, YamlRootRuleConfigurations.class);
        Optional<YamlRuleConfiguration> ruleConfig = rootRuleConfigs.getRules().stream().filter(each -> each instanceof YamlShardingRuleConfiguration).findFirst();
        Preconditions.checkState(ruleConfig.isPresent(), "No available sharding rule to load for governance.");
        return new ShardingRuleConfigurationYamlSwapper().swapToObject((YamlShardingRuleConfiguration) ruleConfig.get());
    }
}
