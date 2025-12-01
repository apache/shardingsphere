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

package org.apache.shardingsphere.shadow.yaml.swapper;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.datasource.YamlShadowDataSourceConfigurationSwapper;
import org.apache.shardingsphere.shadow.yaml.swapper.table.YamlShadowTableConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML shadow rule configuration swapper.
 */
public final class YamlShadowRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlShadowRuleConfiguration, ShadowRuleConfiguration> {
    
    private final YamlShadowDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlShadowDataSourceConfigurationSwapper();
    
    private final YamlShadowTableConfigurationSwapper tableConfigSwapper = new YamlShadowTableConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlShadowRuleConfiguration swapToYamlConfiguration(final ShadowRuleConfiguration data) {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.getDataSources().putAll(swapToYamlDataSources(data.getDataSources()));
        result.getTables().putAll(swapToYamlShadowTables(data.getTables()));
        result.getShadowAlgorithms().putAll(swapToYamlShadowAlgorithms(data.getShadowAlgorithms()));
        result.setDefaultShadowAlgorithmName(data.getDefaultShadowAlgorithmName());
        setTableDefaultShadowDataSource(data.getTables(), data.getDataSources());
        setTableDefaultShadowAlgorithm(data.getTables(), data.getDefaultShadowAlgorithmName());
        return result;
    }
    
    private Map<String, YamlShadowDataSourceConfiguration> swapToYamlDataSources(final Collection<ShadowDataSourceConfiguration> dataSources) {
        return dataSources.stream()
                .collect(Collectors.toMap(ShadowDataSourceConfiguration::getName, dataSourceConfigSwapper::swapToYamlConfiguration, (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    private Map<String, YamlShadowTableConfiguration> swapToYamlShadowTables(final Map<String, ShadowTableConfiguration> data) {
        return data.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> tableConfigSwapper.swapToYamlConfiguration(entry.getValue()), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    private Map<String, YamlAlgorithmConfiguration> swapToYamlShadowAlgorithms(final Map<String, AlgorithmConfiguration> shadowAlgorithms) {
        return shadowAlgorithms.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> algorithmSwapper.swapToYamlConfiguration(entry.getValue()), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    @Override
    public ShadowRuleConfiguration swapToObject(final YamlShadowRuleConfiguration yamlConfig) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(swapToDataSources(yamlConfig.getDataSources()));
        result.setTables(swapToShadowTables(yamlConfig.getTables()));
        result.setShadowAlgorithms(swapToShadowAlgorithms(yamlConfig.getShadowAlgorithms()));
        setTableDefaultShadowDataSource(result.getTables(), result.getDataSources());
        setTableDefaultShadowAlgorithm(result.getTables(), result.getDefaultShadowAlgorithmName());
        result.setDefaultShadowAlgorithmName(yamlConfig.getDefaultShadowAlgorithmName());
        return result;
    }
    
    private Collection<ShadowDataSourceConfiguration> swapToDataSources(final Map<String, YamlShadowDataSourceConfiguration> dataSources) {
        return dataSources.entrySet().stream().map(entry -> swapToDataSource(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
    
    private ShadowDataSourceConfiguration swapToDataSource(final String name, final YamlShadowDataSourceConfiguration yamlConfig) {
        return new ShadowDataSourceConfiguration(name, yamlConfig.getProductionDataSourceName(), yamlConfig.getShadowDataSourceName());
    }
    
    private Map<String, ShadowTableConfiguration> swapToShadowTables(final Map<String, YamlShadowTableConfiguration> tables) {
        return tables.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> tableConfigSwapper.swapToObject(entry.getValue()), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    private Map<String, AlgorithmConfiguration> swapToShadowAlgorithms(final Map<String, YamlAlgorithmConfiguration> shadowAlgorithms) {
        return shadowAlgorithms.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> algorithmSwapper.swapToObject(entry.getValue()), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    private void setTableDefaultShadowDataSource(final Map<String, ShadowTableConfiguration> shadowTables, final Collection<ShadowDataSourceConfiguration> shadowDataSources) {
        if (1 == shadowDataSources.size()) {
            for (ShadowTableConfiguration each : shadowTables.values()) {
                if (each.getDataSourceNames().isEmpty()) {
                    each.getDataSourceNames().add(shadowDataSources.iterator().next().getName());
                }
            }
        }
    }
    
    private void setTableDefaultShadowAlgorithm(final Map<String, ShadowTableConfiguration> shadowTables, final String defaultShadowAlgorithmName) {
        for (ShadowTableConfiguration each : shadowTables.values()) {
            Collection<String> shadowAlgorithmNames = each.getShadowAlgorithmNames();
            if (null != defaultShadowAlgorithmName && shadowAlgorithmNames.isEmpty()) {
                shadowAlgorithmNames.add(defaultShadowAlgorithmName);
            }
        }
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getTypeClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SHADOW";
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ORDER;
    }
}
