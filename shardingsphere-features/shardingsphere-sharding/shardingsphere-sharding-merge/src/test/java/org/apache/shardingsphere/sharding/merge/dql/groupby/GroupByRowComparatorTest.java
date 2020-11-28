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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.ExecuteQueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByRowComparatorTest {
    
    private final List<Boolean> caseSensitives = Lists.newArrayList(false, false, false);

    @Test
    public void assertCompareToForAscWithOrderByItemsForMySQL() throws SQLException {
        assertCompareToForAscWithOrderByItems(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToForAscWithOrderByItemsForOracle() throws SQLException {
        assertCompareToForAscWithOrderByItems(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForAscWithOrderByItemsForPostgreSQL() throws SQLException {
        assertCompareToForAscWithOrderByItems(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForAscWithOrderByItemsForSQL92() throws SQLException {
        assertCompareToForAscWithOrderByItems(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForAscWithOrderByItemsForSQLServer() throws SQLException {
        assertCompareToForAscWithOrderByItems(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForAscWithOrderByItems(final SelectStatement selectStatement) throws SQLException {
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)), 
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }

    @Test
    public void assertCompareToForDecsWithOrderByItemsForMySQL() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithOrderByItemsForOracle() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithOrderByItemsForPostgreSQL() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithOrderByItemsForSQL92() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithOrderByItemsForSQLServer() throws SQLException {
        assertCompareToForDecsWithOrderByItems(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForDecsWithOrderByItems(final SelectStatement selectStatement) throws SQLException {
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }

    @Test
    public void assertCompareToForEqualWithOrderByItemsForMySQL() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithOrderByItemsForOracle() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithOrderByItemsForPostgreSQL() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithOrderByItemsForSQL92() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithOrderByItemsForSQLServer() throws SQLException {
        assertCompareToForEqualWithOrderByItems(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForEqualWithOrderByItems(final SelectStatement selectStatement) throws SQLException {
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }

    @Test
    public void assertCompareToForAscWithGroupByItemsForMySQL() throws SQLException {
        assertCompareToForAscWithGroupByItems(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToForAscWithGroupByItemsForOracle() throws SQLException {
        assertCompareToForAscWithGroupByItems(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForAscWithGroupByItemsForPostgreSQL() throws SQLException {
        assertCompareToForAscWithGroupByItems(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForAscWithGroupByItemsForSQL92() throws SQLException {
        assertCompareToForAscWithGroupByItems(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForAscWithGroupByItemsForSQLServer() throws SQLException {
        assertCompareToForAscWithGroupByItems(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForAscWithGroupByItems(final SelectStatement selectStatement) throws SQLException {
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0), new OrderByContext(Collections.emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }

    @Test
    public void assertCompareToForDecsWithGroupByItemsForMySQL() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithGroupByItemsForOracle() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithGroupByItemsForPostgreSQL() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithGroupByItemsForSQL92() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForDecsWithGroupByItemsForSQLServer() throws SQLException {
        assertCompareToForDecsWithGroupByItems(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForDecsWithGroupByItems(final SelectStatement selectStatement) throws SQLException {
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0), new OrderByContext(Collections.emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }

    @Test
    public void assertCompareToForEqualWithGroupByItemsForMySQL() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithGroupByItemsForOracle() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithGroupByItemsForPostgreSQL() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithGroupByItemsForSQL92() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForEqualWithGroupByItemsForSQLServer() throws SQLException {
        assertCompareToForEqualWithGroupByItems(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForEqualWithGroupByItems(final SelectStatement selectStatement) throws SQLException {
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0), new OrderByContext(Collections.emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
    
    private ExecuteQueryResult mockQueryResult(final Object... values) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(values.length);
        ExecuteQueryResult result = mock(ExecuteQueryResult.class);
        when(result.getColumnCount()).thenReturn(values.length);
        int index = 0;
        for (Object each : values) {
            when(result.getValue(++index, Object.class)).thenReturn(each);
        }
        return result;
    }
}
