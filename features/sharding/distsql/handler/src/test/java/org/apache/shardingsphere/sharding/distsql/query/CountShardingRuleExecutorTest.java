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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.CountShardingRuleExecutor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CountShardingRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountShardingRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        Collection<LocalDataQueryResultRow> actual = new CountShardingRuleExecutor().getRows(mockDatabase(), mock(CountShardingRuleStatement.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("sharding_table"));
        assertThat(row.getCell(2), is("db_1"));
        assertThat(row.getCell(3), is(2));
        row = iterator.next();
        assertThat(row.getCell(1), is("sharding_table_reference"));
        assertThat(row.getCell(2), is("db_1"));
        assertThat(row.getCell(3), is(1));
    }
    
    @Test
    void assertGetColumns() {
        Collection<String> columns = new CountShardingRuleExecutor().getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("rule_name"));
        assertThat(iterator.next(), is("database"));
        assertThat(iterator.next(), is("count"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singletonList(mockShardingRule()));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        Map<String, TableRule> tableRules = new LinkedHashMap<>();
        tableRules.put("t_order_item", mock(TableRule.class));
        tableRules.put("t_order", mock(TableRule.class));
        ShardingRuleConfiguration ruleConfiguration = new ShardingRuleConfiguration();
        ShardingTableReferenceRuleConfiguration shardingTableReferenceRuleConfiguration = new ShardingTableReferenceRuleConfiguration("refRule", "ref");
        ruleConfiguration.getBindingTableGroups().add(shardingTableReferenceRuleConfiguration);
        ShardingRule result = mock(ShardingRule.class);
        when(result.getTableRules()).thenReturn(tableRules);
        when(result.getConfiguration()).thenReturn(ruleConfiguration);
        return result;
    }
}
