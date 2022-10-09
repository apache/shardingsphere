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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

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
 * Query result set for show default sharding strategy.
 */
public final class DefaultShardingStrategyQueryResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Entry<String, LinkedList<Object>>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        shardingRule.ifPresent(optional -> data = buildData(optional).entrySet().iterator());
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
        result.add(algorithmConfig.getProps());
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "sharding_column", "sharding_algorithm_name", "sharding_algorithm_type", "sharding_algorithm_props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, LinkedList<Object>> entry = data.next();
        entry.getValue().addFirst(entry.getKey());
        return entry.getValue();
    }
    
    @Override
    public String getType() {
        return ShowDefaultShardingStrategyStatement.class.getName();
    }
}
