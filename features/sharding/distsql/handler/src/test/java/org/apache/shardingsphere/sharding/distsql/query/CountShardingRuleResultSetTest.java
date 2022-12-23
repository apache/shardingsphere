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

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.CountShardingRuleResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CountShardingRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountShardingRuleResultSetTest {
    
    @Test
    public void assertGetRowData() {
        CountShardingRuleResultSet resultSet = new CountShardingRuleResultSet();
        resultSet.init(mockDatabase(), mock(CountShardingRuleStatement.class));
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("sharding_table"));
        assertThat(actual.get(1), is("db_1"));
        assertThat(actual.get(2), is(2));
        assertTrue(resultSet.next());
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("sharding_table_reference"));
        assertThat(actual.get(1), is("db_1"));
        assertThat(actual.get(2), is(1));
        assertTrue(resultSet.next());
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("broadcast_table"));
        assertThat(actual.get(1), is("db_1"));
        assertThat(actual.get(2), is(2));
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = new ShardingSphereRuleMetaData(Collections.singletonList(mockShardingRule()));
        when(result.getRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
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
        when(result.getBroadcastTables()).thenReturn(Arrays.asList("broadcast_table_1", "broadcast_table_2"));
        return result;
    }
}
