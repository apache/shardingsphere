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

package org.apache.shardingsphere.readwritesplitting.rule.changed;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Readwrite-splitting data source changed processor.
 */
public final class ReadwriteSplittingDataSourceChangedProcessor
        implements
            RuleItemConfigurationChangedProcessor<ReadwriteSplittingRuleConfiguration, ReadwriteSplittingDataSourceGroupRuleConfiguration> {
    
    @Override
    public ReadwriteSplittingDataSourceGroupRuleConfiguration swapRuleItemConfiguration(final String itemName, final String yamlContent) {
        YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlDataSourceGroupRuleConfig = YamlEngine.unmarshal(yamlContent, YamlReadwriteSplittingDataSourceGroupRuleConfiguration.class);
        return new ReadwriteSplittingDataSourceGroupRuleConfiguration(itemName, yamlDataSourceGroupRuleConfig.getWriteDataSourceName(),
                yamlDataSourceGroupRuleConfig.getReadDataSourceNames(), getTransactionalReadQueryStrategy(yamlDataSourceGroupRuleConfig), yamlDataSourceGroupRuleConfig.getLoadBalancerName());
    }
    
    private TransactionalReadQueryStrategy getTransactionalReadQueryStrategy(final YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlDataSourceGroupRuleConfig) {
        return Strings.isNullOrEmpty(yamlDataSourceGroupRuleConfig.getTransactionalReadQueryStrategy())
                ? TransactionalReadQueryStrategy.PRIMARY
                : TransactionalReadQueryStrategy.valueOf(yamlDataSourceGroupRuleConfig.getTransactionalReadQueryStrategy());
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class).map(ReadwriteSplittingRule::getConfiguration)
                .orElseGet(() -> new ReadwriteSplittingRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
    }
    
    @Override
    public void changeRuleItemConfiguration(final String itemName, final ReadwriteSplittingRuleConfiguration currentRuleConfig,
                                            final ReadwriteSplittingDataSourceGroupRuleConfiguration toBeChangedItemConfig) {
        // TODO refactor DistSQL to only persist config
        currentRuleConfig.getDataSourceGroups().removeIf(each -> each.getName().equals(toBeChangedItemConfig.getName()));
        currentRuleConfig.getDataSourceGroups().add(toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final String itemName, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getDataSourceGroups().removeIf(each -> each.getName().equals(itemName));
    }
    
    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("readwrite_splitting", "data_source_groups");
    }
}
