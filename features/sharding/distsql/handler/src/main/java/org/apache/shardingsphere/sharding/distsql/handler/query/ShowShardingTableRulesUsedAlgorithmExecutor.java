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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingTableRulesUsedAlgorithmStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Show sharding table rules used algorithm executor.
 */
@Setter
public final class ShowShardingTableRulesUsedAlgorithmExecutor implements DistSQLQueryExecutor<ShowShardingTableRulesUsedAlgorithmStatement>, DistSQLExecutorRuleAware<ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowShardingTableRulesUsedAlgorithmStatement sqlStatement) {
        return Arrays.asList("type", "name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShardingTableRulesUsedAlgorithmStatement sqlStatement, final ContextManager contextManager) {
        if (!sqlStatement.getShardingAlgorithmName().isPresent()) {
            return Collections.emptyList();
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ShardingRuleConfiguration ruleConfig = rule.getConfiguration();
        String algorithmName = sqlStatement.getShardingAlgorithmName().get();
        boolean matchDefaultDatabaseShardingStrategy = null != ruleConfig.getDefaultDatabaseShardingStrategy()
                && algorithmName.equals(ruleConfig.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName());
        boolean matchDefaultTableShardingStrategy = null != ruleConfig.getDefaultTableShardingStrategy()
                && algorithmName.equals(ruleConfig.getDefaultTableShardingStrategy().getShardingAlgorithmName());
        ruleConfig.getTables().forEach(each -> {
            if (isMatchDatabaseShardingStrategy(each, algorithmName, matchDefaultDatabaseShardingStrategy) || isMatchTableShardingStrategy(each, algorithmName, matchDefaultTableShardingStrategy)) {
                result.add(new LocalDataQueryResultRow("table", each.getLogicTable()));
            }
        });
        ruleConfig.getAutoTables().forEach(each -> {
            if (null != each.getShardingStrategy() && algorithmName.equals(each.getShardingStrategy().getShardingAlgorithmName())) {
                result.add(new LocalDataQueryResultRow("auto_table", each.getLogicTable()));
            }
        });
        return result;
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
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowShardingTableRulesUsedAlgorithmStatement> getType() {
        return ShowShardingTableRulesUsedAlgorithmStatement.class;
    }
}
