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

import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAlgorithmStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Query result set for show sharding table rules used algorithm.
 */
public final class ShardingTableRulesUsedAlgorithmQueryResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ShowShardingTableRulesUsedAlgorithmStatement statement = (ShowShardingTableRulesUsedAlgorithmStatement) sqlStatement;
        Collection<Collection<Object>> data = new LinkedList<>();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        rule.ifPresent(optional -> requireResult(statement, data, optional));
        this.data = data.iterator();
    }
    
    private void requireResult(final ShowShardingTableRulesUsedAlgorithmStatement statement, final Collection<Collection<Object>> data, final ShardingRule rule) {
        if (!statement.getShardingAlgorithmName().isPresent()) {
            return;
        }
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.getConfiguration();
        String algorithmName = statement.getShardingAlgorithmName().get();
        boolean matchDefaultDatabaseShardingStrategy = null != config.getDefaultDatabaseShardingStrategy()
                && algorithmName.equals(config.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName());
        boolean matchDefaultTableShardingStrategy = null != config.getDefaultTableShardingStrategy()
                && algorithmName.equals(config.getDefaultTableShardingStrategy().getShardingAlgorithmName());
        config.getTables().forEach(each -> {
            if (((null == each.getDatabaseShardingStrategy() && matchDefaultDatabaseShardingStrategy)
                    || (null != each.getDatabaseShardingStrategy() && algorithmName.equals(each.getDatabaseShardingStrategy().getShardingAlgorithmName())))
                    || ((null == each.getTableShardingStrategy() && matchDefaultTableShardingStrategy)
                            || (null != each.getTableShardingStrategy() && algorithmName.equals(each.getTableShardingStrategy().getShardingAlgorithmName())))) {
                data.add(Arrays.asList("table", each.getLogicTable()));
            }
        });
        config.getAutoTables().forEach(each -> {
            if (null != each.getShardingStrategy() && algorithmName.equals(each.getShardingStrategy().getShardingAlgorithmName())) {
                data.add(Arrays.asList("auto_table", each.getLogicTable()));
            }
        });
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "name");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowShardingTableRulesUsedAlgorithmStatement.class.getName();
    }
}
