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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Show default sharding strategy executor.
 */
@Setter
public final class ShowDefaultShardingStrategyExecutor implements DistSQLQueryExecutor<ShowDefaultShardingStrategyStatement>, DistSQLExecutorRuleAware<ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowDefaultShardingStrategyStatement sqlStatement) {
        return Arrays.asList("name", "type", "sharding_column", "sharding_algorithm_name", "sharding_algorithm_type", "sharding_algorithm_props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowDefaultShardingStrategyStatement sqlStatement, final ContextManager contextManager) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        result.add(buildDataItem("TABLE", rule.getConfiguration(), rule.getConfiguration().getDefaultTableShardingStrategy()));
        result.add(buildDataItem("DATABASE", rule.getConfiguration(), rule.getConfiguration().getDefaultDatabaseShardingStrategy()));
        return result;
    }
    
    private LocalDataQueryResultRow buildDataItem(final String defaultType, final ShardingRuleConfiguration ruleConfig, final ShardingStrategyConfiguration strategyConfig) {
        if (null == strategyConfig) {
            return new LocalDataQueryResultRow(defaultType, "", "", "", "", "");
        }
        ShardingStrategyType strategyType = ShardingStrategyType.getValueOf(strategyConfig);
        if (strategyType == ShardingStrategyType.NONE) {
            return new LocalDataQueryResultRow(defaultType, "NONE", "", "", "", "");
        }
        AlgorithmConfiguration algorithmConfig = ruleConfig.getShardingAlgorithms().get(strategyConfig.getShardingAlgorithmName());
        Iterator<String> iterator = strategyType.getConfigurationContents(strategyConfig).iterator();
        String shardingColumn = iterator.next();
        String algorithmName = iterator.next();
        return new LocalDataQueryResultRow(defaultType, strategyType, shardingColumn, algorithmName, algorithmConfig.getType(), algorithmConfig.getProps());
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowDefaultShardingStrategyStatement> getType() {
        return ShowDefaultShardingStrategyStatement.class;
    }
}
