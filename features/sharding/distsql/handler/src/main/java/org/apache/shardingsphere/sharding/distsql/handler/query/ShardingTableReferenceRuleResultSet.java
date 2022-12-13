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

import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableReferenceRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Result set for show sharding table reference rules.
 */
public final class ShardingTableReferenceRuleResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<ShardingTableReferenceRuleConfiguration> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        rule.ifPresent(optional -> data = buildData((ShardingRuleConfiguration) optional.getConfiguration()).iterator());
    }
    
    private Collection<ShardingTableReferenceRuleConfiguration> buildData(final ShardingRuleConfiguration ruleConfig) {
        return ruleConfig.getBindingTableGroups();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "sharding_table_reference");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildTableRowData(data.next());
    }
    
    private Collection<Object> buildTableRowData(final ShardingTableReferenceRuleConfiguration ruleConfig) {
        return Arrays.asList(ruleConfig.getName(), ruleConfig.getReference());
    }
    
    @Override
    public String getType() {
        return ShowShardingTableReferenceRulesStatement.class.getName();
    }
}
