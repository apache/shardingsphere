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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShowBroadcastTableRuleExecutor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowBroadcastTableRuleExecutorTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule = mockShardingRule();
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        RQLExecutor<ShowBroadcastTableRulesStatement> executor = new ShowBroadcastTableRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowBroadcastTableRulesStatement.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
    }
    
    @Test
    public void assertGetColumnNames() {
        RQLExecutor<ShowBroadcastTableRulesStatement> executor = new ShowBroadcastTableRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(1));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("broadcast_table"));
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getBroadcastTables()).thenReturn(Arrays.asList("t_order", "t_order_item"));
        return result;
    }
}
