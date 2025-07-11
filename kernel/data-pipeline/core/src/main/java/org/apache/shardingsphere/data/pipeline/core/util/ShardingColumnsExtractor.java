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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sharding columns extractor.
 */
public final class ShardingColumnsExtractor {
    
    /**
     * Get table and sharding columns map.
     *
     * @param yamlRuleConfigs YAML rule configurations
     * @param logicTableNames logic table names
     * @return table and sharding columns map
     */
    public Map<ShardingSphereIdentifier, Collection<String>> getTableAndShardingColumnsMap(final Collection<YamlRuleConfiguration> yamlRuleConfigs,
                                                                                           final Collection<ShardingSphereIdentifier> logicTableNames) {
        Optional<ShardingRuleConfiguration> shardingRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(yamlRuleConfigs);
        if (!shardingRuleConfig.isPresent()) {
            return Collections.emptyMap();
        }
        Set<String> defaultDatabaseShardingColumns = extractShardingColumns(shardingRuleConfig.get().getDefaultDatabaseShardingStrategy());
        Set<String> defaultTableShardingColumns = extractShardingColumns(shardingRuleConfig.get().getDefaultTableShardingStrategy());
        // TODO check is it need to be ConcurrentHashMap?
        // TODO check is it need to be ShardingSphereIdentifier with column names?
        Map<ShardingSphereIdentifier, Collection<String>> result = new ConcurrentHashMap<>(shardingRuleConfig.get().getTables().size(), 1F);
        for (ShardingTableRuleConfiguration each : shardingRuleConfig.get().getTables()) {
            ShardingSphereIdentifier logicTableName = new ShardingSphereIdentifier(each.getLogicTable());
            if (logicTableNames.contains(logicTableName)) {
                Collection<String> shardingColumns = new HashSet<>();
                shardingColumns.addAll(null == each.getDatabaseShardingStrategy() ? defaultDatabaseShardingColumns : extractShardingColumns(each.getDatabaseShardingStrategy()));
                shardingColumns.addAll(null == each.getTableShardingStrategy() ? defaultTableShardingColumns : extractShardingColumns(each.getTableShardingStrategy()));
                result.put(logicTableName, shardingColumns);
            }
        }
        for (ShardingAutoTableRuleConfiguration each : shardingRuleConfig.get().getAutoTables()) {
            ShardingSphereIdentifier logicTableName = new ShardingSphereIdentifier(each.getLogicTable());
            if (logicTableNames.contains(logicTableName)) {
                result.put(logicTableName, extractShardingColumns(each.getShardingStrategy()));
            }
        }
        return result;
    }
    
    private Set<String> extractShardingColumns(final ShardingStrategyConfiguration shardingStrategy) {
        if (shardingStrategy instanceof StandardShardingStrategyConfiguration) {
            return Collections.singleton(((StandardShardingStrategyConfiguration) shardingStrategy).getShardingColumn());
        }
        if (shardingStrategy instanceof ComplexShardingStrategyConfiguration) {
            return new HashSet<>(Arrays.asList(((ComplexShardingStrategyConfiguration) shardingStrategy).getShardingColumns().split(",")));
        }
        return Collections.emptySet();
    }
}
