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

import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.datasource.ShadowDataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.shadow.yaml.swapper.table.ShadowTableConfigurationYamlSwapper;

/**
 * Shadow rule algorithm provider configuration YAML swapper.
 */
public final class ShadowRuleAlgorithmProviderConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlShadowRuleConfiguration, AlgorithmProvidedShadowRuleConfiguration> {
    
    private final ShadowDataSourceConfigurationYamlSwapper dataSourceConfigurationSwapper = new ShadowDataSourceConfigurationYamlSwapper();
    
    private final ShadowTableConfigurationYamlSwapper tableConfigurationYamlSwapper = new ShadowTableConfigurationYamlSwapper();
    
    @Override
    public YamlShadowRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedShadowRuleConfiguration dataConfiguration) {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.setEnable(dataConfiguration.isEnable());
        parseDataSources(dataConfiguration, result);
        parseShadowTables(dataConfiguration, result);
        parseShadowAlgorithms(dataConfiguration, result);
        return result;
    }
    
    private void parseShadowAlgorithms(final AlgorithmProvidedShadowRuleConfiguration dataConfiguration, final YamlShadowRuleConfiguration yamlConfiguration) {
        dataConfiguration.getShadowAlgorithms().forEach((key, value) ->
                yamlConfiguration.getShadowAlgorithms().put(key, new YamlShardingSphereAlgorithmConfiguration(value.getType(), value.getProps())));
    }
    
    private void parseShadowTables(final AlgorithmProvidedShadowRuleConfiguration dataConfiguration, final YamlShadowRuleConfiguration yamlConfiguration) {
        dataConfiguration.getTables().forEach((key, value) -> yamlConfiguration.getTables().put(key, tableConfigurationYamlSwapper.swapToYamlConfiguration(value)));
    }
    
    private void parseDataSources(final AlgorithmProvidedShadowRuleConfiguration dataConfiguration, final YamlShadowRuleConfiguration yamlConfiguration) {
        dataConfiguration.getDataSources().forEach((key, value) -> yamlConfiguration.getDataSources().put(key, dataSourceConfigurationSwapper.swapToYamlConfiguration(value)));
    }
    
    @Override
    public AlgorithmProvidedShadowRuleConfiguration swapToObject(final YamlShadowRuleConfiguration yamlConfiguration) {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setEnable(yamlConfiguration.isEnable());
        parseYamlDataSources(yamlConfiguration, result);
        parseYamlShadowTables(yamlConfiguration, result);
        return result;
    }
    
    private void parseYamlShadowTables(final YamlShadowRuleConfiguration yamlConfiguration, final AlgorithmProvidedShadowRuleConfiguration dataConfiguration) {
        yamlConfiguration.getTables().forEach((key, value) -> dataConfiguration.getTables().put(key, tableConfigurationYamlSwapper.swapToObject(value)));
    }
    
    private void parseYamlDataSources(final YamlShadowRuleConfiguration yamlConfiguration, final AlgorithmProvidedShadowRuleConfiguration dataConfiguration) {
        yamlConfiguration.getDataSources().forEach((key, value) -> dataConfiguration.getDataSources().put(key, dataSourceConfigurationSwapper.swapToObject(value)));
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
