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
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowUnusedShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Show unused sharding algorithms executor.
 */
@Setter
public final class ShowUnusedShardingAlgorithmsExecutor implements DistSQLQueryExecutor<ShowUnusedShardingAlgorithmsStatement>, DistSQLExecutorRuleAware<ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowUnusedShardingAlgorithmsStatement sqlStatement) {
        return Arrays.asList("name", "type", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowUnusedShardingAlgorithmsStatement sqlStatement, final ContextManager contextManager) {
        ShardingRuleConfiguration shardingRuleConfig = rule.getConfiguration();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Collection<String> inUsedAlgorithms = getUsedShardingAlgorithms(shardingRuleConfig);
        for (Entry<String, AlgorithmConfiguration> entry : shardingRuleConfig.getShardingAlgorithms().entrySet()) {
            if (!inUsedAlgorithms.contains(entry.getKey())) {
                result.add(new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), entry.getValue().getProps()));
            }
        }
        return result;
    }
    
    private Collection<String> getUsedShardingAlgorithms(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().forEach(each -> {
            if (null != each.getDatabaseShardingStrategy()) {
                result.add(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
            }
            if (null != each.getTableShardingStrategy()) {
                result.add(each.getTableShardingStrategy().getShardingAlgorithmName());
            }
        });
        shardingRuleConfig.getAutoTables().stream().filter(each -> null != each.getShardingStrategy()).forEach(each -> result.add(each.getShardingStrategy().getShardingAlgorithmName()));
        ShardingStrategyConfiguration tableShardingStrategy = shardingRuleConfig.getDefaultTableShardingStrategy();
        if (null != tableShardingStrategy && !Strings.isNullOrEmpty(tableShardingStrategy.getShardingAlgorithmName())) {
            result.add(tableShardingStrategy.getShardingAlgorithmName());
        }
        ShardingStrategyConfiguration databaseShardingStrategy = shardingRuleConfig.getDefaultDatabaseShardingStrategy();
        if (null != databaseShardingStrategy && !Strings.isNullOrEmpty(databaseShardingStrategy.getShardingAlgorithmName())) {
            result.add(databaseShardingStrategy.getShardingAlgorithmName());
        }
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowUnusedShardingAlgorithmsStatement> getType() {
        return ShowUnusedShardingAlgorithmsStatement.class;
    }
}
