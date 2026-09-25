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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.SQLBuilderEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingOrderByTokenGeneratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final RouteUnit routeUnit = new RouteUnit(new RouteMapper("foo_db", "foo_db_0"), Collections.singleton(new RouteMapper("t_account", "t_account_0")));
    
    private final RouteUnit otherRouteUnit = new RouteUnit(new RouteMapper("foo_db", "foo_db_1"), Collections.singleton(new RouteMapper("t_account", "t_account_1")));
    
    private final ShardingRule rule = mock(ShardingRule.class);
    
    private ShardingOrderByTokenGenerator generator;
    
    @BeforeEach
    void setUp() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        routeContext.getRouteUnits().add(otherRouteUnit);
        generator = new ShardingOrderByTokenGenerator(rule);
        generator.setRouteContext(routeContext);
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotGeneratedOrderByContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithGeneratedOrderByContext() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getOrderByContext().isGenerated()).thenReturn(true);
        assertTrue(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokenWithWindow() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getWindow()).thenReturn(Optional.of(new WindowSegment(0, 10)));
        OrderByToken actual = generator.generateSQLToken(mockSelectStatementContext(selectStatement, createOrderByItems()));
        assertThat(actual.toString(routeUnit), is(" ORDER BY foo_col ASC,foo_expr ASC,5 ASC "));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithHaving() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getHaving()).thenReturn(Optional.of(new HavingSegment(0, 10, mock())));
        OrderByToken actual = generator.generateSQLToken(mockSelectStatementContext(selectStatement, createOrderByItems()));
        assertThat(actual.toString(routeUnit), is(" ORDER BY foo_col ASC,foo_expr ASC,5 ASC "));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithGroupBy() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getGroupBy()).thenReturn(Optional.of(new GroupBySegment(0, 10, Collections.emptyList())));
        OrderByToken actual = generator.generateSQLToken(mockSelectStatementContext(selectStatement, createOrderByItems()));
        assertThat(actual.toString(routeUnit), is(" ORDER BY foo_col ASC,foo_expr ASC,5 ASC "));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithWhere() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 10, mock())));
        OrderByToken actual = generator.generateSQLToken(mockSelectStatementContext(selectStatement, createOrderByItems()));
        assertThat(actual.toString(routeUnit), is(" ORDER BY foo_col ASC,foo_expr ASC,5 ASC "));
        assertThat(actual.getStopIndex(), is(11));
    }
    
    @Test
    void assertGenerateSQLTokenWithNothing() {
        OrderByToken actual = generator.generateSQLToken(mockSelectStatementContext(mock(SelectStatement.class), createOrderByItems()));
        assertThat(actual.toString(routeUnit), is(" ORDER BY foo_col ASC,foo_expr ASC,5 ASC "));
        assertThat(actual.getStopIndex(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithTableOwner() {
        SelectStatementContext selectStatementContext = mockSelectStatementContext(mock(SelectStatement.class), Collections.singleton(createColumnOrderByItem("t_account", "status")));
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.toString(routeUnit), is(" ORDER BY t_account_0.status ASC "));
        assertThat(actual.toString(otherRouteUnit), is(" ORDER BY t_account_1.status ASC "));
    }
    
    @Test
    void assertGenerateSQLTokenWithQuotedTableOwner() {
        SelectStatementContext selectStatementContext = mockSelectStatementContext(mock(SelectStatement.class), Collections.singleton(createColumnOrderByItem("`t_account`", "`status`")));
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.toString(routeUnit), is(" ORDER BY `t_account_0`.`status` ASC "));
    }
    
    @Test
    void assertGenerateSQLTokenWithBindingTableOwner() {
        Collection<String> tableNames = Arrays.asList("t_account", "t_account_detail");
        when(rule.getLogicAndActualTablesFromBindingTable("foo_db", "t_account", "t_account_0", tableNames)).thenReturn(Collections.singletonMap("t_account_detail", "t_account_detail_0"));
        SelectStatementContext selectStatementContext = mockSelectStatementContext(mock(SelectStatement.class),
                Arrays.asList(createColumnOrderByItem("t_account", "status"), createColumnOrderByItem("t_account_detail", "amount")));
        when(selectStatementContext.getTablesContext().getTableNames()).thenReturn(tableNames);
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.toString(routeUnit), is(" ORDER BY t_account_0.status ASC,t_account_detail_0.amount ASC "));
    }
    
    @Test
    void assertGenerateSQLTokenWithAliasOwner() {
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_account")));
        table.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).from(table).build();
        OrderByToken actual = generator.generateSQLToken(mockSelectStatementContext(selectStatement, Collections.singleton(createColumnOrderByItem("a", "status"))));
        assertThat(actual.toString(routeUnit), is(" ORDER BY a.status ASC "));
    }
    
    @Test
    void assertGenerateSQLTokenWithNotRoutedTableOwner() {
        SelectStatementContext selectStatementContext = mockSelectStatementContext(mock(SelectStatement.class), Collections.singleton(createColumnOrderByItem("t_single", "status")));
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(actual.toString(routeUnit), is(" ORDER BY t_single.status ASC "));
    }
    
    @Test
    void assertGenerateSQLTokenWithoutRouteUnits() {
        generator.setRouteContext(new RouteContext());
        SelectStatementContext selectStatementContext = mockSelectStatementContext(mock(SelectStatement.class), Collections.singleton(createColumnOrderByItem("t_account", "status")));
        when(selectStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(new SimpleTableSegment(new TableNameSegment(14, 22, new IdentifierValue("t_account")))));
        OrderByToken actual = generator.generateSQLToken(selectStatementContext);
        assertThat(new SQLBuilderEngine("SELECT * FROM t_account", Collections.singletonList(actual)).buildSQL(), is("SELECT * FROM t_account ORDER BY t_account.status ASC "));
    }
    
    private SelectStatementContext mockSelectStatementContext(final SelectStatement selectStatement, final Collection<OrderByItem> orderByItems) {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(selectStatement);
        when(result.getOrderByContext().getItems()).thenReturn(orderByItems);
        return result;
    }
    
    private Collection<OrderByItem> createOrderByItems() {
        OrderByItem indexOrderByItem = new OrderByItem(new IndexOrderByItemSegment(0, 0, 5, OrderDirection.ASC, NullsOrderType.FIRST));
        indexOrderByItem.setIndex(5);
        return Arrays.asList(new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("foo_col")), OrderDirection.ASC, NullsOrderType.FIRST)),
                new OrderByItem(new ExpressionOrderByItemSegment(0, 0, "foo_expr", OrderDirection.ASC, NullsOrderType.FIRST)), indexOrderByItem);
    }
    
    private OrderByItem createColumnOrderByItem(final String owner, final String column) {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue(column));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        return new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
    }
}
