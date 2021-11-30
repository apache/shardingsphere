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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Result set for show sharding table nodes.
 */
public final class ShardingTableNodesQueryResultSet implements DistSQLResultSet {
    
    private static final String SHARDING_COUNT_KEY = "sharding-count";
    
    private static final String NAME = "name";
    
    private static final String NODES = "nodes";
    
    private Iterator<Entry<String, String>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        metaData.getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration)
                .map(each -> (ShardingRuleConfiguration) each)
                .forEach(each -> data = getData(each, (ShowShardingTableNodesStatement) sqlStatement).entrySet().iterator());
    }
    
    private Map<String, String> getData(final ShardingRuleConfiguration config, final ShowShardingTableNodesStatement sqlStatement) {
        String tableName = sqlStatement.getTableName();
        Map<String, String> dataNodes = config.getTables().stream().filter(each -> null == tableName || each.getLogicTable().equals(tableName))
                .collect(Collectors.toMap(ShardingTableRuleConfiguration::getLogicTable, this::getDataNodes, (x, y) -> x, LinkedHashMap::new));
        Map<String, String> autoTables = config.getAutoTables().stream().filter(each -> null == tableName || each.getLogicTable().equals(tableName))
                .collect(Collectors.toMap(ShardingAutoTableRuleConfiguration::getLogicTable, each -> getDataNodes(each, getShardingCount(config, each)), (x, y) -> x, LinkedHashMap::new));
        Map<String, String> result = new LinkedHashMap<>();
        result.putAll(dataNodes);
        result.putAll(autoTables);
        return result;
    }
    
    private int getShardingCount(final ShardingRuleConfiguration ruleConfiguration, final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        int result = 0;
        ShardingStrategyConfiguration shardingStrategy = shardingAutoTableRuleConfig.getShardingStrategy();
        String shardingAlgorithmName;
        if (null == shardingStrategy || Strings.isNullOrEmpty(shardingStrategy.getShardingAlgorithmName())) {
            if (ruleConfiguration.getDefaultTableShardingStrategy() != null && ruleConfiguration.getDefaultDatabaseShardingStrategy() != null) {
                int tableCount = Integer.parseInt(ruleConfiguration.getShardingAlgorithms().get(ruleConfiguration.getDefaultTableShardingStrategy().getShardingAlgorithmName())
                        .getProps().getOrDefault(SHARDING_COUNT_KEY, 0).toString());
                int databaseCount = Integer.parseInt(ruleConfiguration.getShardingAlgorithms().get(ruleConfiguration.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName())
                        .getProps().getOrDefault(SHARDING_COUNT_KEY, 0).toString());
                result = tableCount * databaseCount;
            }
        } else {
            shardingAlgorithmName = shardingStrategy.getShardingAlgorithmName();
            ShardingSphereAlgorithmConfiguration algorithmConfiguration = ruleConfiguration.getShardingAlgorithms().get(shardingAlgorithmName);
            result = algorithmConfiguration != null ? Integer.parseInt(algorithmConfiguration.getProps().getOrDefault(SHARDING_COUNT_KEY, result).toString()) : result;
        }
        return result;
    }
    
    private String getDataNodes(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig, final int shardingCount) {
        List<String> dataSources = new InlineExpressionParser(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return fillDataSourceNames(shardingAutoTableRuleConfig.getLogicTable(), shardingCount, dataSources);
    }
    
    private String getDataNodes(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        return String.join(", ", new InlineExpressionParser(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate());
    }
    
    private String fillDataSourceNames(final String logicTable, final int amount, final List<String> dataSources) {
        List<String> result = new LinkedList<>();
        Iterator<String> iterator = dataSources.iterator();
        for (int i = 0; i < amount; i++) {
            if (!iterator.hasNext()) {
                iterator = dataSources.iterator();
            }
            result.add(String.format("%s.%s_%s", iterator.next(), logicTable, i));
        }
        return String.join(", ", result);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(NAME, NODES);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        if (!data.hasNext()) {
            return Collections.emptyList();
        }
        Entry<String, String> entry = data.next();
        return Arrays.asList(entry.getKey(), entry.getValue());
    }
    
    @Override
    public String getType() {
        return ShowShardingTableNodesStatement.class.getCanonicalName();
    }
}
