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

import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.datasource.ShadowDataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.shadow.yaml.swapper.table.ShadowTableConfigurationYamlSwapper;

/**
 * Shadow rule configuration yaml swapper.
 */
public final class ShadowRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlShadowRuleConfiguration, ShadowRuleConfiguration> {
    
    private final ShadowDataSourceConfigurationYamlSwapper dataSourceConfigurationSwapper = new ShadowDataSourceConfigurationYamlSwapper();
    
    private final ShadowTableConfigurationYamlSwapper tableConfigurationYamlSwapper = new ShadowTableConfigurationYamlSwapper();
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlShadowRuleConfiguration swapToYamlConfiguration(final ShadowRuleConfiguration dataConfiguration) {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        parseBasicShadowRule(dataConfiguration, result);
        parseDataSources(dataConfiguration, result);
        parseShadowTables(dataConfiguration, result);
        parseShadowAlgorithms(dataConfiguration, result);
        return result;
    }
    
    private void parseShadowAlgorithms(final ShadowRuleConfiguration dataConfiguration, final YamlShadowRuleConfiguration yamlConfiguration) {
        dataConfiguration.getShadowAlgorithms().forEach((key, value) -> yamlConfiguration.getShadowAlgorithms().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
    }
    
    private void parseShadowTables(final ShadowRuleConfiguration dataConfiguration, final YamlShadowRuleConfiguration yamlConfiguration) {
        dataConfiguration.getTables().forEach((key, value) -> yamlConfiguration.getTables().put(key, tableConfigurationYamlSwapper.swapToYamlConfiguration(value)));
    }
    
    private void parseDataSources(final ShadowRuleConfiguration dataConfiguration, final YamlShadowRuleConfiguration yamlConfiguration) {
        dataConfiguration.getDataSources().forEach((key, value) -> yamlConfiguration.getDataSources().put(key, dataSourceConfigurationSwapper.swapToYamlConfiguration(value)));
    }
    
    // fixme remove method when the api refactoring is complete
    private void parseBasicShadowRule(final ShadowRuleConfiguration dataConfiguration, final YamlShadowRuleConfiguration yamlConfiguration) {
        yamlConfiguration.setColumn(dataConfiguration.getColumn());
        yamlConfiguration.setSourceDataSourceNames(dataConfiguration.getSourceDataSourceNames());
        yamlConfiguration.setShadowDataSourceNames(dataConfiguration.getShadowDataSourceNames());
    }
    
    @Override
    public ShadowRuleConfiguration swapToObject(final YamlShadowRuleConfiguration yamlConfiguration) {
        ShadowRuleConfiguration result = createBasicShadowRule(yamlConfiguration);
        parseYamlDataSources(yamlConfiguration, result);
        parseYamlShadowTables(yamlConfiguration, result);
        parseYamlShadowAlgorithms(yamlConfiguration, result);
        return result;
    }
    
    private void parseYamlShadowAlgorithms(final YamlShadowRuleConfiguration yamlConfiguration, final ShadowRuleConfiguration dataConfiguration) {
        yamlConfiguration.getShadowAlgorithms().forEach((key, value) -> dataConfiguration.getShadowAlgorithms().put(key, algorithmSwapper.swapToObject(value)));
    }
    
    private void parseYamlShadowTables(final YamlShadowRuleConfiguration yamlConfiguration, final ShadowRuleConfiguration dataConfiguration) {
        yamlConfiguration.getTables().forEach((key, value) -> dataConfiguration.getTables().put(key, tableConfigurationYamlSwapper.swapToObject(value)));
    }
    
    private void parseYamlDataSources(final YamlShadowRuleConfiguration yamlConfiguration, final ShadowRuleConfiguration dataConfiguration) {
        yamlConfiguration.getDataSources().forEach((key, value) -> dataConfiguration.getDataSources().put(key, dataSourceConfigurationSwapper.swapToObject(value)));
    }
    
    // fixme remove method when the api refactoring is complete
    private ShadowRuleConfiguration createBasicShadowRule(final YamlShadowRuleConfiguration yamlConfiguration) {
        return new ShadowRuleConfiguration(yamlConfiguration.getColumn(), yamlConfiguration.getSourceDataSourceNames(), yamlConfiguration.getShadowDataSourceNames());
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
