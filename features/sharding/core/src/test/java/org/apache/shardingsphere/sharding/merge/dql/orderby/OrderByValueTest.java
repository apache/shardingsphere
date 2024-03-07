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

import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MemberAccessor;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderByValueTest {
    
    @Test
    void assertCompareToForAscForMySQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscForOracle() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscForPostgreSQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscForSQL92() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForAscForSQLServer() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForAsc(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForAsc(final GenericSelectStatement selectStatement) throws SQLException, NoSuchFieldException, IllegalAccessException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(createOrderBySegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))),
                selectStatementContext, schema);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue1, Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("3", "4");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))),
                selectStatementContext, schema);
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue2, Arrays.asList(false, false));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) < 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private static ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    @Test
    void assertCompareToForDescForMySQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDescForOracle() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDescForPostgreSQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDescForSQL92() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToForDescForSQLServer() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToForDesc(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToForDesc(final GenericSelectStatement selectStatement) throws SQLException, NoSuchFieldException, IllegalAccessException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(createOrderBySegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database),
                Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getTable("table")).thenReturn(new ShardingSphereTable());
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))),
                selectStatementContext, schema);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue1, Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("3", "4");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))),
                selectStatementContext, schema);
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue2, Arrays.asList(false, false));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) > 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }
    
    @Test
    void assertCompareToWhenEqualForMySQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToWhenEqualForOracle() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCompareToWhenEqualForPostgreSQL() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCompareToWhenEqualForSQL92() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCompareToWhenEqualForSQLServer() throws SQLException, NoSuchFieldException, IllegalAccessException {
        assertCompareToWhenEqual(new SQLServerGenericSelectStatement());
    }
    
    private void assertCompareToWhenEqual(final GenericSelectStatement selectStatement) throws SQLException, NoSuchFieldException, IllegalAccessException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(createOrderBySegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database),
                Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))),
                selectStatementContext, schema);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), orderByValue1, Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("1", "2");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST)),
                createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, NullsOrderType.FIRST))),
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
        OrderByItemSegment orderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("id")), OrderDirection.ASC, NullsOrderType.FIRST);
        return new OrderBySegment(0, 0, Collections.singletonList(orderByItemSegment));
    }
}
