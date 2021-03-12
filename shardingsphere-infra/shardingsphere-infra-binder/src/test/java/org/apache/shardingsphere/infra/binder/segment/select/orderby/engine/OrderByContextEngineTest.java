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

package org.apache.shardingsphere.infra.binder.segment.select.orderby.engine;

import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.engine.GroupByContextEngine;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OrderByContextEngineTest {
    
    @Test
    public void assertCreateOrderByWithoutOrderByForMySQL() {
        assertCreateOrderByWithoutOrderBy(new MySQLSelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithoutOrderByForOracle() {
        assertCreateOrderByWithoutOrderBy(new OracleSelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithoutOrderByForPostgreSQL() {
        assertCreateOrderByWithoutOrderBy(new PostgreSQLSelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithoutOrderByForSQL92() {
        assertCreateOrderByWithoutOrderBy(new SQL92SelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithoutOrderByForSQLServer() {
        assertCreateOrderByWithoutOrderBy(new SQLServerSelectStatement());
    }
    
    private void assertCreateOrderByWithoutOrderBy(final SelectStatement selectStatement) {
        OrderByItem orderByItem1 = new OrderByItem(new IndexOrderByItemSegment(0, 1, 1, OrderDirection.ASC, OrderDirection.DESC));
        OrderByItem orderByItem2 = new OrderByItem(new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, OrderDirection.DESC));
        Collection<OrderByItem> orderByItems = Arrays.asList(orderByItem1, orderByItem2);
        GroupByContext groupByContext = new GroupByContext(orderByItems, 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(new ShardingSphereSchema(), selectStatement, groupByContext);
        assertThat(actualOrderByContext.getItems(), is(orderByItems));
        assertTrue(actualOrderByContext.isGenerated());
    }
    
    @Test
    public void assertCreateOrderByWithOrderByForMySQL() {
        assertCreateOrderByWithOrderBy(new MySQLSelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithOrderByForOracle() {
        assertCreateOrderByWithOrderBy(new OracleSelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithOrderByForPostgreSQL() {
        assertCreateOrderByWithOrderBy(new PostgreSQLSelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithOrderByForSQL92() {
        assertCreateOrderByWithOrderBy(new SQL92SelectStatement());
    }
    
    @Test
    public void assertCreateOrderByWithOrderByForSQLServer() {
        assertCreateOrderByWithOrderBy(new SQLServerSelectStatement());
    }
    
    private void assertCreateOrderByWithOrderBy(final SelectStatement selectStatement) {
        OrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")), OrderDirection.ASC);
        OrderByItemSegment indexOrderByItemSegment1 = new IndexOrderByItemSegment(1, 2, 2, OrderDirection.ASC, OrderDirection.DESC);
        OrderByItemSegment indexOrderByItemSegment2 = new IndexOrderByItemSegment(2, 3, 3, OrderDirection.ASC, OrderDirection.DESC);
        OrderBySegment orderBySegment = new OrderBySegment(0, 1, Arrays.asList(columnOrderByItemSegment, indexOrderByItemSegment1, indexOrderByItemSegment2));
        selectStatement.setOrderBy(orderBySegment);
        GroupByContext emptyGroupByContext = new GroupByContext(Collections.emptyList(), 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(new ShardingSphereSchema(), selectStatement, emptyGroupByContext);
        OrderByItem expectedOrderByItem1 = new OrderByItem(columnOrderByItemSegment);
        OrderByItem expectedOrderByItem2 = new OrderByItem(indexOrderByItemSegment1);
        expectedOrderByItem2.setIndex(2);
        OrderByItem expectedOrderByItem3 = new OrderByItem(indexOrderByItemSegment2);
        expectedOrderByItem3.setIndex(3);
        assertThat(actualOrderByContext.getItems(), is(Arrays.asList(expectedOrderByItem1, expectedOrderByItem2, expectedOrderByItem3)));
        assertFalse(actualOrderByContext.isGenerated());
    }
    
    @Test
    public void assertCreateOrderInDistinctByWithoutOrderByForMySQL() {
        assertCreateOrderInDistinctByWithoutOrderBy(new MySQLSelectStatement());
    }
    
    @Test
    public void assertCreateOrderInDistinctByWithoutOrderByForOracle() {
        assertCreateOrderInDistinctByWithoutOrderBy(new OracleSelectStatement());
    }
    
    @Test
    public void assertCreateOrderInDistinctByWithoutOrderByForPostgreSQL() {
        assertCreateOrderInDistinctByWithoutOrderBy(new PostgreSQLSelectStatement());
    }
    
    @Test
    public void assertCreateOrderInDistinctByWithoutOrderByForSQL92() {
        assertCreateOrderInDistinctByWithoutOrderBy(new SQL92SelectStatement());
    }
    
    @Test
    public void assertCreateOrderInDistinctByWithoutOrderByForSQLServer() {
        assertCreateOrderInDistinctByWithoutOrderBy(new SQLServerSelectStatement());
    }
    
    public void assertCreateOrderInDistinctByWithoutOrderBy(final SelectStatement selectStatement) {
        ColumnProjectionSegment columnProjectionSegment1 = new ColumnProjectionSegment(new ColumnSegment(0, 1, new IdentifierValue("column1")));
        ColumnProjectionSegment columnProjectionSegment2 = new ColumnProjectionSegment(new ColumnSegment(1, 2, new IdentifierValue("column2")));
        List<ProjectionSegment> list = Arrays.asList(columnProjectionSegment1, columnProjectionSegment2);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 1);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().addAll(list);
        selectStatement.setProjections(projectionsSegment);
        GroupByContext groupByContext = new GroupByContext(Collections.emptyList(), 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(new ShardingSphereSchema(), selectStatement, groupByContext);
        assertThat(actualOrderByContext.getItems().size(), is(list.size()));
        List<OrderByItem> items = (List<OrderByItem>) actualOrderByContext.getItems();
        assertThat(((ColumnOrderByItemSegment) items.get(0).getSegment()).getColumn(), is(columnProjectionSegment1.getColumn()));
        assertThat(((ColumnOrderByItemSegment) items.get(1).getSegment()).getColumn(), is(columnProjectionSegment2.getColumn()));
        assertTrue(actualOrderByContext.isGenerated());
    }
    
    @Test
    public void assertCreateOrderByContextForMySQLSelectWithoutOrderByOnPlainQuery() {
        SelectStatement selectStatement = mock(MySQLSelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getFrom()).thenReturn(new SimpleTableSegment(0, 1, new IdentifierValue("t_order")));
        GroupByContext groupByContext = new GroupByContext(Collections.emptyList(), 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(getShardingSphereSchemaForMySQLSelectWithoutOrderBy(), selectStatement, groupByContext);
        assertTrue(actualOrderByContext.isGenerated());
        assertThat(actualOrderByContext.getItems().size(), is(1));
        ColumnOrderByItemSegment actualItemSegment = (ColumnOrderByItemSegment) actualOrderByContext.getItems().iterator().next().getSegment();
        assertThat(actualItemSegment.getColumn().getIdentifier().getValue(), is("order_id"));
    }
    
    @Test
    public void assertCreateOrderByContextForMySQLSelectWithoutOrderByOnOrderByQuery() {
        SelectStatement selectStatement = mock(MySQLSelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getFrom()).thenReturn(new SimpleTableSegment(0, 1, new IdentifierValue("t_order")));
        when(selectStatement.getOrderBy()).thenReturn(Optional.of(new OrderBySegment(0, 1, Collections.singleton(new ColumnOrderByItemSegment(
            new ColumnSegment(0, 1, new IdentifierValue("order_id")), OrderDirection.ASC)))));
        GroupByContext groupByContext = new GroupByContext(Collections.emptyList(), 0);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(getShardingSphereSchemaForMySQLSelectWithoutOrderBy(), selectStatement, groupByContext);
        assertFalse(actualOrderByContext.isGenerated());
        assertThat(actualOrderByContext.getItems().size(), is(1));
        ColumnOrderByItemSegment actualItemSegment = (ColumnOrderByItemSegment) actualOrderByContext.getItems().iterator().next().getSegment();
        assertThat(actualItemSegment.getColumn().getIdentifier().getValue(), is("order_id"));
    }
    
    @Test
    public void assertCreateOrderByContextForMySQLSelectWithoutOrderByOnCountQuery() {
        SelectStatement selectStatement = mock(MySQLSelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getFrom()).thenReturn(new SimpleTableSegment(0, 1, new IdentifierValue("t_order")));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 1);
        projectionsSegment.setDistinctRow(false);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 1, AggregationType.COUNT, "COUNT(1)"));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        assertCreateOrderByContextForMySQLSelectWithoutOrderByOnUnsupportedQuery(selectStatement);
    }
    
    @Test
    public void assertCreateOrderByContextForMySQLSelectWithoutOrderByOnSubQuery() {
        SelectStatement selectStatement = mock(MySQLSelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getFrom()).thenReturn(mock(SubqueryTableSegment.class));
        assertCreateOrderByContextForMySQLSelectWithoutOrderByOnUnsupportedQuery(selectStatement);
    }
    
    @Test
    public void assertCreateOrderByContextForMySQLSelectWithoutOrderByOnJoinQuery() {
        SelectStatement selectStatement = mock(MySQLSelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getFrom()).thenReturn(mock(JoinTableSegment.class));
        assertCreateOrderByContextForMySQLSelectWithoutOrderByOnUnsupportedQuery(selectStatement);
    }
    
    private void assertCreateOrderByContextForMySQLSelectWithoutOrderByOnUnsupportedQuery(final SelectStatement selectStatement) {
        GroupByContext groupByContext = new GroupByContextEngine().createGroupByContext(selectStatement);
        OrderByContext actualOrderByContext = new OrderByContextEngine().createOrderBy(getShardingSphereSchemaForMySQLSelectWithoutOrderBy(), selectStatement, groupByContext);
        assertFalse(actualOrderByContext.isGenerated());
        assertThat(actualOrderByContext.getItems().size(), is(0));
    }
    
    private ShardingSphereSchema getShardingSphereSchemaForMySQLSelectWithoutOrderBy() {
        Map<String, TableMetaData> tables = new HashMap<>();
        TableMetaData orderTable = new TableMetaData(Arrays.asList(
            new ColumnMetaData("order_id", Types.INTEGER, true, true, false),
            new ColumnMetaData("user_id", Types.INTEGER, false, false, false),
            new ColumnMetaData("status", Types.VARCHAR, false, false, false)
        ), Collections.emptyList());
        tables.put("t_order", orderTable);
        return new ShardingSphereSchema(tables);
    }
}
