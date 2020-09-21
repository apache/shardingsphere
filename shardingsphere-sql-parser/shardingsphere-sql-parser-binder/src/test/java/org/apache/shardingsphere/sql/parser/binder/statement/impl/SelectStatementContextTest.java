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

package org.apache.shardingsphere.sql.parser.binder.statement.impl;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SelectStatementContextTest {
    
    private static final String INDEX_ORDER_BY = "IndexOrderBy";
    
    private static final String COLUMN_ORDER_BY_WITH_OWNER = "ColumnOrderByWithOwner";
    
    private static final String COLUMN_ORDER_BY_WITH_ALIAS = "ColumnOrderByWithAlias";
    
    private static final String COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS = "ColumnOrderByWithoutOwnerAlias";

    @Test
    public void assertSetIndexForItemsByIndexOrderByForMySQL() {
        assertSetIndexForItemsByIndexOrderBy(new MySQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByIndexOrderByForOracle() {
        assertSetIndexForItemsByIndexOrderBy(new OracleSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByIndexOrderByForPostgreSQL() {
        assertSetIndexForItemsByIndexOrderBy(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByIndexOrderByForSQL92() {
        assertSetIndexForItemsByIndexOrderBy(new SQL92SelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByIndexOrderByForSQLServer() {
        assertSetIndexForItemsByIndexOrderBy(new SQLServerSelectStatement());
    }
    
    public void assertSetIndexForItemsByIndexOrderBy(final SelectStatement selectStatement) {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(INDEX_ORDER_BY), createProjectionsContext(), null);
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(4));
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwnerForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new MySQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwnerForOracle() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new OracleSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwnerForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwnerForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new SQL92SelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithOwnerForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithOwner(final SelectStatement selectStatement) {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_OWNER), createProjectionsContext(), null);
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(1));
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAliasForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new MySQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAliasForOracle() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new OracleSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAliasForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAliasForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new SQL92SelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithAliasForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithAlias(final SelectStatement selectStatement) {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITH_ALIAS), createProjectionsContext(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("n", 2);
        selectStatementContext.setIndexes(columnLabelIndexMap);
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(2));
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAliasForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new MySQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAliasForOracle() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new OracleSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAliasForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAliasForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new SQL92SelectStatement());
    }

    @Test
    public void assertSetIndexForItemsByColumnOrderByWithoutAliasForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithoutAlias(final SelectStatement selectStatement) {
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS), createProjectionsContext(), null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("id", 3);
        selectStatementContext.setIndexes(columnLabelIndexMap);
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }

    @Test
    public void assertIsSameGroupByAndOrderByItemsForMySQL() {
        assertIsSameGroupByAndOrderByItems(new MySQLSelectStatement());
    }

    @Test
    public void assertIsSameGroupByAndOrderByItemsForOracle() {
        assertIsSameGroupByAndOrderByItems(new OracleSelectStatement());
    }

    @Test
    public void assertIsSameGroupByAndOrderByItemsForPostgreSQL() {
        assertIsSameGroupByAndOrderByItems(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertIsSameGroupByAndOrderByItemsForSQL92() {
        assertIsSameGroupByAndOrderByItems(new SQL92SelectStatement());
    }

    @Test
    public void assertIsSameGroupByAndOrderByItemsForSQLServer() {
        assertIsSameGroupByAndOrderByItems(new SQLServerSelectStatement());
    }
    
    private void assertIsSameGroupByAndOrderByItems(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        SelectStatementContext selectStatementContext = new SelectStatementContext(null, Collections.emptyList(), selectStatement);
        assertTrue(selectStatementContext.isSameGroupByAndOrderByItems());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForMySQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new MySQLSelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForOracle() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new OracleSelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForPostgreSQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForSQL92() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new SQL92SelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForSQLServer() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new SQLServerSelectStatement());
    }
    
    private void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(null, Collections.emptyList(), selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForMySQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new MySQLSelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForOracle() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new OracleSelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForPostgreSQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForSQL92() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new SQL92SelectStatement());
    }

    @Test
    public void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForSQLServer() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new SQLServerSelectStatement());
    }
    
    private void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.DESC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.DESC))));
        SelectStatementContext selectStatementContext = new SelectStatementContext(null, Collections.emptyList(), selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }

    @Test
    public void assertSetIndexWhenAggregationProjectionsPresentForMySQL() {
        assertSetIndexWhenAggregationProjectionsPresent(new MySQLSelectStatement());
    }

    @Test
    public void assertSetIndexWhenAggregationProjectionsPresentForOracle() {
        assertSetIndexWhenAggregationProjectionsPresent(new OracleSelectStatement());
    }

    @Test
    public void assertSetIndexWhenAggregationProjectionsPresentForPostgreSQL() {
        assertSetIndexWhenAggregationProjectionsPresent(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertSetIndexWhenAggregationProjectionsPresentForSQL92() {
        assertSetIndexWhenAggregationProjectionsPresent(new SQL92SelectStatement());
    }

    @Test
    public void assertSetIndexWhenAggregationProjectionsPresentForSQLServer() {
        assertSetIndexWhenAggregationProjectionsPresent(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexWhenAggregationProjectionsPresent(final SelectStatement selectStatement) {
        AggregationProjection aggregationProjection = new AggregationProjection(AggregationType.MAX, "", "id");
        aggregationProjection.getDerivedAggregationProjections().addAll(Collections.singletonList(aggregationProjection));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(aggregationProjection));
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new GroupByContext(Collections.emptyList(), 0), createOrderBy(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS), projectionsContext, null);
        Map<String, Integer> columnLabelIndexMap = new HashMap<>();
        columnLabelIndexMap.put("id", 3);
        selectStatementContext.setIndexes(columnLabelIndexMap);
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }

    @Test
    public void assertSetWhereForMySQL() {
        assertSetWhere(new MySQLSelectStatement());
    }

    @Test
    public void assertSetWhereForOracle() {
        assertSetWhere(new OracleSelectStatement());
    }

    @Test
    public void assertSetWhereForPostgreSQL() {
        assertSetWhere(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertSetWhereForSQL92() {
        assertSetWhere(new SQL92SelectStatement());
    }

    @Test
    public void assertSetWhereForSQLServer() {
        assertSetWhere(new SQLServerSelectStatement());
    }
    
    public void assertSetWhere(final SelectStatement selectStatement) {
        WhereSegment whereSegment = mock(WhereSegment.class);
        selectStatement.setWhere(whereSegment);
        SelectStatementContext actual = new SelectStatementContext(
                selectStatement, null, null, null, null);
        assertTrue(actual.toString().startsWith(String.format("%s(super", SelectStatementContext.class.getSimpleName())));
        assertThat(actual.getTablesContext().getTables(), is(Lists.newLinkedList()));
        assertThat(actual.getAllTables(), is(Lists.newLinkedList()));
        assertNull(actual.getPaginationContext());
        assertNull(actual.getPaginationContext());
        assertNull(actual.getGroupByContext());
        assertNull(actual.getPaginationContext());
        assertThat(actual.getWhere(), is(Optional.of(whereSegment)));
    }

    @Test
    public void assertContainsSubqueryForMySQL() {
        assertContainsSubquery(new MySQLSelectStatement(), new MySQLSelectStatement());
    }

    @Test
    public void assertContainsSubqueryForOracle() {
        assertContainsSubquery(new OracleSelectStatement(), new OracleSelectStatement());
    }

    @Test
    public void assertContainsSubqueryForPostgreSQL() {
        assertContainsSubquery(new PostgreSQLSelectStatement(), new PostgreSQLSelectStatement());
    }

    @Test
    public void assertContainsSubqueryForSQL92() {
        assertContainsSubquery(new SQL92SelectStatement(), new SQL92SelectStatement());
    }

    @Test
    public void assertContainsSubqueryForSQLServer() {
        assertContainsSubquery(new SQLServerSelectStatement(), new SQLServerSelectStatement());
    }
    
    private void assertContainsSubquery(final SelectStatement selectStatement, final SelectStatement subSelectStatement) {
        SubqueryProjectionSegment projectionSegment = mock(SubqueryProjectionSegment.class);
        SubquerySegment subquery = mock(SubquerySegment.class);
        when(projectionSegment.getSubquery()).thenReturn(subquery);
        SelectStatement select = mock(SelectStatement.class);
        when(subquery.getSelect()).thenReturn(select);
        WhereSegment subwhere = mock(WhereSegment.class);
        when(select.getWhere()).thenReturn(Optional.of(subwhere));
        when(projectionSegment.getSubquery().getSelect().getWhere()).thenReturn(Optional.of(mock(WhereSegment.class)));
        WhereSegment whereSegment = new WhereSegment(0, 0, null);
        subSelectStatement.setWhere(whereSegment);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement);
        when(projectionSegment.getSubquery()).thenReturn(subquerySegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext actual = new SelectStatementContext(
                selectStatement, null, null, null, null);
        assertTrue(actual.isContainsSubquery());
    }

    @Test
    public void assertContainsSubqueryWhereEmptyForMySQL() {
        assertContainsSubqueryWhereEmpty(new MySQLSelectStatement(), new MySQLSelectStatement());
    }

    @Test
    public void assertContainsSubqueryWhereEmptyForOracle() {
        assertContainsSubqueryWhereEmpty(new OracleSelectStatement(), new OracleSelectStatement());
    }

    @Test
    public void assertContainsSubqueryWhereEmptyForPostgreSQL() {
        assertContainsSubqueryWhereEmpty(new PostgreSQLSelectStatement(), new PostgreSQLSelectStatement());
    }

    @Test
    public void assertContainsSubqueryWhereEmptyForSQL92() {
        assertContainsSubqueryWhereEmpty(new SQL92SelectStatement(), new SQL92SelectStatement());
    }

    @Test
    public void assertContainsSubqueryWhereEmptyForSQLServer() {
        assertContainsSubqueryWhereEmpty(new SQLServerSelectStatement(), new SQLServerSelectStatement());
    }
    
    private void assertContainsSubqueryWhereEmpty(final SelectStatement selectStatement, final SelectStatement subSelectStatement) {
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue("id"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, 20);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, "=", null);
        WhereSegment subWhereSegment = new WhereSegment(0, 0, expression);
        subSelectStatement.setWhere(subWhereSegment);
        SubqueryExpressionSegment subqueryExpressionSegment = new SubqueryExpressionSegment(new SubquerySegment(0, 0, subSelectStatement));
        SubqueryProjectionSegment projectionSegment = mock(SubqueryProjectionSegment.class);
        WhereSegment whereSegment = new WhereSegment(0, 0, subqueryExpressionSegment);
        selectStatement.setWhere(whereSegment);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement);
        when(projectionSegment.getSubquery()).thenReturn(subquerySegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext actual = new SelectStatementContext(
                selectStatement, null, null, null, null);
        assertTrue(actual.isContainsSubquery());
    }
    
    private OrderByContext createOrderBy(final String type) {
        OrderByItemSegment orderByItemSegment = createOrderByItemSegment(type);
        OrderByItem orderByItem = new OrderByItem(orderByItemSegment);
        return new OrderByContext(Lists.newArrayList(orderByItem), true);
    }
    
    private OrderByItemSegment createOrderByItemSegment(final String type) {
        switch (type) {
            case INDEX_ORDER_BY:
                return new IndexOrderByItemSegment(0, 0, 4, OrderDirection.ASC, OrderDirection.ASC);
            case COLUMN_ORDER_BY_WITH_OWNER:
                ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
                columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
                return new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, OrderDirection.ASC);
            case COLUMN_ORDER_BY_WITH_ALIAS:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("n")), OrderDirection.ASC, OrderDirection.ASC);
            default:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("id")), OrderDirection.ASC, OrderDirection.ASC);
        }
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
