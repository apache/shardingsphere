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

package org.apache.shardingsphere.dbdiscovery.yaml.swapper;

import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.constant.DatabaseDiscoveryOrder;
import org.apache.shardingsphere.dbdiscovery.yaml.config.rule.YamlDatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.config.rule.YamlDatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML database discovery rule configuration swapper.
 */
public final class YamlDatabaseDiscoveryRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlDatabaseDiscoveryRuleConfiguration, DatabaseDiscoveryRuleConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlDatabaseDiscoveryRuleConfiguration swapToYamlConfiguration(final DatabaseDiscoveryRuleConfiguration data) {
        YamlDatabaseDiscoveryRuleConfiguration result = new YamlDatabaseDiscoveryRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getDiscoveryHeartbeats()) {
            data.getDiscoveryHeartbeats().forEach((key, value) -> result.getDiscoveryHeartbeats().put(key, swapToYamlConfiguration(value)));
        }
        if (null != data.getDiscoveryTypes()) {
            data.getDiscoveryTypes().forEach((key, value) -> result.getDiscoveryTypes().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        return result;
    }
    
    private YamlDatabaseDiscoveryDataSourceRuleConfiguration swapToYamlConfiguration(final DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlDatabaseDiscoveryDataSourceRuleConfiguration result = new YamlDatabaseDiscoveryDataSourceRuleConfiguration();
        result.setDataSourceNames(dataSourceRuleConfig.getDataSourceNames());
        result.setDiscoveryHeartbeatName(dataSourceRuleConfig.getDiscoveryHeartbeatName());
        result.setDiscoveryTypeName(dataSourceRuleConfig.getDiscoveryTypeName());
        return result;
    }
    
    private YamlDatabaseDiscoveryHeartBeatConfiguration swapToYamlConfiguration(final DatabaseDiscoveryHeartBeatConfiguration heartBeatRuleConfig) {
        YamlDatabaseDiscoveryHeartBeatConfiguration result = new YamlDatabaseDiscoveryHeartBeatConfiguration();
        result.setProps(heartBeatRuleConfig.getProps());
        return result;
    }
    
    @Override
    public DatabaseDiscoveryRuleConfiguration swapToObject(final YamlDatabaseDiscoveryRuleConfiguration yamlConfig) {
        Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlDatabaseDiscoveryDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> heartBeats = new LinkedHashMap<>(yamlConfig.getDiscoveryHeartbeats().entrySet().size(), 1);
        if (null != yamlConfig.getDiscoveryHeartbeats()) {
            yamlConfig.getDiscoveryHeartbeats().forEach((key, value) -> heartBeats.put(key, swapToObject(value)));
        }
        Map<String, AlgorithmConfiguration> haTypes = new LinkedHashMap<>(yamlConfig.getDiscoveryTypes().entrySet().size(), 1);
        if (null != yamlConfig.getDiscoveryTypes()) {
            yamlConfig.getDiscoveryTypes().forEach((key, value) -> haTypes.put(key, algorithmSwapper.swapToObject(value)));
        }
        return new DatabaseDiscoveryRuleConfiguration(dataSources, heartBeats, haTypes);
    }
    
    private DatabaseDiscoveryDataSourceRuleConfiguration swapToObject(final String name, final YamlDatabaseDiscoveryDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new DatabaseDiscoveryDataSourceRuleConfiguration(name, yamlDataSourceRuleConfig.getDataSourceNames(), yamlDataSourceRuleConfig.getDiscoveryHeartbeatName(),
                yamlDataSourceRuleConfig.getDiscoveryTypeName());
    }
    
    private DatabaseDiscoveryHeartBeatConfiguration swapToObject(final YamlDatabaseDiscoveryHeartBeatConfiguration yamlHeartbeatRuleConfig) {
        return new DatabaseDiscoveryHeartBeatConfiguration(yamlHeartbeatRuleConfig.getProps());
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getTypeClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "DB_DISCOVERY";
    }
    
    @Override
    public int getOrder() {
        return DatabaseDiscoveryOrder.ORDER;
    }
}
