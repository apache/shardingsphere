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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowDefaultShardingStrategyStatement;
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
 * Result set for show default sharding strategy.
 */
public final class DefaultShardingStrategyQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Entry<String, LinkedList<Object>>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ShardingRuleConfiguration> shardingRuleConfiguration = metaData.getRuleMetaData().findRuleConfiguration(ShardingRuleConfiguration.class).stream().findAny();
        shardingRuleConfiguration.ifPresent(op -> data = buildData(op).entrySet().iterator());
    }
    
    private Map<String, LinkedList<Object>> buildData(final ShardingRuleConfiguration ruleConfiguration) {
        Map<String, LinkedList<Object>> result = new LinkedHashMap<>(2);
        result.put("TABLE", buildDataItem(ruleConfiguration, ruleConfiguration.getDefaultTableShardingStrategy()));
        result.put("DATABASE", buildDataItem(ruleConfiguration, ruleConfiguration.getDefaultDatabaseShardingStrategy()));
        return result;
    }
    
    private LinkedList<Object> buildDataItem(final ShardingRuleConfiguration ruleConfiguration, final ShardingStrategyConfiguration strategyConfiguration) {
        if (null == strategyConfiguration) {
            return new LinkedList<>(Arrays.asList("NONE", "", "", "", ""));
        }
        ShardingStrategyType strategyType = ShardingStrategyType.getValueOf(strategyConfiguration);
        if (strategyType == ShardingStrategyType.NONE) {
            return new LinkedList<>(Arrays.asList("NONE", "", "", "", ""));
        }
        LinkedList<Object> result = new LinkedList<>(Collections.singleton(strategyType.name()));
        result.addAll(strategyType.getConfigurationContents(strategyConfiguration));
        ShardingSphereAlgorithmConfiguration algorithmConfiguration = ruleConfiguration.getShardingAlgorithms().get(strategyConfiguration.getShardingAlgorithmName());
        result.add(algorithmConfiguration.getType());
        result.add(algorithmConfiguration.getProps());
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
