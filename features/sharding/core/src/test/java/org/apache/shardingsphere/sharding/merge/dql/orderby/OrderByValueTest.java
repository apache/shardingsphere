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

package org.apache.shardingsphere.sharding.merge.dql.orderby;

import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.sql.common.constant.NullsOrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MemberAccessor;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OrderByValueTest {
    
    @Test
    public void assertCompareToForAscForMySQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new MySQLSelectStatement());
    }
    
    @Test
    public void assertCompareToForAscForOracle() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new OracleSelectStatement());
    }
    
    @Test
    public void assertCompareToForAscForPostgreSQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new PostgreSQLSelectStatement());
    }
    
    @Test
    public void assertCompareToForAscForSQL92() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new SQL92SelectStatement());
    }
    
    @Test
    public void assertCompareToForAscForSQLServer() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForAsc(final SelectStatement selectStatement) throws SQLException, NoSuchFieldException, IllegalAccessException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        selectStatement.setOrderBy(createOrderBySegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderDirection.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderDirection.FIRST))),
                selectStatementContext, schema);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue1, Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("3", "4");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderDirection.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderDirection.FIRST))),
                selectStatementContext, schema);
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue2, Arrays.asList(false, false));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) < 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
    
    private static ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    @Test
    public void assertCompareToForDescForMySQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new MySQLSelectStatement());
    }
    
    @Test
    public void assertCompareToForDescForOracle() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new OracleSelectStatement());
    }
    
    @Test
    public void assertCompareToForDescForPostgreSQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new PostgreSQLSelectStatement());
    }
    
    @Test
    public void assertCompareToForDescForSQL92() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new SQL92SelectStatement());
    }
    
    @Test
    public void assertCompareToForDescForSQLServer() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForDesc(final SelectStatement selectStatement) throws SQLException, NoSuchFieldException, IllegalAccessException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        selectStatement.setOrderBy(createOrderBySegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database),
                Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getTable("table")).thenReturn(new ShardingSphereTable());
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderDirection.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderDirection.FIRST))),
                selectStatementContext, schema);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue1, Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("3", "4");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderDirection.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderDirection.FIRST))),
                selectStatementContext, schema);
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue2, Arrays.asList(false, false));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) > 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
    
    @Test
    public void assertCompareToWhenEqualForMySQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new MySQLSelectStatement());
    }
    
    @Test
    public void assertCompareToWhenEqualForOracle() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new OracleSelectStatement());
    }
    
    @Test
    public void assertCompareToWhenEqualForPostgreSQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new PostgreSQLSelectStatement());
    }
    
    @Test
    public void assertCompareToWhenEqualForSQL92() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new SQL92SelectStatement());
    }
    
    @Test
    public void assertCompareToWhenEqualForSQLServer() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new SQLServerSelectStatement());
    }
    
    private void assertCompareToWhenEqual(final SelectStatement selectStatement) throws SQLException, NoSuchFieldException, IllegalAccessException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        selectStatement.setOrderBy(createOrderBySegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database),
                Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderDirection.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderDirection.FIRST))),
                selectStatementContext, schema);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue1, Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("1", "2");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderDirection.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderDirection.FIRST))),
                selectStatementContext, schema);
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue2, Arrays.asList(false, false));
        assertTrue(orderByValue2.next());
        assertThat(orderByValue1.compareTo(orderByValue2), is(0));
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
    
    private QueryResult createQueryResult(final String... values) throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.next()).thenReturn(true, false);
        for (int i = 0; i < values.length; i++) {
            when(result.getValue(i + 1, Object.class)).thenReturn(values[i]);
        }
        return result;
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
    
    private OrderBySegment createOrderBySegment() {
        OrderByItemSegment orderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("id")), OrderDirection.ASC, NullsOrderDirection.FIRST);
        return new OrderBySegment(0, 0, Collections.singletonList(orderByItemSegment));
    }
}
