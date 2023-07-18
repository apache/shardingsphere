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

package org.apache.shardingsphere.infra.binder.statement.dml;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectStatementContextTest {
    
    private static final String INDEX_ORDER_BY = "IndexOrderBy";
    
    private static final String COLUMN_ORDER_BY_WITH_OWNER = "ColumnOrderByWithOwner";
    
    private static final String COLUMN_ORDER_BY_WITH_ALIAS = "ColumnOrderByWithAlias";
    
    private static final String COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS = "ColumnOrderByWithoutOwnerAlias";
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForMySQL() {
        assertSetIndexForItemsByIndexOrderBy(new MySQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForOracle() {
        assertSetIndexForItemsByIndexOrderBy(new OracleSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForPostgreSQL() {
        assertSetIndexForItemsByIndexOrderBy(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForSQL92() {
        assertSetIndexForItemsByIndexOrderBy(new SQL92SelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForSQLServer() {
        assertSetIndexForItemsByIndexOrderBy(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexForItemsByIndexOrderBy(final SelectStatement selectStatement) {
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(INDEX_ORDER_BY))));
        selectStatement.setProjections(createProjectionsSegment());
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("table"))));
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(4));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        TableContainedRule tableContainedRule = mock(TableContainedRule.class, RETURNS_DEEP_STUBS);
        when(tableContainedRule.getEnhancedTableMapper().contains("t_order")).thenReturn(true);
        when(result.getRuleMetaData().findRules(TableContainedRule.class)).thenReturn(Collections.singletonList(tableContainedRule));
        return result;
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new MySQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForOracle() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new OracleSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new SQL92SelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithOwner(final SelectStatement selectStatement) {
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITH_OWNER))));
        selectStatement.setProjections(createProjectionsSegment());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("table")));
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue(DefaultDatabase.LOGIC_NAME.toUpperCase())));
        selectStatement.setFrom(tableSegment);
        ShardingSphereDatabase database = mockDatabase();
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(1));
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new MySQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForOracle() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new OracleSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new SQL92SelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithAlias(final SelectStatement selectStatement) {
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITH_ALIAS))));
        selectStatement.setProjections(createProjectionsSegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.singletonMap("n", 2));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(2));
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new MySQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForOracle() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new OracleSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new SQL92SelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithoutAlias(final SelectStatement selectStatement) {
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS))));
        selectStatement.setProjections(createProjectionsSegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.singletonMap("id", 3));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForMySQL() {
        assertIsSameGroupByAndOrderByItems(new MySQLSelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForOracle() {
        assertIsSameGroupByAndOrderByItems(new OracleSelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForPostgreSQL() {
        assertIsSameGroupByAndOrderByItems(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForSQL92() {
        assertIsSameGroupByAndOrderByItems(new SQL92SelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForSQLServer() {
        assertIsSameGroupByAndOrderByItems(new SQLServerSelectStatement());
    }
    
    private void assertIsSameGroupByAndOrderByItems(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertTrue(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    private SelectStatementContext createSelectStatementContext(final SelectStatement selectStatement) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        return new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForMySQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new MySQLSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForOracle() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new OracleSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForPostgreSQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForSQL92() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new SQL92SelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForSQLServer() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new SQLServerSelectStatement());
    }
    
    private void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForMySQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new MySQLSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForOracle() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new OracleSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForPostgreSQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForSQL92() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new SQL92SelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForSQLServer() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new SQLServerSelectStatement());
    }
    
    private void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.LAST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForMySQL() {
        assertSetIndexWhenAggregationProjectionsPresent(new MySQLSelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForOracle() {
        assertSetIndexWhenAggregationProjectionsPresent(new OracleSelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForPostgreSQL() {
        assertSetIndexWhenAggregationProjectionsPresent(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForSQL92() {
        assertSetIndexWhenAggregationProjectionsPresent(new SQL92SelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForSQLServer() {
        assertSetIndexWhenAggregationProjectionsPresent(new SQLServerSelectStatement());
    }
    
    private void assertSetIndexWhenAggregationProjectionsPresent(final SelectStatement selectStatement) {
        final ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 0, AggregationType.MAX, "");
        aggregationProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("id")));
        projectionsSegment.getProjections().add(aggregationProjectionSegment);
        selectStatement.setProjections(projectionsSegment);
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.singletonMap("id", 3));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    void assertSetWhereForMySQL() {
        assertSetWhere(new MySQLSelectStatement());
    }
    
    @Test
    void assertSetWhereForOracle() {
        assertSetWhere(new OracleSelectStatement());
    }
    
    @Test
    void assertSetWhereForPostgreSQL() {
        assertSetWhere(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertSetWhereForSQL92() {
        assertSetWhere(new SQL92SelectStatement());
    }
    
    @Test
    void assertSetWhereForSQLServer() {
        assertSetWhere(new SQLServerSelectStatement());
    }
    
    private void assertSetWhere(final SelectStatement selectStatement) {
        WhereSegment whereSegment = mock(WhereSegment.class);
        selectStatement.setWhere(whereSegment);
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext actual = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.emptySet()));
        assertThat(actual.getAllTables(), is(Collections.emptyList()));
        assertThat(actual.getGroupByContext().getItems(), is(Collections.emptyList()));
        assertThat(actual.getWhereSegments(), is(Collections.singletonList(whereSegment)));
    }
    
    @Test
    void assertContainsSubqueryForMySQL() {
        assertContainsSubquery(new MySQLSelectStatement(), new MySQLSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForOracle() {
        assertContainsSubquery(new OracleSelectStatement(), new OracleSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForPostgreSQL() {
        assertContainsSubquery(new PostgreSQLSelectStatement(), new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForSQL92() {
        assertContainsSubquery(new SQL92SelectStatement(), new SQL92SelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForSQLServer() {
        assertContainsSubquery(new SQLServerSelectStatement(), new SQLServerSelectStatement());
    }
    
    private void assertContainsSubquery(final SelectStatement selectStatement, final SelectStatement subSelectStatement) {
        WhereSegment whereSegment = new WhereSegment(0, 0, null);
        subSelectStatement.setWhere(whereSegment);
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        subSelectStatement.setProjections(subqueryProjections);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement);
        SubqueryProjectionSegment subqueryProjectionSegment = new SubqueryProjectionSegment(subquerySegment, "");
        projectionsSegment.getProjections().add(subqueryProjectionSegment);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mockDatabase();
        assertTrue(new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME).isContainsSubquery());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForMySQL() {
        assertContainsSubqueryWhereEmpty(new MySQLSelectStatement(), new MySQLSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForOracle() {
        assertContainsSubqueryWhereEmpty(new OracleSelectStatement(), new OracleSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForPostgreSQL() {
        assertContainsSubqueryWhereEmpty(new PostgreSQLSelectStatement(), new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForSQL92() {
        assertContainsSubqueryWhereEmpty(new SQL92SelectStatement(), new SQL92SelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForSQLServer() {
        assertContainsSubqueryWhereEmpty(new SQLServerSelectStatement(), new SQLServerSelectStatement());
    }
    
    private void assertContainsSubqueryWhereEmpty(final SelectStatement selectStatement, final SelectStatement subSelectStatement) {
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue("id"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, 20);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, "=", null);
        WhereSegment subWhereSegment = new WhereSegment(0, 0, expression);
        subSelectStatement.setWhere(subWhereSegment);
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        subSelectStatement.setProjections(subqueryProjections);
        SubqueryExpressionSegment subqueryExpressionSegment = new SubqueryExpressionSegment(new SubquerySegment(0, 0, subSelectStatement));
        SubqueryProjectionSegment projectionSegment = mock(SubqueryProjectionSegment.class);
        WhereSegment whereSegment = new WhereSegment(0, 0, subqueryExpressionSegment);
        selectStatement.setWhere(whereSegment);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement);
        when(projectionSegment.getSubquery()).thenReturn(subquerySegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        assertTrue(new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME).isContainsSubquery());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForMySQL() {
        assertContainsDollarParameterMarker(new MySQLSelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForOracle() {
        assertContainsDollarParameterMarker(new OracleSelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForPostgreSQL() {
        assertContainsDollarParameterMarker(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForSQL92() {
        assertContainsDollarParameterMarker(new SQL92SelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForSQLServer() {
        assertContainsDollarParameterMarker(new SQLServerSelectStatement());
    }
    
    private void assertContainsDollarParameterMarker(final SelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ParameterMarkerExpressionSegment(0, 0, 0, ParameterMarkerType.DOLLAR));
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext selectStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertTrue(selectStatementContext.isContainsDollarParameterMarker());
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setCondition(new ParameterMarkerExpressionSegment(0, 0, 0, ParameterMarkerType.DOLLAR));
        selectStatement.setFrom(joinTableSegment);
        selectStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertTrue(selectStatementContext.isContainsDollarParameterMarker());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForMySQL() {
        assertContainsPartialDistinctAggregation(new MySQLSelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForOracle() {
        assertContainsPartialDistinctAggregation(new OracleSelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForPostgreSQL() {
        assertContainsPartialDistinctAggregation(new PostgreSQLSelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForSQL92() {
        assertContainsPartialDistinctAggregation(new SQL92SelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForSQLServer() {
        assertContainsPartialDistinctAggregation(new SQLServerSelectStatement());
    }
    
    private void assertContainsPartialDistinctAggregation(final SelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        projectionsSegment.getProjections().add(new AggregationDistinctProjectionSegment(0, 10, AggregationType.COUNT, "(1)", "distinctExpression"));
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext selectStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertTrue(selectStatementContext.isContainsPartialDistinctAggregation());
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private OrderByItemSegment createOrderByItemSegment(final String type) {
        switch (type) {
            case INDEX_ORDER_BY:
                return new IndexOrderByItemSegment(0, 0, 4, OrderDirection.ASC, NullsOrderType.FIRST);
            case COLUMN_ORDER_BY_WITH_OWNER:
                ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
                columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
                return new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST);
            case COLUMN_ORDER_BY_WITH_ALIAS:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("n")), OrderDirection.ASC, NullsOrderType.FIRST);
            default:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("id")), OrderDirection.ASC, NullsOrderType.FIRST);
        }
    }
    
    private ProjectionsSegment createProjectionsSegment() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().addAll(Arrays.asList(getColumnProjectionSegmentWithoutOwner(),
                getColumnProjectionSegmentWithoutOwner(true), getColumnProjectionSegmentWithoutOwner(false)));
        return projectionsSegment;
    }
    
    private ProjectionSegment getColumnProjectionSegmentWithoutOwner() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
        return new ColumnProjectionSegment(columnSegment);
    }
    
    private ProjectionSegment getColumnProjectionSegmentWithoutOwner(final boolean hasAlias) {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue(hasAlias ? "name" : "id"));
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        columnProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue(hasAlias ? "n" : null)));
        return columnProjectionSegment;
    }
    
    @Test
    void assertIsContainsEnhancedTable() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext actual = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertTrue(actual.isContainsEnhancedTable());
    }
}
