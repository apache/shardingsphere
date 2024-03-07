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

package org.apache.shardingsphere.infra.binder.context.statement.dml;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.RuleIdentifiers;
import org.apache.shardingsphere.infra.rule.identifier.type.table.TableMapperRule;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;
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
        assertSetIndexForItemsByIndexOrderBy(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForOracle() {
        assertSetIndexForItemsByIndexOrderBy(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForPostgreSQL() {
        assertSetIndexForItemsByIndexOrderBy(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForSQL92() {
        assertSetIndexForItemsByIndexOrderBy(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByIndexOrderByForSQLServer() {
        assertSetIndexForItemsByIndexOrderBy(new SQLServerGenericSelectStatement());
    }
    
    private void assertSetIndexForItemsByIndexOrderBy(final GenericSelectStatement selectStatement) {
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
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        TableMapperRule tableMapperRule = mock(TableMapperRule.class, RETURNS_DEEP_STUBS);
        when(tableMapperRule.getEnhancedTableMapper().contains("t_order")).thenReturn(true);
        when(rule.getRuleIdentifiers()).thenReturn(new RuleIdentifiers(tableMapperRule));
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        return result;
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForOracle() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwnerForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithOwner(new SQLServerGenericSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithOwner(final GenericSelectStatement selectStatement) {
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
        assertSetIndexForItemsByColumnOrderByWithAlias(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForOracle() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAliasForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithAlias(new SQLServerGenericSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithAlias(final GenericSelectStatement selectStatement) {
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITH_ALIAS))));
        selectStatement.setProjections(createProjectionsSegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.singletonMap("n", 2));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(2));
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForMySQL() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForOracle() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForPostgreSQL() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForSQL92() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAliasForSQLServer() {
        assertSetIndexForItemsByColumnOrderByWithoutAlias(new SQLServerGenericSelectStatement());
    }
    
    private void assertSetIndexForItemsByColumnOrderByWithoutAlias(final GenericSelectStatement selectStatement) {
        ShardingSphereDatabase database = mockDatabase();
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS))));
        selectStatement.setProjections(createProjectionsSegment());
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.singletonMap("id", 3));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForMySQL() {
        assertIsSameGroupByAndOrderByItems(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForOracle() {
        assertIsSameGroupByAndOrderByItems(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForPostgreSQL() {
        assertIsSameGroupByAndOrderByItems(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForSQL92() {
        assertIsSameGroupByAndOrderByItems(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItemsForSQLServer() {
        assertIsSameGroupByAndOrderByItems(new SQLServerGenericSelectStatement());
    }
    
    private void assertIsSameGroupByAndOrderByItems(final GenericSelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertTrue(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    private SelectStatementContext createSelectStatementContext(final GenericSelectStatement selectStatement) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        return new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForMySQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForOracle() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForPostgreSQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForSQL92() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupByForSQLServer() {
        assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(new SQLServerGenericSelectStatement());
    }
    
    private void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy(final GenericSelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForMySQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForOracle() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForPostgreSQL() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForSQL92() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderByForSQLServer() {
        assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(new SQLServerGenericSelectStatement());
    }
    
    private void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy(final GenericSelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.LAST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForMySQL() {
        assertSetIndexWhenAggregationProjectionsPresent(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForOracle() {
        assertSetIndexWhenAggregationProjectionsPresent(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForPostgreSQL() {
        assertSetIndexWhenAggregationProjectionsPresent(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForSQL92() {
        assertSetIndexWhenAggregationProjectionsPresent(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresentForSQLServer() {
        assertSetIndexWhenAggregationProjectionsPresent(new SQLServerGenericSelectStatement());
    }
    
    private void assertSetIndexWhenAggregationProjectionsPresent(final GenericSelectStatement selectStatement) {
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 0, AggregationType.MAX, "MAX(id)");
        aggregationProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("id", QuoteCharacter.QUOTE)));
        projectionsSegment.getProjections().add(aggregationProjectionSegment);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mockDatabase();
        SelectStatementContext selectStatementContext = new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        selectStatementContext.setIndexes(Collections.singletonMap("id", 3));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    void assertSetWhereForMySQL() {
        assertSetWhere(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertSetWhereForOracle() {
        assertSetWhere(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertSetWhereForPostgreSQL() {
        assertSetWhere(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertSetWhereForSQL92() {
        assertSetWhere(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertSetWhereForSQLServer() {
        assertSetWhere(new SQLServerGenericSelectStatement());
    }
    
    private void assertSetWhere(final GenericSelectStatement selectStatement) {
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
        assertContainsSubquery(new MySQLGenericSelectStatement(), new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForOracle() {
        assertContainsSubquery(new OracleGenericSelectStatement(), new OracleGenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForPostgreSQL() {
        assertContainsSubquery(new PostgreSQLGenericSelectStatement(), new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForSQL92() {
        assertContainsSubquery(new SQL92GenericSelectStatement(), new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryForSQLServer() {
        assertContainsSubquery(new SQLServerGenericSelectStatement(), new SQLServerGenericSelectStatement());
    }
    
    private void assertContainsSubquery(final GenericSelectStatement selectStatement, final GenericSelectStatement subSelectStatement) {
        WhereSegment whereSegment = new WhereSegment(0, 0, null);
        subSelectStatement.setWhere(whereSegment);
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        subSelectStatement.setProjections(subqueryProjections);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement, "");
        SubqueryProjectionSegment subqueryProjectionSegment = new SubqueryProjectionSegment(subquerySegment, "");
        projectionsSegment.getProjections().add(subqueryProjectionSegment);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mockDatabase();
        assertTrue(new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME).isContainsSubquery());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForMySQL() {
        assertContainsSubqueryWhereEmpty(new MySQLGenericSelectStatement(), new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForOracle() {
        assertContainsSubqueryWhereEmpty(new OracleGenericSelectStatement(), new OracleGenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForPostgreSQL() {
        assertContainsSubqueryWhereEmpty(new PostgreSQLGenericSelectStatement(), new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForSQL92() {
        assertContainsSubqueryWhereEmpty(new SQL92GenericSelectStatement(), new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertContainsSubqueryWhereEmptyForSQLServer() {
        assertContainsSubqueryWhereEmpty(new SQLServerGenericSelectStatement(), new SQLServerGenericSelectStatement());
    }
    
    private void assertContainsSubqueryWhereEmpty(final GenericSelectStatement selectStatement, final GenericSelectStatement subSelectStatement) {
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue("id"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, 20);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, "=", null);
        WhereSegment subWhereSegment = new WhereSegment(0, 0, expression);
        subSelectStatement.setWhere(subWhereSegment);
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        subSelectStatement.setProjections(subqueryProjections);
        SubqueryExpressionSegment subqueryExpressionSegment = new SubqueryExpressionSegment(new SubquerySegment(0, 0, subSelectStatement, ""));
        SubqueryProjectionSegment projectionSegment = mock(SubqueryProjectionSegment.class);
        WhereSegment whereSegment = new WhereSegment(0, 0, subqueryExpressionSegment);
        selectStatement.setWhere(whereSegment);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement, "");
        when(projectionSegment.getSubquery()).thenReturn(subquerySegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        assertTrue(new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME).isContainsSubquery());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForMySQL() {
        assertContainsDollarParameterMarker(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForOracle() {
        assertContainsDollarParameterMarker(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForPostgreSQL() {
        assertContainsDollarParameterMarker(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForSQL92() {
        assertContainsDollarParameterMarker(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertContainsDollarParameterMarkerForSQLServer() {
        assertContainsDollarParameterMarker(new SQLServerGenericSelectStatement());
    }
    
    private void assertContainsDollarParameterMarker(final GenericSelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ParameterMarkerExpressionSegment(0, 0, 0, ParameterMarkerType.DOLLAR));
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
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
        assertContainsPartialDistinctAggregation(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForOracle() {
        assertContainsPartialDistinctAggregation(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForPostgreSQL() {
        assertContainsPartialDistinctAggregation(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForSQL92() {
        assertContainsPartialDistinctAggregation(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertContainsPartialDistinctAggregationForSQLServer() {
        assertContainsPartialDistinctAggregation(new SQLServerGenericSelectStatement());
    }
    
    private void assertContainsPartialDistinctAggregation(final GenericSelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        projectionsSegment.getProjections().add(new AggregationDistinctProjectionSegment(0, 10, AggregationType.COUNT, "COUNT(1)", "distinctExpression"));
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext selectStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertTrue(selectStatementContext.isContainsPartialDistinctAggregation());
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
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
        GenericSelectStatement selectStatement = new MySQLGenericSelectStatement();
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext actual = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertTrue(actual.isContainsEnhancedTable());
    }
    
    @Test
    void assertContainsEnhancedTable() {
        GenericSelectStatement selectStatement = new MySQLGenericSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(new SubqueryTableSegment(new SubquerySegment(0, 0, createSubSelectStatement(), "")));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase()), mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext actual = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertTrue(actual.containsTableSubquery());
    }
    
    private GenericSelectStatement createSubSelectStatement() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        GenericSelectStatement result = new MySQLGenericSelectStatement();
        result.setProjections(projectionsSegment);
        result.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        return result;
    }
}
