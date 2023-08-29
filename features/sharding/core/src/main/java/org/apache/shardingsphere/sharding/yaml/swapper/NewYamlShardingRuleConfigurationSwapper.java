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
import org.apache.shardingsphere.mode.path.RuleNodePath;
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
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO Rename to YamlShardingRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * New YAML sharding rule configuration swapper.
 */
public final class NewYamlShardingRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<ShardingRuleConfiguration> {
    
    private final YamlShardingTableRuleConfigurationSwapper tableSwapper = new YamlShardingTableRuleConfigurationSwapper();
    
    private final YamlShardingAutoTableRuleConfigurationSwapper autoTableSwapper = new YamlShardingAutoTableRuleConfigurationSwapper();
    
    private final YamlShardingStrategyConfigurationSwapper shardingStrategySwapper = new YamlShardingStrategyConfigurationSwapper();
    
    private final YamlKeyGenerateStrategyConfigurationSwapper keyGenerateStrategySwapper = new YamlKeyGenerateStrategyConfigurationSwapper();
    
    private final YamlShardingAuditStrategyConfigurationSwapper auditStrategySwapper = new YamlShardingAuditStrategyConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final YamlShardingCacheConfigurationSwapper shardingCacheSwapper = new YamlShardingCacheConfigurationSwapper();
    
    private final RuleNodePath shardingRuleNodePath = new ShardingRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final ShardingRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedList<>();
        swapAlgorithms(data, result);
        swapStrategies(data, result);
        if (null != data.getDefaultShardingColumn()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN).getPath(), data.getDefaultShardingColumn()));
        }
        if (null != data.getShardingCache()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.SHARDING_CACHE).getPath(),
                    YamlEngine.marshal(shardingCacheSwapper.swapToYamlConfiguration(data.getShardingCache()))));
        }
        swapTableRules(data, result);
        return result;
    }
    
    private void swapAlgorithms(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (Entry<String, AlgorithmConfiguration> each : data.getShardingAlgorithms().entrySet()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.ALGORITHMS).getPath(each.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getKeyGenerators().entrySet()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.KEY_GENERATORS).getPath(each.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
        for (Entry<String, AlgorithmConfiguration> each : data.getAuditors().entrySet()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUDITORS).getPath(each.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(each.getValue()))));
        }
    }
    
    private void swapStrategies(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        if (null != data.getDefaultDatabaseShardingStrategy()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY).getPath(),
                    YamlEngine.marshal(shardingStrategySwapper.swapToYamlConfiguration(data.getDefaultDatabaseShardingStrategy()))));
        }
        if (null != data.getDefaultTableShardingStrategy()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY).getPath(),
                    YamlEngine.marshal(shardingStrategySwapper.swapToYamlConfiguration(data.getDefaultTableShardingStrategy()))));
        }
        if (null != data.getDefaultKeyGenerateStrategy()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY).getPath(),
                    YamlEngine.marshal(keyGenerateStrategySwapper.swapToYamlConfiguration(data.getDefaultKeyGenerateStrategy()))));
        }
        if (null != data.getDefaultAuditStrategy()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY).getPath(),
                    YamlEngine.marshal(auditStrategySwapper.swapToYamlConfiguration(data.getDefaultAuditStrategy()))));
        }
    }
    
    private void swapTableRules(final ShardingRuleConfiguration data, final Collection<YamlDataNode> result) {
        for (ShardingTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.TABLES).getPath(each.getLogicTable()),
                    YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        for (ShardingAutoTableRuleConfiguration each : data.getAutoTables()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUTO_TABLES).getPath(each.getLogicTable()),
                    YamlEngine.marshal(autoTableSwapper.swapToYamlConfiguration(each))));
        }
        for (ShardingTableReferenceRuleConfiguration each : data.getBindingTableGroups()) {
            result.add(new YamlDataNode(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.BINDING_TABLES).getPath(each.getName()),
                    YamlShardingTableReferenceRuleConfigurationConverter.convertToYamlString(each)));
        }
    }
    
    @Override
    public Optional<ShardingRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        List<YamlDataNode> validDataNodes = dataNodes.stream().filter(each -> shardingRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validDataNodes.isEmpty()) {
            return Optional.empty();
        }
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (YamlDataNode each : validDataNodes) {
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> result.getTables().add(tableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlTableRuleConfiguration.class))));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUTO_TABLES).getName(each.getKey())
                    .ifPresent(optional -> result.getAutoTables().add(autoTableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingAutoTableRuleConfiguration.class))));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.BINDING_TABLES).getName(each.getKey())
                    .ifPresent(optional -> result.getBindingTableGroups().add(YamlShardingTableReferenceRuleConfigurationConverter.convertToObject(each.getValue())));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.ALGORITHMS).getName(each.getKey())
                    .ifPresent(optional -> result.getShardingAlgorithms().put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.KEY_GENERATORS).getName(each.getKey())
                    .ifPresent(optional -> result.getKeyGenerators().put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUDITORS).getName(each.getKey())
                    .ifPresent(optional -> result.getAuditors().put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY).isValidatedPath(each.getKey())) {
                result.setDefaultDatabaseShardingStrategy(shardingStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class)));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY).isValidatedPath(each.getKey())) {
                result.setDefaultTableShardingStrategy(shardingStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class)));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY).isValidatedPath(each.getKey())) {
                result.setDefaultKeyGenerateStrategy(keyGenerateStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlKeyGenerateStrategyConfiguration.class)));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY).isValidatedPath(each.getKey())) {
                result.setDefaultAuditStrategy(auditStrategySwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingAuditStrategyConfiguration.class)));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN).isValidatedPath(each.getKey())) {
                result.setDefaultShardingColumn(each.getValue());
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.SHARDING_CACHE).isValidatedPath(each.getKey())) {
                result.setShardingCache(shardingCacheSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShardingCacheConfiguration.class)));
            }
        }
        return Optional.of(result);
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
