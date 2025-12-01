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

package org.apache.shardingsphere.readwritesplitting.yaml.swapper;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML readwrite-splitting rule configuration swapper.
 */
public final class YamlReadwriteSplittingRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlReadwriteSplittingRuleConfiguration, ReadwriteSplittingRuleConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlReadwriteSplittingRuleConfiguration swapToYamlConfiguration(final ReadwriteSplittingRuleConfiguration data) {
        YamlReadwriteSplittingRuleConfiguration result = new YamlReadwriteSplittingRuleConfiguration();
        result.setDataSourceGroups(data.getDataSourceGroups().stream().collect(
                Collectors.toMap(ReadwriteSplittingDataSourceGroupRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getLoadBalancers()) {
            data.getLoadBalancers().forEach((key, value) -> result.getLoadBalancers().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        return result;
    }
    
    private YamlReadwriteSplittingDataSourceGroupRuleConfiguration swapToYamlConfiguration(final ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupRuleConfig) {
        YamlReadwriteSplittingDataSourceGroupRuleConfiguration result = new YamlReadwriteSplittingDataSourceGroupRuleConfiguration();
        result.setWriteDataSourceName(dataSourceGroupRuleConfig.getWriteDataSourceName());
        if (null != dataSourceGroupRuleConfig.getReadDataSourceNames() && !dataSourceGroupRuleConfig.getReadDataSourceNames().isEmpty()) {
            result.setReadDataSourceNames(dataSourceGroupRuleConfig.getReadDataSourceNames());
        }
        result.setTransactionalReadQueryStrategy(dataSourceGroupRuleConfig.getTransactionalReadQueryStrategy().name());
        result.setLoadBalancerName(dataSourceGroupRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration swapToObject(final YamlReadwriteSplittingRuleConfiguration yamlConfig) {
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> dataSources = yamlConfig.getDataSourceGroups().entrySet().stream()
                .map(entry -> swapToObject(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        Map<String, AlgorithmConfiguration> loadBalancerMap = null == yamlConfig.getLoadBalancers()
                ? Collections.emptyMap()
                : yamlConfig.getLoadBalancers().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> algorithmSwapper.swapToObject(entry.getValue())));
        return new ReadwriteSplittingRuleConfiguration(dataSources, loadBalancerMap);
    }
    
    private ReadwriteSplittingDataSourceGroupRuleConfiguration swapToObject(final String name, final YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlDataSourceGroupRuleConfig) {
        return new ReadwriteSplittingDataSourceGroupRuleConfiguration(name, yamlDataSourceGroupRuleConfig.getWriteDataSourceName(), yamlDataSourceGroupRuleConfig.getReadDataSourceNames(),
                getTransactionalReadQueryStrategy(yamlDataSourceGroupRuleConfig), yamlDataSourceGroupRuleConfig.getLoadBalancerName());
    }
    
    private TransactionalReadQueryStrategy getTransactionalReadQueryStrategy(final YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlDataSourceGroupRuleConfig) {
        return Strings.isNullOrEmpty(yamlDataSourceGroupRuleConfig.getTransactionalReadQueryStrategy())
                ? TransactionalReadQueryStrategy.PRIMARY
                : TransactionalReadQueryStrategy.valueOf(yamlDataSourceGroupRuleConfig.getTransactionalReadQueryStrategy());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getTypeClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "READWRITE_SPLITTING";
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
}
