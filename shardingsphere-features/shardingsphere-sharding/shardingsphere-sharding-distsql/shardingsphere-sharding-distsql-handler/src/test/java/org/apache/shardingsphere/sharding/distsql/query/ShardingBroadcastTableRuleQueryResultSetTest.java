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
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingBroadcastTableRuleQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingBroadcastTableRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule = mockShardingRule();
        when(database.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule));
        ShardingBroadcastTableRuleQueryResultSet resultSet = new ShardingBroadcastTableRuleQueryResultSet();
        resultSet.init(database, mock(ShowShardingBroadcastTableRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(1));
        assertThat(actual, is(Collections.singleton("t_order")));
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getBroadcastTables()).thenReturn(Arrays.asList("t_order", "t_order_item"));
        return result;
    }
}
