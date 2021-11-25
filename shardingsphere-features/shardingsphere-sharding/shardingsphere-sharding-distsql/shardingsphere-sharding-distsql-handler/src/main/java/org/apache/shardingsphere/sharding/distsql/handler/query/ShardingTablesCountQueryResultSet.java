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

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTablesCountStatement;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Result set for show sharding table count.
 */
public final class ShardingTablesCountQueryResultSet implements DistSQLResultSet {
    
    private static final String TABLE = "table";
    
    private static final String COUNT = "count";
    
    private Iterator<Entry<String, Integer>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        metaData.getRuleMetaData().getConfigurations().stream().filter(each -> each instanceof ShardingRuleConfiguration)
                .map(each -> (ShardingRuleConfiguration) each).forEach(each -> data = getDataSourceCount(each).entrySet().iterator());
    }
    
    private Map<String, Integer> getDataSourceCount(final ShardingRuleConfiguration config) {
        Map<String, Integer> result = new LinkedHashMap<>();
        Map<String, Integer> tableCount = config.getTables().stream()
                .collect(Collectors.toMap(ShardingTableRuleConfiguration::getLogicTable, this::getCount, Integer::sum, LinkedHashMap::new));
        Map<String, Integer> autoTableCount = config.getAutoTables().stream()
                .collect(Collectors.toMap(ShardingAutoTableRuleConfiguration::getLogicTable, this::getCount, Integer::sum, LinkedHashMap::new));
        result.putAll(tableCount);
        result.putAll(autoTableCount);
        return result;
    }
    
    private Integer getCount(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        List<String> actualDataSources = new InlineExpressionParser(shardingAutoTableRuleConfig.getActualDataSources())
                .splitAndEvaluate();
        return actualDataSources.size();
    }
    
    private Integer getCount(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        List<String> actualDataNodes = new InlineExpressionParser(shardingTableRuleConfig.getActualDataNodes())
                .splitAndEvaluate();
        return (int) actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).count();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(TABLE, COUNT);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, Integer> entry = data.next();
        return Arrays.asList(entry.getKey(), entry.getValue());
    }
    
    @Override
    public String getType() {
        return ShowShardingTablesCountStatement.class.getCanonicalName();
    }
}
