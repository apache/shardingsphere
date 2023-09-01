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
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAuditorStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Show sharding table rules used auditor executor.
 */
public final class ShowShardingTableRulesUsedAuditorExecutor implements RQLExecutor<ShowShardingTableRulesUsedAuditorStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShardingTableRulesUsedAuditorStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        database.getRuleMetaData().findSingleRule(ShardingRule.class).ifPresent(optional -> requireResult(sqlStatement, result, optional));
        return result;
    }
    
    private void requireResult(final ShowShardingTableRulesUsedAuditorStatement statement, final Collection<LocalDataQueryResultRow> result, final ShardingRule rule) {
        if (!statement.getAuditorName().isPresent()) {
            return;
        }
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.getConfiguration();
        config.getTables().forEach(each -> {
            if (null != each.getAuditStrategy() && each.getAuditStrategy().getAuditorNames().contains(statement.getAuditorName().get())) {
                result.add(new LocalDataQueryResultRow("table", each.getLogicTable()));
            }
        });
        config.getAutoTables().forEach(each -> {
            if (null != each.getAuditStrategy() && each.getAuditStrategy().getAuditorNames().contains(statement.getAuditorName().get())) {
                result.add(new LocalDataQueryResultRow("auto_table", each.getLogicTable()));
            }
        });
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "name");
    }
    
    @Override
    public Class<ShowShardingTableRulesUsedAuditorStatement> getType() {
        return ShowShardingTableRulesUsedAuditorStatement.class;
    }
}
