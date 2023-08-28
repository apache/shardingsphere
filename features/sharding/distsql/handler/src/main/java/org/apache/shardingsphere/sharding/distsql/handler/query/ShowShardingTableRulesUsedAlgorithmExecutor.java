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
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAlgorithmStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Show sharding table rules used algorithm executor.
 */
public final class ShowShardingTableRulesUsedAlgorithmExecutor implements RQLExecutor<ShowShardingTableRulesUsedAlgorithmStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShardingTableRulesUsedAlgorithmStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        rule.ifPresent(optional -> requireResult(sqlStatement, result, optional));
        return result;
    }
    
    private void requireResult(final ShowShardingTableRulesUsedAlgorithmStatement statement, final Collection<LocalDataQueryResultRow> result, final ShardingRule rule) {
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
            if (isMatchDatabaseShardingStrategy(each, algorithmName, matchDefaultDatabaseShardingStrategy) || isMatchTableShardingStrategy(each, algorithmName, matchDefaultTableShardingStrategy)) {
                result.add(new LocalDataQueryResultRow("table", each.getLogicTable()));
            }
        });
        config.getAutoTables().forEach(each -> {
            if (null != each.getShardingStrategy() && algorithmName.equals(each.getShardingStrategy().getShardingAlgorithmName())) {
                result.add(new LocalDataQueryResultRow("auto_table", each.getLogicTable()));
            }
        });
    }
    
    private boolean isMatchDatabaseShardingStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final String algorithmName, final boolean matchDefaultDatabaseShardingStrategy) {
        return isMatchDatabaseShardingStrategy(tableRuleConfig, algorithmName) || isMatchDefaultDatabaseShardingStrategy(tableRuleConfig, matchDefaultDatabaseShardingStrategy);
    }
    
    private boolean isMatchDatabaseShardingStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final String algorithmName) {
        return null != tableRuleConfig.getDatabaseShardingStrategy() && algorithmName.equals(tableRuleConfig.getDatabaseShardingStrategy().getShardingAlgorithmName());
    }
    
    private boolean isMatchDefaultDatabaseShardingStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final boolean matchDefaultDatabaseShardingStrategy) {
        return null == tableRuleConfig.getDatabaseShardingStrategy() && matchDefaultDatabaseShardingStrategy;
    }
    
    private boolean isMatchTableShardingStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final String algorithmName, final boolean matchDefaultTableShardingStrategy) {
        return isMatchTableShardingStrategy(tableRuleConfig, algorithmName) || isMatchDefaultTableShardingStrategy(tableRuleConfig, matchDefaultTableShardingStrategy);
    }
    
    private boolean isMatchTableShardingStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final String algorithmName) {
        return null != tableRuleConfig.getTableShardingStrategy() && algorithmName.equals(tableRuleConfig.getTableShardingStrategy().getShardingAlgorithmName());
    }
    
    private boolean isMatchDefaultTableShardingStrategy(final ShardingTableRuleConfiguration tableRuleConfig, final boolean matchDefaultTableShardingStrategy) {
        return null == tableRuleConfig.getTableShardingStrategy() && matchDefaultTableShardingStrategy;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "name");
    }
    
    @Override
    public Class<ShowShardingTableRulesUsedAlgorithmStatement> getType() {
        return ShowShardingTableRulesUsedAlgorithmStatement.class;
    }
}
