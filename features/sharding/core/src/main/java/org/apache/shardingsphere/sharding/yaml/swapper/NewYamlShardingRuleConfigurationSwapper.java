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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.metadata.converter.ShardingNodeConverter;
import org.apache.shardingsphere.sharding.yaml.swapper.cache.YamlShardingCacheConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingAutoTableRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingTableReferenceRuleConfigurationConverter;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingTableRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlKeyGenerateStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingAuditStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingStrategyConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

/**
 * TODO Rename to YamlShardingRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * New YAML sharding rule configuration swapper.
 */
public final class NewYamlShardingRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<ShardingRuleConfiguration> {
    
    private final YamlShardingTableRuleConfigurationSwapper tableSwapper = new YamlShardingTableRuleConfigurationSwapper();
    
    private final YamlShardingAutoTableRuleConfigurationSwapper autoTableYamlSwapper = new YamlShardingAutoTableRuleConfigurationSwapper();
    
    private final YamlShardingStrategyConfigurationSwapper shardingStrategySwapper = new YamlShardingStrategyConfigurationSwapper();
    
    private final YamlKeyGenerateStrategyConfigurationSwapper keyGenerateStrategySwapper = new YamlKeyGenerateStrategyConfigurationSwapper();
    
    private final YamlShardingAuditStrategyConfigurationSwapper auditStrategySwapper = new YamlShardingAuditStrategyConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final YamlShardingCacheConfigurationSwapper shardingCacheYamlSwapper = new YamlShardingCacheConfigurationSwapper();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final ShardingRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        swapTableRules(data, result);
        swapStrategies(data, result);
        swapAlgorithms(data, result);
        if (null != data.getDefaultShardingColumn()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultShardingColumnPath(), data.getDefaultShardingColumn()));
        }
        if (null != data.getShardingCache()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getShardingCachePath(), YamlEngine.marshal(shardingCacheYamlSwapper.swapToYamlConfiguration(data.getShardingCache()))));
        }
        return result;
    }
    
    private void swapTableRules(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (ShardingTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getTableNamePath(each.getLogicTable()), YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        for (ShardingAutoTableRuleConfiguration each : data.getAutoTables()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getAutoTableNamePath(each.getLogicTable()), YamlEngine.marshal(autoTableYamlSwapper.swapToYamlConfiguration(each))));
        }
        for (ShardingTableReferenceRuleConfiguration each : data.getBindingTableGroups()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getBindingTableNamePath(each.getName()), YamlShardingTableReferenceRuleConfigurationConverter.convertToYamlString(each)));
        }
        if (null != data.getBroadcastTables() && !data.getBroadcastTables().isEmpty()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getBroadcastTablesPath(), YamlEngine.marshal(data.getBroadcastTables())));
        }
    }
    
    private void swapStrategies(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        if (null != data.getDefaultDatabaseShardingStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultDatabaseStrategyPath(),
                    YamlEngine.marshal(shardingStrategySwapper.swapToYamlConfiguration(data.getDefaultDatabaseShardingStrategy()))));
        }
        if (null != data.getDefaultTableShardingStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultTableStrategyPath(),
                    YamlEngine.marshal(shardingStrategySwapper.swapToYamlConfiguration(data.getDefaultTableShardingStrategy()))));
        }
        if (null != data.getDefaultKeyGenerateStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultKeyGenerateStrategyPath(),
                    YamlEngine.marshal(keyGenerateStrategySwapper.swapToYamlConfiguration(data.getDefaultKeyGenerateStrategy()))));
        }
        if (null != data.getDefaultAuditStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultAuditStrategyPath(),
                    YamlEngine.marshal(auditStrategySwapper.swapToYamlConfiguration(data.getDefaultAuditStrategy()))));
        }
    }
    
    private void swapAlgorithms(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (Entry<String, AlgorithmConfiguration> each : data.getShardingAlgorithms().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getShardingAlgorithmPath(each.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getKeyGenerators().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getKeyGeneratorPath(each.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getAuditors().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getAuditorPath(each.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
    }
    
    @Override
    public ShardingRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        return new ShardingRuleConfiguration();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getTypeClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SHARDING";
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
}
