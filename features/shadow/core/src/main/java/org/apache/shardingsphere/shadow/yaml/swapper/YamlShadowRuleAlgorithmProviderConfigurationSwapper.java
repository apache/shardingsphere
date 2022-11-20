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

import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.table.YamlShadowTableConfigurationSwapper;

/**
 * YAML shadow rule algorithm provider configuration swapper.
 */
public final class YamlShadowRuleAlgorithmProviderConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlShadowRuleConfiguration, AlgorithmProvidedShadowRuleConfiguration> {
    
    private final YamlShadowTableConfigurationSwapper tableSwapper = new YamlShadowTableConfigurationSwapper();
    
    @Override
    public YamlShadowRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedShadowRuleConfiguration data) {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.setDefaultShadowAlgorithmName(data.getDefaultShadowAlgorithmName());
        parseDataSources(data, result);
        parseShadowTables(data, result);
        parseShadowAlgorithms(data, result);
        return result;
    }
    
    private void parseShadowAlgorithms(final AlgorithmProvidedShadowRuleConfiguration data, final YamlShadowRuleConfiguration yamlConfig) {
        data.getShadowAlgorithms().forEach((key, value) -> yamlConfig.getShadowAlgorithms().put(key, new YamlAlgorithmConfiguration(value.getType(), value.getProps())));
    }
    
    private void parseShadowTables(final AlgorithmProvidedShadowRuleConfiguration data, final YamlShadowRuleConfiguration yamlConfig) {
        data.getTables().forEach((key, value) -> yamlConfig.getTables().put(key, tableSwapper.swapToYamlConfiguration(value)));
    }
    
    private void parseDataSources(final AlgorithmProvidedShadowRuleConfiguration data, final YamlShadowRuleConfiguration yamlConfig) {
        data.getDataSources().forEach(each -> yamlConfig.getDataSources().put(each.getName(), swapToDataSourceYamlConfiguration(each)));
    }
    
    private YamlShadowDataSourceConfiguration swapToDataSourceYamlConfiguration(final ShadowDataSourceConfiguration data) {
        YamlShadowDataSourceConfiguration result = new YamlShadowDataSourceConfiguration();
        result.setProductionDataSourceName(data.getProductionDataSourceName());
        result.setShadowDataSourceName(data.getShadowDataSourceName());
        return result;
    }
    
    @Override
    public AlgorithmProvidedShadowRuleConfiguration swapToObject(final YamlShadowRuleConfiguration yamlConfig) {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDefaultShadowAlgorithmName(yamlConfig.getDefaultShadowAlgorithmName());
        parseYamlDataSources(yamlConfig, result);
        parseYamlShadowTables(yamlConfig, result);
        return result;
    }
    
    private void parseYamlShadowTables(final YamlShadowRuleConfiguration yamlConfig, final AlgorithmProvidedShadowRuleConfiguration data) {
        yamlConfig.getTables().forEach((key, value) -> data.getTables().put(key, tableSwapper.swapToObject(value)));
    }
    
    private void parseYamlDataSources(final YamlShadowRuleConfiguration yamlConfig, final AlgorithmProvidedShadowRuleConfiguration data) {
        yamlConfig.getDataSources().forEach((key, value) -> data.getDataSources().add(swapToDataSourceObject(key, value)));
    }
    
    private ShadowDataSourceConfiguration swapToDataSourceObject(final String name, final YamlShadowDataSourceConfiguration yamlConfig) {
        return new ShadowDataSourceConfiguration(name, yamlConfig.getProductionDataSourceName(), yamlConfig.getShadowDataSourceName());
    }
    
    @Override
    public Class<AlgorithmProvidedShadowRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedShadowRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SHADOW";
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ALGORITHM_PROVIDER_ORDER;
    }
}
