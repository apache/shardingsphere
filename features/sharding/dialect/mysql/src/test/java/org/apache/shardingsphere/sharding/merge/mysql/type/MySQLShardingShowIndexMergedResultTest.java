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

package org.apache.shardingsphere.sharding.merge.mysql.type;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLShardingShowIndexMergedResultTest {
    
    @Mock
    private ShardingRule rule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    void assertNextForEmptyQueryResult() throws SQLException {
        assertFalse(new MySQLShardingShowIndexMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.emptyList()).next());
    }
    
    @Test
    void assertNextForTableRuleIsPresent() throws SQLException {
        assertTrue(new MySQLShardingShowIndexMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResult())).next());
    }
    
    @Test
    void assertGetValueWithShortenedIndexName() throws SQLException {
        MySQLShardingShowIndexMergedResult actual = new MySQLShardingShowIndexMergedResult(rule, mock(SQLStatementContext.class), schema,
                Collections.singletonList(mockQueryResult(IndexMetaDataUtils.getActualIndexName("t_order_index", "t_order_0"))));
        assertTrue(actual.next());
        assertThat(actual.getValue(3, String.class), is("t_order_index"));
    }
    
    @Test
    void assertGetValueWithLogicalIndexNameEndingWithHashLikeSuffix() throws SQLException {
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(rule.findShardingTableByActualTable("t_order_0")).thenReturn(Optional.of(shardingTable));
        when(shardingTable.getLogicTable()).thenReturn("t_order");
        when(schema.getTable("t_order")).thenReturn(new ShardingSphereTable("t_order", Collections.emptyList(),
                Collections.singleton(new ShardingSphereIndex("foo_h12345678", Collections.emptyList(), false)), Collections.emptyList()));
        MySQLShardingShowIndexMergedResult actual = new MySQLShardingShowIndexMergedResult(rule, mock(SQLStatementContext.class), schema,
                Collections.singletonList(mockQueryResult("foo_h12345678")));
        assertTrue(actual.next());
        assertThat(actual.getValue(3, String.class), is("foo_h12345678"));
    }
    
    @Test
    void assertGetValueWithLogicalIndexNameEndingWithTruncationLikeSuffix() throws SQLException {
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(rule.findShardingTableByActualTable("t_order_0")).thenReturn(Optional.of(shardingTable));
        when(shardingTable.getLogicTable()).thenReturn("t_order");
        when(schema.getTable("t_order")).thenReturn(new ShardingSphereTable("t_order", Collections.emptyList(),
                Collections.singleton(new ShardingSphereIndex("foo_t12345678", Collections.emptyList(), false)), Collections.emptyList()));
        MySQLShardingShowIndexMergedResult actual = new MySQLShardingShowIndexMergedResult(rule, mock(SQLStatementContext.class), schema,
                Collections.singletonList(mockQueryResult("foo_t12345678")));
        assertTrue(actual.next());
        assertThat(actual.getValue(3, String.class), is("foo_t12345678"));
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        return mockQueryResult("t_order_index_t_order_0");
    }
    
    private QueryResult mockQueryResult(final String actualIndexName) throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(3);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn("t_order_0");
        when(result.getValue(2, Object.class)).thenReturn(1);
        when(result.getValue(3, Object.class)).thenReturn(actualIndexName);
        return result;
    }
}
