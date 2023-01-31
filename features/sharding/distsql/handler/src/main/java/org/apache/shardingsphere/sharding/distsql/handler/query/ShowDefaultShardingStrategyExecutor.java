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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Show default sharding strategy executor.
 */
public final class ShowDefaultShardingStrategyExecutor implements RQLExecutor<ShowDefaultShardingStrategyStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowDefaultShardingStrategyStatement sqlStatement) {
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!shardingRule.isPresent()) {
            return Collections.emptyList();
        }
        Iterator<Entry<String, LinkedList<Object>>> data = buildData(shardingRule.get()).entrySet().iterator();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (data.hasNext()) {
            Entry<String, LinkedList<Object>> entry = data.next();
            entry.getValue().addFirst(entry.getKey());
            result.add(new LocalDataQueryResultRow(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, LinkedList<Object>> buildData(final ShardingRule rule) {
        Map<String, LinkedList<Object>> result = new LinkedHashMap<>(2, 1);
        ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) rule.getConfiguration();
        result.put("TABLE", buildDataItem(ruleConfig, ruleConfig.getDefaultTableShardingStrategy()));
        result.put("DATABASE", buildDataItem(ruleConfig, ruleConfig.getDefaultDatabaseShardingStrategy()));
        return result;
    }
    
    private LinkedList<Object> buildDataItem(final ShardingRuleConfiguration ruleConfig, final ShardingStrategyConfiguration strategyConfig) {
        if (null == strategyConfig) {
            return new LinkedList<>(Arrays.asList("NONE", "", "", "", ""));
        }
        ShardingStrategyType strategyType = ShardingStrategyType.getValueOf(strategyConfig);
        if (strategyType == ShardingStrategyType.NONE) {
            return new LinkedList<>(Arrays.asList("NONE", "", "", "", ""));
        }
        LinkedList<Object> result = new LinkedList<>(Collections.singleton(strategyType.name()));
        result.addAll(strategyType.getConfigurationContents(strategyConfig));
        AlgorithmConfiguration algorithmConfig = ruleConfig.getShardingAlgorithms().get(strategyConfig.getShardingAlgorithmName());
        result.add(algorithmConfig.getType());
        result.add(algorithmConfig.getProps().toString());
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "sharding_column", "sharding_algorithm_name", "sharding_algorithm_type", "sharding_algorithm_props");
    }
    
    @Override
    public String getType() {
        return ShowDefaultShardingStrategyStatement.class.getName();
    }
}
