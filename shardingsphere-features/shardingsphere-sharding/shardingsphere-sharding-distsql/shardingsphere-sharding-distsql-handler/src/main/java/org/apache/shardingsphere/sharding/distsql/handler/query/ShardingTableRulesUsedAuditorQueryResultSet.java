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
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAuditorStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Query result set for show sharding table rules used auditor.
 */
public final class ShardingTableRulesUsedAuditorQueryResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ShowShardingTableRulesUsedAuditorStatement statement = (ShowShardingTableRulesUsedAuditorStatement) sqlStatement;
        List<Collection<Object>> result = new ArrayList<>();
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        rule.ifPresent(optional -> requireResult(statement, result, optional));
        data = result.iterator();
    }
    
    private void requireResult(final ShowShardingTableRulesUsedAuditorStatement statement, final List<Collection<Object>> result, final ShardingRule rule) {
        if (!statement.getAuditorName().isPresent()) {
            return;
        }
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) rule.getConfiguration();
        config.getTables().forEach(each -> {
            if (null != each.getKeyGenerateStrategy() && statement.getAuditorName().get().equals(each.getKeyGenerateStrategy().getKeyGeneratorName())) {
                result.add(Arrays.asList("table", each.getLogicTable()));
            }
        });
        config.getAutoTables().forEach(each -> {
            if (null != each.getKeyGenerateStrategy() && statement.getAuditorName().get().equals(each.getKeyGenerateStrategy().getKeyGeneratorName())) {
                result.add(Arrays.asList("auto_table", each.getLogicTable()));
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
        return ShowShardingTableRulesUsedAuditorStatement.class.getName();
    }
}
