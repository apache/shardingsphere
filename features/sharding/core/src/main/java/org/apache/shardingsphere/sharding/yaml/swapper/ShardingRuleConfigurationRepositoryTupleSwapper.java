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

import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding rule configuration repository tuple swapper.
 */
public final class ShardingRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<ShardingRuleConfiguration, YamlShardingRuleConfiguration> {
    
    private final YamlShardingRuleConfigurationSwapper ruleConfigSwapper = new YamlShardingRuleConfigurationSwapper();
    
    private final RuleNodePath shardingRuleNodePath = new ShardingRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlShardingRuleConfiguration yamlRuleConfig) {
        Collection<RepositoryTuple> result = new LinkedList<>();
        swapAlgorithms(yamlRuleConfig, result);
        swapStrategies(yamlRuleConfig, result);
        if (null != yamlRuleConfig.getDefaultShardingColumn()) {
            result.add(new RepositoryTuple(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN).getPath(), yamlRuleConfig.getDefaultShardingColumn()));
        }
        if (null != yamlRuleConfig.getShardingCache()) {
            result.add(new RepositoryTuple(shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.SHARDING_CACHE).getPath(), YamlEngine.marshal(yamlRuleConfig.getShardingCache())));
        }
        swapTableRules(yamlRuleConfig, result);
        return result;
    }
    
    private void swapAlgorithms(final YamlShardingRuleConfiguration yamlRuleConfig, final Collection<RepositoryTuple> repositoryTuples) {
        for (Entry<String, YamlAlgorithmConfiguration> each : yamlRuleConfig.getShardingAlgorithms().entrySet()) {
            repositoryTuples.add(new RepositoryTuple(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.ALGORITHMS).getPath(each.getKey()), YamlEngine.marshal(each.getValue())));
        }
        for (Entry<String, YamlAlgorithmConfiguration> each : yamlRuleConfig.getKeyGenerators().entrySet()) {
            repositoryTuples.add(new RepositoryTuple(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.KEY_GENERATORS).getPath(each.getKey()), YamlEngine.marshal(each.getValue())));
        }
        for (Entry<String, YamlAlgorithmConfiguration> each : yamlRuleConfig.getAuditors().entrySet()) {
            repositoryTuples.add(new RepositoryTuple(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUDITORS).getPath(each.getKey()), YamlEngine.marshal(each.getValue())));
        }
    }
    
    private void swapStrategies(final YamlShardingRuleConfiguration yamlRuleConfig, final Collection<RepositoryTuple> repositoryTuples) {
        if (null != yamlRuleConfig.getDefaultDatabaseStrategy()) {
            repositoryTuples.add(new RepositoryTuple(
                    shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY).getPath(), YamlEngine.marshal(yamlRuleConfig.getDefaultDatabaseStrategy())));
        }
        if (null != yamlRuleConfig.getDefaultTableStrategy()) {
            repositoryTuples.add(new RepositoryTuple(
                    shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY).getPath(), YamlEngine.marshal(yamlRuleConfig.getDefaultTableStrategy())));
        }
        if (null != yamlRuleConfig.getDefaultKeyGenerateStrategy()) {
            repositoryTuples.add(new RepositoryTuple(
                    shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY).getPath(), YamlEngine.marshal(yamlRuleConfig.getDefaultKeyGenerateStrategy())));
        }
        if (null != yamlRuleConfig.getDefaultAuditStrategy()) {
            repositoryTuples.add(new RepositoryTuple(
                    shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY).getPath(), YamlEngine.marshal(yamlRuleConfig.getDefaultAuditStrategy())));
        }
    }
    
    private void swapTableRules(final YamlShardingRuleConfiguration yamlRuleConfig, final Collection<RepositoryTuple> repositoryTuples) {
        for (YamlTableRuleConfiguration each : yamlRuleConfig.getTables().values()) {
            repositoryTuples.add(new RepositoryTuple(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.TABLES).getPath(each.getLogicTable()), YamlEngine.marshal(each)));
        }
        for (YamlShardingAutoTableRuleConfiguration each : yamlRuleConfig.getAutoTables().values()) {
            repositoryTuples.add(new RepositoryTuple(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUTO_TABLES).getPath(each.getLogicTable()), YamlEngine.marshal(each)));
        }
        for (String each : yamlRuleConfig.getBindingTables()) {
            repositoryTuples.add(new RepositoryTuple(shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.BINDING_TABLES).getPath(getBindingGroupName(each)), each));
        }
    }
    
    private String getBindingGroupName(final String bindingGroup) {
        return bindingGroup.contains(":") ? bindingGroup.substring(0, bindingGroup.indexOf(":")) : bindingGroup;
    }
    
    @Override
    public Optional<YamlShardingRuleConfiguration> swapToObject0(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validRepositoryTuples = repositoryTuples.stream().filter(each -> shardingRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validRepositoryTuples.isEmpty()) {
            return Optional.empty();
        }
        YamlShardingRuleConfiguration yamlRuleConfig = new YamlShardingRuleConfiguration();
        Map<String, YamlTableRuleConfiguration> tables = new LinkedHashMap<>();
        Map<String, YamlShardingAutoTableRuleConfiguration> autoTables = new LinkedHashMap<>();
        Collection<String> bindingTables = new LinkedList<>();
        Map<String, YamlAlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> keyGenerators = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> auditors = new LinkedHashMap<>();
        for (RepositoryTuple each : validRepositoryTuples) {
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.put(optional, YamlEngine.unmarshal(each.getValue(), YamlTableRuleConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUTO_TABLES).getName(each.getKey())
                    .ifPresent(optional -> autoTables.put(optional, YamlEngine.unmarshal(each.getValue(), YamlShardingAutoTableRuleConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.BINDING_TABLES).getName(each.getKey()).ifPresent(optional -> bindingTables.add(each.getValue()));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.ALGORITHMS).getName(each.getKey())
                    .ifPresent(optional -> shardingAlgorithms.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.KEY_GENERATORS).getName(each.getKey())
                    .ifPresent(optional -> keyGenerators.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUDITORS).getName(each.getKey())
                    .ifPresent(optional -> auditors.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
            if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultDatabaseStrategy(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultTableStrategy(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultKeyGenerateStrategy(YamlEngine.unmarshal(each.getValue(), YamlKeyGenerateStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultAuditStrategy(YamlEngine.unmarshal(each.getValue(), YamlShardingAuditStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultShardingColumn(each.getValue());
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.SHARDING_CACHE).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setShardingCache(YamlEngine.unmarshal(each.getValue(), YamlShardingCacheConfiguration.class));
            }
        }
        yamlRuleConfig.setTables(tables);
        yamlRuleConfig.setAutoTables(autoTables);
        yamlRuleConfig.setBindingTables(bindingTables);
        yamlRuleConfig.setShardingAlgorithms(shardingAlgorithms);
        yamlRuleConfig.setKeyGenerators(keyGenerators);
        yamlRuleConfig.setAuditors(auditors);
        return Optional.of(yamlRuleConfig);
    }
    
    @Override
    public Optional<ShardingRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validRepositoryTuples = repositoryTuples.stream().filter(each -> shardingRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validRepositoryTuples.isEmpty()) {
            return Optional.empty();
        }
        YamlShardingRuleConfiguration yamlRuleConfig = new YamlShardingRuleConfiguration();
        Map<String, YamlTableRuleConfiguration> tables = new LinkedHashMap<>();
        Map<String, YamlShardingAutoTableRuleConfiguration> autoTables = new LinkedHashMap<>();
        Collection<String> bindingTables = new LinkedList<>();
        Map<String, YamlAlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> keyGenerators = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> auditors = new LinkedHashMap<>();
        for (RepositoryTuple each : validRepositoryTuples) {
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.put(optional, YamlEngine.unmarshal(each.getValue(), YamlTableRuleConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUTO_TABLES).getName(each.getKey())
                    .ifPresent(optional -> autoTables.put(optional, YamlEngine.unmarshal(each.getValue(), YamlShardingAutoTableRuleConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.BINDING_TABLES).getName(each.getKey()).ifPresent(optional -> bindingTables.add(each.getValue()));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.ALGORITHMS).getName(each.getKey())
                    .ifPresent(optional -> shardingAlgorithms.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.KEY_GENERATORS).getName(each.getKey())
                    .ifPresent(optional -> keyGenerators.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
            shardingRuleNodePath.getNamedItem(ShardingRuleNodePathProvider.AUDITORS).getName(each.getKey())
                    .ifPresent(optional -> auditors.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
            if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultDatabaseStrategy(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultTableStrategy(YamlEngine.unmarshal(each.getValue(), YamlShardingStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultKeyGenerateStrategy(YamlEngine.unmarshal(each.getValue(), YamlKeyGenerateStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultAuditStrategy(YamlEngine.unmarshal(each.getValue(), YamlShardingAuditStrategyConfiguration.class));
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultShardingColumn(each.getValue());
            } else if (shardingRuleNodePath.getUniqueItem(ShardingRuleNodePathProvider.SHARDING_CACHE).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setShardingCache(YamlEngine.unmarshal(each.getValue(), YamlShardingCacheConfiguration.class));
            }
        }
        yamlRuleConfig.setTables(tables);
        yamlRuleConfig.setAutoTables(autoTables);
        yamlRuleConfig.setBindingTables(bindingTables);
        yamlRuleConfig.setShardingAlgorithms(shardingAlgorithms);
        yamlRuleConfig.setKeyGenerators(keyGenerators);
        yamlRuleConfig.setAuditors(auditors);
        return Optional.of(ruleConfigSwapper.swapToObject(yamlRuleConfig));
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
