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
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.metadata.converter.ShardingNodeConverter;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
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
        swapAlgorithms(data, result);
        swapStrategies(data, result);
        swapTableRules(data, result);
        if (null != data.getDefaultShardingColumn()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultShardingColumnNodePath().getPath(), data.getDefaultShardingColumn()));
        }
        if (null != data.getShardingCache()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getShardingCacheNodePath().getPath(),
                    YamlEngine.marshal(shardingCacheYamlSwapper.swapToYamlConfiguration(data.getShardingCache()))));
        }
        return result;
    }
    
    private void swapAlgorithms(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (Entry<String, AlgorithmConfiguration> each : data.getShardingAlgorithms().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getAlgorithmNodePath().getPath(each.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getKeyGenerators().entrySet()) {
            result.add(new YamlDataNode(
                    ShardingNodeConverter.getKeyGeneratorNodePath().getPath(each.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getAuditors().entrySet()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getAuditorNodePath().getPath(each.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
    }
    
    private void swapStrategies(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        if (null != data.getDefaultDatabaseShardingStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultDatabaseStrategyNodePath().getPath(),
                    YamlEngine.marshal(shardingStrategySwapper.swapToYamlConfiguration(data.getDefaultDatabaseShardingStrategy()))));
        }
        if (null != data.getDefaultTableShardingStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultTableStrategyNodePath().getPath(),
                    YamlEngine.marshal(shardingStrategySwapper.swapToYamlConfiguration(data.getDefaultTableShardingStrategy()))));
        }
        if (null != data.getDefaultKeyGenerateStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultKeyGenerateStrategyNodePath().getPath(),
                    YamlEngine.marshal(keyGenerateStrategySwapper.swapToYamlConfiguration(data.getDefaultKeyGenerateStrategy()))));
        }
        if (null != data.getDefaultAuditStrategy()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getDefaultAuditStrategyNodePath().getPath(),
                    YamlEngine.marshal(auditStrategySwapper.swapToYamlConfiguration(data.getDefaultAuditStrategy()))));
        }
    }
    
    private void swapTableRules(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (ShardingTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getTableNodePath().getPath(each.getLogicTable()), YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        for (ShardingAutoTableRuleConfiguration each : data.getAutoTables()) {
            result.add(new YamlDataNode(ShardingNodeConverter.getAutoTableNodePath().getPath(each.getLogicTable()), YamlEngine.marshal(autoTableYamlSwapper.swapToYamlConfiguration(each))));
        }
        for (ShardingTableReferenceRuleConfiguration each : data.getBindingTableGroups()) {
            result.add(new YamlDataNode(
                    ShardingNodeConverter.getBindingTableNodePath().getPath(each.getName()), YamlShardingTableReferenceRuleConfigurationConverter.convertToYamlString(each)));
        }
    }
    
    @Override
    public ShardingRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (YamlDataNode each : dataNodes) {
            if (ShardingNodeConverter.getTableNodePath().isValidatedPath(each.getKey())) {
                ShardingNodeConverter.getTableNodePath().getName(each.getKey())
                        .ifPresent(tableName -> result.getTables().add(tableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlTableRuleConfiguration.class))));
            } else if (ShardingNodeConverter.getAutoTableNodePath().isValidatedPath(each.getKey())) {
                ShardingNodeConverter.getAutoTableNodePath().getName(each.getKey())
                        .ifPresent(autoTableName -> result.getAutoTables().add(autoTableYamlSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingAutoTableRuleConfiguration.class))));
            } else if (ShardingNodeConverter.getBindingTableNodePath().isValidatedPath(each.getKey())) {
                ShardingNodeConverter.getBindingTableNodePath().getName(each.getKey())
                        .ifPresent(bindingTableName -> result.getBindingTableGroups().add(YamlShardingTableReferenceRuleConfigurationConverter.convertToObject(each.getValue())));
            } else if (ShardingNodeConverter.getDefaultDatabaseStrategyNodePath().isValidatedPath(each.getKey())) {
                result.setDefaultDatabaseShardingStrategy(shardingStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class)));
            } else if (ShardingNodeConverter.getDefaultTableStrategyNodePath().isValidatedPath(each.getKey())) {
                result.setDefaultTableShardingStrategy(shardingStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class)));
            } else if (ShardingNodeConverter.getDefaultKeyGenerateStrategyNodePath().isValidatedPath(each.getKey())) {
                result.setDefaultKeyGenerateStrategy(keyGenerateStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlKeyGenerateStrategyConfiguration.class)));
            } else if (ShardingNodeConverter.getDefaultAuditStrategyNodePath().isValidatedPath(each.getKey())) {
                result.setDefaultAuditStrategy(auditStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingAuditStrategyConfiguration.class)));
            } else if (ShardingNodeConverter.getDefaultShardingColumnNodePath().isValidatedPath(each.getKey())) {
                result.setDefaultShardingColumn(each.getValue());
            } else if (ShardingNodeConverter.getAlgorithmNodePath().isValidatedPath(each.getKey())) {
                ShardingNodeConverter.getAlgorithmNodePath().getName(each.getKey())
                        .ifPresent(algorithmName -> result.getShardingAlgorithms().put(algorithmName,
                                algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            } else if (ShardingNodeConverter.getKeyGeneratorNodePath().isValidatedPath(each.getKey())) {
                ShardingNodeConverter.getKeyGeneratorNodePath().getName(each.getKey())
                        .ifPresent(keyGeneratorName -> result.getKeyGenerators().put(keyGeneratorName,
                                algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            } else if (ShardingNodeConverter.getAuditorNodePath().isValidatedPath(each.getKey())) {
                ShardingNodeConverter.getAuditorNodePath().getName(each.getKey())
                        .ifPresent(auditorName -> result.getAuditors().put(auditorName, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            } else if (ShardingNodeConverter.getShardingCacheNodePath().isValidatedPath(each.getKey())) {
                result.setShardingCache(shardingCacheYamlSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingCacheConfiguration.class)));
            }
        }
        return result;
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
