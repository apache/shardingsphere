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

package org.apache.shardingsphere.dbdiscovery.common.yaml.swapper;

import org.apache.shardingsphere.dbdiscovery.common.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.common.constant.DatabaseDiscoveryOrder;
import org.apache.shardingsphere.dbdiscovery.common.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.common.yaml.config.rule.YamlDatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Database discovery rule configuration YAML swapper.
 */
public final class DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper
        implements YamlRuleConfigurationSwapper<YamlDatabaseDiscoveryRuleConfiguration, AlgorithmProvidedDatabaseDiscoveryRuleConfiguration> {
    
    @Override
    public YamlDatabaseDiscoveryRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration data) {
        YamlDatabaseDiscoveryRuleConfiguration result = new YamlDatabaseDiscoveryRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(DatabaseDiscoveryDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        return result;
    }
    
    private YamlDatabaseDiscoveryDataSourceRuleConfiguration swapToYamlConfiguration(final DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlDatabaseDiscoveryDataSourceRuleConfiguration result = new YamlDatabaseDiscoveryDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfig.getName());
        result.setDataSourceNames(dataSourceRuleConfig.getDataSourceNames());
        return result;
    }
    
    @Override
    public AlgorithmProvidedDatabaseDiscoveryRuleConfiguration swapToObject(final YamlDatabaseDiscoveryRuleConfiguration yamlConfig) {
        Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlDatabaseDiscoveryDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        AlgorithmProvidedDatabaseDiscoveryRuleConfiguration ruleConfig = new AlgorithmProvidedDatabaseDiscoveryRuleConfiguration();
        ruleConfig.setDataSources(dataSources);
        return ruleConfig;
    }
    
    private DatabaseDiscoveryDataSourceRuleConfiguration swapToObject(final String name, final YamlDatabaseDiscoveryDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new DatabaseDiscoveryDataSourceRuleConfiguration(name, yamlDataSourceRuleConfig.getDataSourceNames(), yamlDataSourceRuleConfig.getDiscoveryTypeName());
    }
    
    @Override
    public Class<AlgorithmProvidedDatabaseDiscoveryRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "DB_DISCOVERY";
    }
    
    @Override
    public int getOrder() {
        return DatabaseDiscoveryOrder.ALGORITHM_PROVIDER_DB_DISCOVERY_ORDER;
    }
}
