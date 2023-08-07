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
import org.apache.shardingsphere.sharding.distsql.parser.statement.CountShardingRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Count sharding rule executor.
 */
public final class CountShardingRuleExecutor implements RQLExecutor<CountShardingRuleStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "database", "count");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final CountShardingRuleStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        rule.ifPresent(optional -> fillRows(result, optional, database.getName()));
        return result;
    }
    
    private void fillRows(final Collection<LocalDataQueryResultRow> result, final ShardingRule rule, final String databaseName) {
        fillRows(result, "sharding_table", databaseName, rule.getTableRules().size());
        fillRows(result, "sharding_table_reference", databaseName, ((ShardingRuleConfiguration) rule.getConfiguration()).getBindingTableGroups().size());
    }
    
    private void fillRows(final Collection<LocalDataQueryResultRow> result, final String ruleName, final String databaseName, final int count) {
        result.add(new LocalDataQueryResultRow(ruleName, databaseName, count));
    }
    
    @Override
    public Class<CountShardingRuleStatement> getType() {
        return CountShardingRuleStatement.class;
    }
}
