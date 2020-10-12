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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.metadata.database.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
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
import org.mockito.internal.util.reflection.FieldSetter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OrderByValueTest {

    @Test
    public void assertCompareToForAscForMySQL() throws SQLException, NoSuchFieldException {
        assertCompareToForAsc(new MySQLSelectStatement());   
    }

    @Test
    public void assertCompareToForAscForOracle() throws SQLException, NoSuchFieldException {
        assertCompareToForAsc(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForAscForPostgreSQL() throws SQLException, NoSuchFieldException {
        assertCompareToForAsc(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForAscForSQL92() throws SQLException, NoSuchFieldException {
        assertCompareToForAsc(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForAscForSQLServer() throws SQLException, NoSuchFieldException {
        assertCompareToForAsc(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForAsc(final SelectStatement selectStatement) throws SQLException, NoSuchFieldException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(
            selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(), createProjectionsContext(), null);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))),
            selectStatementContext, schemaMetaData);
        FieldSetter.setField(orderByValue1, OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("3", "4");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))),
            selectStatementContext, schemaMetaData);
        FieldSetter.setField(orderByValue2, OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), Arrays.asList(false, false));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) < 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }

    @Test
    public void assertCompareToForDescForMySQL() throws SQLException, NoSuchFieldException {
        assertCompareToForDesc(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToForDescForOracle() throws SQLException, NoSuchFieldException {
        assertCompareToForDesc(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToForDescForPostgreSQL() throws SQLException, NoSuchFieldException {
        assertCompareToForDesc(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToForDescForSQL92() throws SQLException, NoSuchFieldException {
        assertCompareToForDesc(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToForDescForSQLServer() throws SQLException, NoSuchFieldException {
        assertCompareToForDesc(new SQLServerSelectStatement());
    }
    
    private void assertCompareToForDesc(final SelectStatement selectStatement) throws SQLException, NoSuchFieldException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(
            selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(), createProjectionsContext(), null);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))),
            selectStatementContext, schemaMetaData);
        FieldSetter.setField(orderByValue1, OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("3", "4");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))),
            selectStatementContext, schemaMetaData);
        FieldSetter.setField(orderByValue2, OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), Arrays.asList(false, false));
        assertTrue(orderByValue2.next());
        assertTrue(orderByValue1.compareTo(orderByValue2) > 0);
        assertFalse(orderByValue1.getQueryResult().next());
        assertFalse(orderByValue2.getQueryResult().next());
    }

    @Test
    public void assertCompareToWhenEqualForMySQL() throws SQLException, NoSuchFieldException {
        assertCompareToWhenEqual(new MySQLSelectStatement());
    }

    @Test
    public void assertCompareToWhenEqualForOracle() throws SQLException, NoSuchFieldException {
        assertCompareToWhenEqual(new OracleSelectStatement());
    }

    @Test
    public void assertCompareToWhenEqualForPostgreSQL() throws SQLException, NoSuchFieldException {
        assertCompareToWhenEqual(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCompareToWhenEqualForSQL92() throws SQLException, NoSuchFieldException {
        assertCompareToWhenEqual(new SQL92SelectStatement());
    }

    @Test
    public void assertCompareToWhenEqualForSQLServer() throws SQLException, NoSuchFieldException {
        assertCompareToWhenEqual(new SQLServerSelectStatement());
    }
    
    private void assertCompareToWhenEqual(final SelectStatement selectStatement) throws SQLException, NoSuchFieldException {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(
            selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(), createProjectionsContext(), null);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        QueryResult queryResult1 = createQueryResult("1", "2");
        OrderByValue orderByValue1 = new OrderByValue(queryResult1, Arrays.asList(
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))),
            selectStatementContext, schemaMetaData);
        FieldSetter.setField(orderByValue1, OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), Arrays.asList(false, false));
        assertTrue(orderByValue1.next());
        QueryResult queryResult2 = createQueryResult("1", "2");
        OrderByValue orderByValue2 = new OrderByValue(queryResult2, Arrays.asList(
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
            createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))),
            selectStatementContext, schemaMetaData);
        FieldSetter.setField(orderByValue2, OrderByValue.class.getDeclaredField("orderValuesCaseSensitive"), Arrays.asList(false, false));
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
    
    private OrderByContext createOrderBy() {
        OrderByItemSegment orderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("id")), OrderDirection.ASC, OrderDirection.ASC);
        OrderByItem orderByItem = new OrderByItem(orderByItemSegment);
        return new OrderByContext(Lists.newArrayList(orderByItem), true);
    }
    
    private ProjectionsContext createProjectionsContext() {
        return new ProjectionsContext(
            0, 0, true, Arrays.asList(getColumnProjectionWithoutOwner(), getColumnProjectionWithoutOwner(true), getColumnProjectionWithoutOwner(false)));
    }
    
    private Projection getColumnProjectionWithoutOwner() {
        return new ColumnProjection("table", "name", null);
    }
    
    private Projection getColumnProjectionWithoutOwner(final boolean hasAlias) {
        return new ColumnProjection(null, hasAlias ? "name" : "id", hasAlias ? "n" : null);
    }
}
