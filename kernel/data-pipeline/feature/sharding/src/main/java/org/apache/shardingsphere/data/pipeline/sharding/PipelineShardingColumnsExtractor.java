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

package org.apache.shardingsphere.data.pipeline.sharding;

import org.apache.shardingsphere.data.pipeline.core.importer.PipelineRequiredColumnsExtractor;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline sharding columns extractor.
 */
public final class PipelineShardingColumnsExtractor implements PipelineRequiredColumnsExtractor<YamlShardingRuleConfiguration> {
    
    @Override
    public Map<ShardingSphereIdentifier, Collection<String>> getTableAndRequiredColumnsMap(final YamlShardingRuleConfiguration yamlRuleConfig,
                                                                                           final Collection<ShardingSphereIdentifier> logicTableNames) {
        ShardingRuleConfiguration ruleConfig = new YamlShardingRuleConfigurationSwapper().swapToObject(yamlRuleConfig);
        Set<String> defaultDatabaseShardingColumns = extractShardingColumns(ruleConfig.getDefaultDatabaseShardingStrategy());
        Set<String> defaultTableShardingColumns = extractShardingColumns(ruleConfig.getDefaultTableShardingStrategy());
        // TODO check is it need to be ConcurrentHashMap?
        // TODO check is it need to be ShardingSphereIdentifier with column names?
        Map<ShardingSphereIdentifier, Collection<String>> result = new ConcurrentHashMap<>(ruleConfig.getTables().size(), 1F);
        for (ShardingTableRuleConfiguration each : ruleConfig.getTables()) {
            ShardingSphereIdentifier logicTableName = new ShardingSphereIdentifier(each.getLogicTable());
            if (logicTableNames.contains(logicTableName)) {
                Collection<String> shardingColumns = new HashSet<>();
                shardingColumns.addAll(null == each.getDatabaseShardingStrategy() ? defaultDatabaseShardingColumns : extractShardingColumns(each.getDatabaseShardingStrategy()));
                shardingColumns.addAll(null == each.getTableShardingStrategy() ? defaultTableShardingColumns : extractShardingColumns(each.getTableShardingStrategy()));
                result.put(logicTableName, shardingColumns);
            }
        }
        for (ShardingAutoTableRuleConfiguration each : ruleConfig.getAutoTables()) {
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
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<YamlShardingRuleConfiguration> getTypeClass() {
        return YamlShardingRuleConfiguration.class;
    }
}
