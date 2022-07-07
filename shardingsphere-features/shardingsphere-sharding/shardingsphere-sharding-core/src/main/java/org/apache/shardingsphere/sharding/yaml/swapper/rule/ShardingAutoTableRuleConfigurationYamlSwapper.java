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

package org.apache.shardingsphere.sharding.yaml.swapper.rule;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNodeUtil;
import org.apache.shardingsphere.infra.expr.InlineExpressionParser;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding auto table rule configuration YAML swapper.
 */
@RequiredArgsConstructor
public final class ShardingAutoTableRuleConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlShardingAutoTableRuleConfiguration, ShardingAutoTableRuleConfiguration> {
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    private final KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyYamlSwapper = new KeyGenerateStrategyConfigurationYamlSwapper();
    
    // TODO remove after refactoring auto table actual data node.
    private final Map<String, ShardingAlgorithm> shardingAlgorithms;
    
    // TODO remove after refactoring auto table actual data node.
    private final Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithmConfigs;
    
    @Override
    public YamlShardingAutoTableRuleConfiguration swapToYamlConfiguration(final ShardingAutoTableRuleConfiguration data) {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setActualDataSources(data.getActualDataSources());
        result.setActualTablePrefix(data.getActualTablePrefix());
        if (null != data.getShardingStrategy()) {
            result.setShardingStrategy(shardingStrategyYamlSwapper.swapToYamlConfiguration(data.getShardingStrategy()));
        }
        if (null != data.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToYamlConfiguration(data.getKeyGenerateStrategy()));
        }
        if (null != data.getActualDataNodes() && !data.getActualDataNodes().isEmpty()) {
            result.setActualDataNodes(data.getActualDataNodes());
        } else if (!shardingAlgorithms.isEmpty() || !shardingAlgorithmConfigs.isEmpty()) {
            setActualDataNodesWithAlgorithms(data, result);
        }
        return result;
    }
    
    @Override
    public ShardingAutoTableRuleConfiguration swapToObject(final YamlShardingAutoTableRuleConfiguration yamlConfig) {
        Preconditions.checkNotNull(yamlConfig.getLogicTable(), "Logic table cannot be null.");
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(yamlConfig.getLogicTable(), yamlConfig.getActualDataSources());
        result.setActualTablePrefix(yamlConfig.getActualTablePrefix());
        result.setActualDataNodes(yamlConfig.getActualDataNodes());
        if (null != yamlConfig.getShardingStrategy()) {
            result.setShardingStrategy(shardingStrategyYamlSwapper.swapToObject(yamlConfig.getShardingStrategy()));
        }
        if (null != yamlConfig.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToObject(yamlConfig.getKeyGenerateStrategy()));
        }
        return result;
    }
    
    private void setActualDataNodesWithAlgorithms(final ShardingAutoTableRuleConfiguration data, final YamlShardingAutoTableRuleConfiguration result) {
        if (null != data.getActualDataSources() && !data.getActualDataSources().isEmpty()) {
            getShardingCountWithAlgorithms(data, shardingAlgorithms).ifPresent(shardingCount -> result.setActualDataNodes(getActualDataNodes(data, shardingCount)));
            getShardingCountWithAlgorithmConfig(data, shardingAlgorithmConfigs).ifPresent(shardingCount -> result.setActualDataNodes(getActualDataNodes(data, shardingCount)));
        }
    }
    
    private Optional<Integer> getShardingCountWithAlgorithms(final ShardingAutoTableRuleConfiguration configuration, final Map<String, ShardingAlgorithm> shardingAlgorithms) {
        if (null != configuration.getShardingStrategy() && shardingAlgorithms.containsKey(configuration.getShardingStrategy().getShardingAlgorithmName())) {
            ShardingAlgorithm algorithm = shardingAlgorithms.get(configuration.getShardingStrategy().getShardingAlgorithmName());
            if (algorithm instanceof ShardingAutoTableAlgorithm) {
                return Optional.of(((ShardingAutoTableAlgorithm) algorithm).getAutoTablesAmount());
            }
        }
        return Optional.empty();
    }
    
    private Optional<Integer> getShardingCountWithAlgorithmConfig(final ShardingAutoTableRuleConfiguration configuration, final Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithms) {
        if (null != configuration.getShardingStrategy() && shardingAlgorithms.containsKey(configuration.getShardingStrategy().getShardingAlgorithmName())) {
            ShardingAlgorithm algorithm = ShardingAlgorithmFactory.newInstance(shardingAlgorithms.get(configuration.getShardingStrategy().getShardingAlgorithmName()));
            if (algorithm instanceof ShardingAutoTableAlgorithm) {
                return Optional.of(((ShardingAutoTableAlgorithm) algorithm).getAutoTablesAmount());
            }
        }
        return Optional.empty();
    }
    
    private String getActualDataNodes(final ShardingAutoTableRuleConfiguration data, final int shardingCount) {
        Collection<String> dataSourceNames = new InlineExpressionParser(data.getActualDataSources()).splitAndEvaluate();
        return String.join(",", DataNodeUtil.getFormatDataNodes(shardingCount, data.getLogicTable(), dataSourceNames));
    }
}
