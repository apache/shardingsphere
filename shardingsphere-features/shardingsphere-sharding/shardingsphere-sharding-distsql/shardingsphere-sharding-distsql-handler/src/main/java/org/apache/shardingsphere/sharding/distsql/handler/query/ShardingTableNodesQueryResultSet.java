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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Query result set for show sharding table nodes.
 */
public final class ShardingTableNodesQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final String NAME = "name";
    
    private static final String NODES = "nodes";
    
    private Iterator<Entry<String, String>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        rule.ifPresent(optional -> data = getData((ShardingRuleConfiguration) optional.getConfiguration(), (ShowShardingTableNodesStatement) sqlStatement).entrySet().iterator());
    }
    
    private Map<String, String> getData(final ShardingRuleConfiguration config, final ShowShardingTableNodesStatement sqlStatement) {
        String tableName = sqlStatement.getTableName();
        Map<String, String> dataNodes = config.getTables().stream().filter(each -> null == tableName || each.getLogicTable().equals(tableName))
                .collect(Collectors.toMap(ShardingTableRuleConfiguration::getLogicTable, this::getDataNodes, (x, y) -> x, LinkedHashMap::new));
        Map<String, String> autoTables = config.getAutoTables().stream().filter(each -> null == tableName || each.getLogicTable().equals(tableName))
                .collect(Collectors.toMap(ShardingAutoTableRuleConfiguration::getLogicTable, each -> getDataNodes(each, getTotalShardingCount(config, each)), (x, y) -> x, LinkedHashMap::new));
        Map<String, String> result = new LinkedHashMap<>();
        result.putAll(dataNodes);
        result.putAll(autoTables);
        return result;
    }
    
    private int getTotalShardingCount(final ShardingRuleConfiguration ruleConfig, final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        Map<String, AlgorithmConfiguration> shardingAlgorithms = ruleConfig.getShardingAlgorithms();
        ShardingStrategyConfiguration shardingStrategy = shardingAutoTableRuleConfig.getShardingStrategy();
        if (useDefaultStrategy(shardingStrategy, ruleConfig)) {
            int tableCount = getShardingCount(shardingAlgorithms.get(ruleConfig.getDefaultTableShardingStrategy().getShardingAlgorithmName()));
            int databaseCount = getShardingCount(shardingAlgorithms.get(ruleConfig.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName()));
            return tableCount * databaseCount;
        }
        return getShardingCount(shardingAlgorithms.get(shardingStrategy.getShardingAlgorithmName()));
    }
    
    private boolean useDefaultStrategy(final ShardingStrategyConfiguration currentShardingStrategy, final ShardingRuleConfiguration ruleConfig) {
        return (null == currentShardingStrategy || Strings.isNullOrEmpty(currentShardingStrategy.getShardingAlgorithmName()))
                && null != ruleConfig.getDefaultDatabaseShardingStrategy() && null != ruleConfig.getDefaultTableShardingStrategy();
    }
    
    private int getShardingCount(final AlgorithmConfiguration algorithmConfig) {
        if (null == algorithmConfig) {
            return 0;
        }
        if (ShardingAlgorithmFactory.contains(algorithmConfig.getType())) {
            ShardingAlgorithm shardingAlgorithm = ShardingAlgorithmFactory.newInstance(algorithmConfig);
            if (shardingAlgorithm instanceof ShardingAutoTableAlgorithm) {
                shardingAlgorithm.init(algorithmConfig.getProps());
                return ((ShardingAutoTableAlgorithm) shardingAlgorithm).getAutoTablesAmount();
            }
        }
        return 0;
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
        return ShowShardingTableNodesStatement.class.getName();
    }
}
