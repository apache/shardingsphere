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
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingTableRulesUsedKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Show sharding table rules used key generator executor.
 */
@Setter
public final class ShowShardingTableRulesUsedKeyGeneratorExecutor implements DistSQLQueryExecutor<ShowShardingTableRulesUsedKeyGeneratorStatement>, DistSQLExecutorRuleAware<ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowShardingTableRulesUsedKeyGeneratorStatement sqlStatement) {
        return Arrays.asList("type", "name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShardingTableRulesUsedKeyGeneratorStatement sqlStatement, final ContextManager contextManager) {
        if (!sqlStatement.getKeyGeneratorName().isPresent()) {
            return Collections.emptyList();
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        rule.getConfiguration().getTables().forEach(each -> {
            if (null != each.getKeyGenerateStrategy() && sqlStatement.getKeyGeneratorName().get().equals(each.getKeyGenerateStrategy().getKeyGeneratorName())) {
                result.add(new LocalDataQueryResultRow("table", each.getLogicTable()));
            }
        });
        rule.getConfiguration().getAutoTables().forEach(each -> {
            if (null != each.getKeyGenerateStrategy() && sqlStatement.getKeyGeneratorName().get().equals(each.getKeyGenerateStrategy().getKeyGeneratorName())) {
                result.add(new LocalDataQueryResultRow("auto_table", each.getLogicTable()));
            }
        });
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowShardingTableRulesUsedKeyGeneratorStatement> getType() {
        return ShowShardingTableRulesUsedKeyGeneratorStatement.class;
    }
}
