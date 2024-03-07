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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupByRowComparatorTest {
    
    private final List<Boolean> caseSensitives = Arrays.asList(false, false, false);
    
    @Test
    void assertCompareToForAscWithOrderByItemsForMySQL() throws SQLException {
        assertCompareToForAscWithOrderByItems(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithOrderByItemsForOracle() throws SQLException {
        assertCompareToForAscWithOrderByItems(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithOrderByItemsForPostgreSQL() throws SQLException {
        assertCompareToForAscWithOrderByItems(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithOrderByItemsForSQL92() throws SQLException {
        assertCompareToForAscWithOrderByItems(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithOrderByItemsForSQLServer() throws SQLException {
        assertCompareToForAscWithOrderByItems(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForAscWithOrderByItems(final GenericSelectStatement selectStatement) throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    void assertCompareToForDecsWithOrderByItemsForMySQL() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithOrderByItemsForOracle() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithOrderByItemsForPostgreSQL() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithOrderByItemsForSQL92() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithOrderByItemsForSQLServer() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForDecsWithOrderByItems(final GenericSelectStatement selectStatement) throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    void assertCompareToForEqualWithOrderByItemsForMySQL() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithOrderByItemsForOracle() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithOrderByItemsForPostgreSQL() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithOrderByItemsForSQL92() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithOrderByItemsForSQLServer() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForEqualWithOrderByItems(final GenericSelectStatement selectStatement) throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    @Test
    void assertCompareToForAscWithGroupByItemsForMySQL() throws SQLException {
        assertCompareToForAscWithGroupByItems(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithGroupByItemsForOracle() throws SQLException {
        assertCompareToForAscWithGroupByItems(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithGroupByItemsForPostgreSQL() throws SQLException {
        assertCompareToForAscWithGroupByItems(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithGroupByItemsForSQL92() throws SQLException {
        assertCompareToForAscWithGroupByItems(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscWithGroupByItemsForSQLServer() throws SQLException {
        assertCompareToForAscWithGroupByItems(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForAscWithGroupByItems(final GenericSelectStatement selectStatement) throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.emptyList()));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    void assertCompareToForDecsWithGroupByItemsForMySQL() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithGroupByItemsForOracle() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithGroupByItemsForPostgreSQL() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithGroupByItemsForSQL92() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDecsWithGroupByItemsForSQLServer() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForDecsWithGroupByItems(final GenericSelectStatement selectStatement) throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.emptyList()));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    @Test
    void assertCompareToForEqualWithGroupByItemsForMySQL() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithGroupByItemsForOracle() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithGroupByItemsForPostgreSQL() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithGroupByItemsForSQL92() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForEqualWithGroupByItemsForSQLServer() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForEqualWithGroupByItems(final GenericSelectStatement selectStatement) throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.emptyList()));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(),
                selectStatement, DefaultDatabase.LOGIC_NAME);
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    private QueryResult mockQueryResult(final Object... values) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(values.length);
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(values.length);
        int index = 0;
        for (Object each : values) {
            when(result.getValue(++index, Object.class)).thenReturn(each);
        }
        return result;
    }
}
