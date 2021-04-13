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

package org.apache.shardingsphere.governance.core.yaml.config;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationYamlSwapper;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuration converter for YAML content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlConfigurationConverter {
    
    /**
     * Convert data source configurations from YAML content.
     *
     * @param yamlContent YAML content
     * @return data source configurations
     */
    public static Map<String, DataSourceConfiguration> convertDataSourceConfigurations(final String yamlContent) {
        YamlDataSourceConfigurationWrap result = YamlEngine.unmarshal(yamlContent, YamlDataSourceConfigurationWrap.class);
        if (null == result.getDataSources() || result.getDataSources().isEmpty()) {
            return new LinkedHashMap<>();
        }
        return convertDataSourceConfigurations(result.getDataSources());
    }
    
    /**
     * Convert data source configurations from YAML content.
     *
     * @param yamlDataSourceConfigs YAML data source configurations
     * @return data source configurations
     */
    public static Map<String, DataSourceConfiguration> convertDataSourceConfigurations(final Map<String, Map<String, Object>> yamlDataSourceConfigs) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlDataSourceConfigs.size());
        yamlDataSourceConfigs.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(value)));
        return result;
    }
    
    /**
     * Convert data sources from YAML data source configurations.
     *
     * @param yamlDataSources YAML data sources
     * @return data sources
     */
    public static Map<String, DataSource> convertDataSources(final Map<String, Map<String, Object>> yamlDataSources) {
        return new YamlDataSourceConfigurationSwapper().swapToDataSources(yamlDataSources);
    }
    
    /**
     * Convert sharding rule configuration from YAML .
     *
     * @param yamlRuleConfigs yaml rule configurations
     * @return sharding rule configuration
     */
    public static ShardingRuleConfiguration convertShardingRuleConfig(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        Optional<YamlRuleConfiguration> ruleConfig = yamlRuleConfigs.stream()
                .filter(each -> each instanceof YamlShardingRuleConfiguration)
                .findFirst();
        Preconditions.checkState(ruleConfig.isPresent(), "No available sharding rule to load for governance.");
        return new ShardingRuleConfigurationYamlSwapper().swapToObject((YamlShardingRuleConfiguration) ruleConfig.get());
    }
    
    /**
     * Convert rule configurations from YAML content.
     *
     * @param yamlContent YAML content
     * @return rule configurations
     */
    public static Collection<RuleConfiguration> convertRuleConfigurations(final String yamlContent) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(yamlContent, YamlRuleConfigurationWrap.class).getRules());
    }
    
    /**
     * Convert users from YAML content.
     *
     * @param yamlContent YAML content
     * @return users
     */
    public static Collection<ShardingSphereUser> convertUsers(final String yamlContent) {
        Collection<String> users = YamlEngine.unmarshal(yamlContent, Collection.class);
        return YamlUsersConfigurationConverter.convertShardingSphereUser(users);
    }
    
    /**
     * Convert properties configuration from YAML content.
     *
     * @param yamlContent YAML content
     * @return properties
     */
    public static Properties convertProperties(final String yamlContent) {
        return YamlEngine.unmarshal(yamlContent, Properties.class);
    }
}
