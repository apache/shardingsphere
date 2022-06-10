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
import org.apache.shardingsphere.infra.expr.InlineExpressionParser;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.route.strategy.type.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlNoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

/**
 * Sharding auto table rule configuration YAML swapper.
 */
public final class ShardingAutoTableRuleConfigurationYamlSwapper {
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    private final KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyYamlSwapper = new KeyGenerateStrategyConfigurationYamlSwapper();
    
    public YamlTableRuleConfiguration swapToYamlConfiguration(final ShardingAutoTableRuleConfiguration data, final int shardingCount) {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setActualDataNodes(getActualDataNodes(data, shardingCount));
        result.setActualTablePrefix(data.getActualTablePrefix());
        if (null != data.getShardingStrategy()) {
            result.setTableStrategy(shardingStrategyYamlSwapper.swapToYamlConfiguration(data.getShardingStrategy()));
        }
        YamlShardingStrategyConfiguration yamlShardingStrategyConfiguration = new YamlShardingStrategyConfiguration();
        yamlShardingStrategyConfiguration.setNone(new YamlNoneShardingStrategyConfiguration());
        result.setDatabaseStrategy(yamlShardingStrategyConfiguration);
        if (null != data.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToYamlConfiguration(data.getKeyGenerateStrategy()));
        }
        return result;
    }
    
    public ShardingAutoTableRuleConfiguration swapToObject(final YamlShardingAutoTableRuleConfiguration yamlConfig) {
        Preconditions.checkNotNull(yamlConfig.getLogicTable(), "Logic table cannot be null.");
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(yamlConfig.getLogicTable(), yamlConfig.getActualDataSources());
        result.setActualTablePrefix(yamlConfig.getActualTablePrefix());
        if (null != yamlConfig.getShardingStrategy()) {
            result.setShardingStrategy(shardingStrategyYamlSwapper.swapToObject(yamlConfig.getShardingStrategy()));
        }
        if (null != yamlConfig.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToObject(yamlConfig.getKeyGenerateStrategy()));
        }
        return result;
    }
    
    private String getActualDataNodes(final ShardingAutoTableRuleConfiguration data, final int shardingCount) {
        Collection<String> dataSourceNames = getDataSourceNames(data.getActualDataSources());
        return fillDataSourceNames(shardingCount, dataSourceNames, data.getLogicTable());
    }
    
    private Collection<String> getDataSourceNames(final String dataSources) {
        List<String> actualDataSources = new InlineExpressionParser(dataSources).splitAndEvaluate();
        return new HashSet<>(actualDataSources);
    }
    
    private String fillDataSourceNames(final int amount, final Collection<String> dataSources, final String logicTable) {
        StringJoiner result = new StringJoiner(",");
        Iterator<String> iterator = dataSources.iterator();
        for (int i = 0; i < amount; i++) {
            if (!iterator.hasNext()) {
                iterator = dataSources.iterator();
            }
            result.add(String.format("%s.%s_%s", iterator.next(), logicTable, i));
        }
        return result.toString();
    }
}
