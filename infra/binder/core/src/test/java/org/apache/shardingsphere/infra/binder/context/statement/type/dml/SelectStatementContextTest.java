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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectStatementContextTest {
    
    private static final String INDEX_ORDER_BY = "IndexOrderBy";
    
    private static final String COLUMN_ORDER_BY_WITH_OWNER = "ColumnOrderByWithOwner";
    
    private static final String COLUMN_ORDER_BY_WITH_ALIAS = "ColumnOrderByWithAlias";
    
    private static final String COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS = "ColumnOrderByWithoutOwnerAlias";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertSetIndexForItemsByIndexOrderBy() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("table"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType)
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(INDEX_ORDER_BY))))
                .projections(createProjectionsSegment()).from(new SimpleTableSegment(tableNameSegment)).build();
        ShardingSphereDatabase database = mockDatabase();
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(4));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getEnhancedTableNames().contains("t_order")).thenReturn(true);
        when(result.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        return result;
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithOwner() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("table"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment tableSegment = new SimpleTableSegment(tableNameSegment);
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_db".toUpperCase())));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType)
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITH_OWNER))))
                .projections(createProjectionsSegment()).from(tableSegment).build();
        ShardingSphereDatabase database = mockDatabase();
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        selectStatementContext.setIndexes(Collections.emptyMap());
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(1));
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithAlias() {
        ShardingSphereDatabase database = mockDatabase();
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType)
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITH_ALIAS))))
                .projections(createProjectionsSegment()).build();
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        selectStatementContext.setIndexes(Collections.singletonMap("n", 2));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(2));
    }
    
    @Test
    void assertSetIndexForItemsByColumnOrderByWithoutAlias() {
        ShardingSphereDatabase database = mockDatabase();
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType)
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS))))
                .projections(createProjectionsSegment()).build();
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        selectStatementContext.setIndexes(Collections.singletonMap("id", 3));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
    }
    
    @Test
    void assertIsSameGroupByAndOrderByItems() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0))
                .groupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))))
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST)))).build();
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertTrue(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    private SelectStatementContext createSelectStatementContext(final SelectStatement selectStatement) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        return new SelectStatementContext(selectStatement, metaData, "foo_db", Collections.emptyList());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenEmptyGroupBy() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0)).build();
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    void assertIsNotSameGroupByAndOrderByItemsWhenDifferentGroupByAndOrderBy() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0))
                .groupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.LAST))))
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST)))).build();
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertFalse(selectStatementContext.isSameGroupByAndOrderByItems());
    }
    
    @Test
    void assertSetIndexWhenAggregationProjectionsPresent() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 0, AggregationType.MAX, "MAX(id)");
        aggregationProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("id", QuoteCharacter.QUOTE)));
        projectionsSegment.getProjections().add(aggregationProjectionSegment);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType)
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(createOrderByItemSegment(COLUMN_ORDER_BY_WITHOUT_OWNER_ALIAS))))
                .projections(projectionsSegment).build();
        ShardingSphereDatabase database = mockDatabase();
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        selectStatementContext.setIndexes(Collections.singletonMap("id", 3));
        assertThat(selectStatementContext.getOrderByContext().getItems().iterator().next().getIndex(), is(3));
        selectStatementContext.setNeedAggregateRewrite(true);
        assertTrue(selectStatementContext.isNeedAggregateRewrite());
        selectStatementContext.setNeedAggregateRewrite(false);
        assertFalse(selectStatementContext.isNeedAggregateRewrite());
    }
    
    @Test
    void assertSetWhere() {
        WhereSegment whereSegment = mock(WhereSegment.class, RETURNS_DEEP_STUBS);
        when(whereSegment.getExpr().getText()).thenReturn("");
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).where(whereSegment).projections(new ProjectionsSegment(0, 0)).build();
        ShardingSphereDatabase database = mockDatabase();
        SelectStatementContext actual = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.emptySet()));
        assertThat(actual.getTablesContext().getSimpleTables(), is(Collections.emptyList()));
        assertThat(actual.getGroupByContext().getItems(), is(Collections.emptyList()));
        assertThat(actual.getWhereSegments(), is(Collections.singletonList(whereSegment)));
    }
    
    @Test
    void assertContainsSubquery() {
        BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class, RETURNS_DEEP_STUBS);
        when(binaryOperationExpression.getText()).thenReturn("");
        WhereSegment whereSegment = new WhereSegment(0, 0, binaryOperationExpression);
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        SelectStatement subSelectStatement = SelectStatement.builder().databaseType(databaseType)
                .where(whereSegment).projections(subqueryProjections).subqueryType(SubqueryType.PROJECTION).build();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement, "");
        SubqueryProjectionSegment subqueryProjectionSegment = new SubqueryProjectionSegment(subquerySegment, "");
        projectionsSegment.getProjections().add(subqueryProjectionSegment);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).build();
        ShardingSphereDatabase database = mockDatabase();
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(database), "foo_db", Collections.emptyList());
        assertTrue(selectStatementContext.isContainsSubquery());
        assertThat(selectStatementContext.getSubqueryContexts().size(), is(1));
        assertThat(selectStatementContext.getSubqueryContexts().values().iterator().next().getSubqueryType(), is(SubqueryType.PROJECTION));
    }
    
    @Test
    void assertContainsSubqueryWhereEmpty() {
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue("id"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 0, 20);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, "=", "");
        WhereSegment subWhereSegment = new WhereSegment(0, 0, expression);
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        SelectStatement subSelectStatement = SelectStatement.builder().databaseType(databaseType).where(subWhereSegment).projections(subqueryProjections).build();
        SubqueryExpressionSegment subqueryExpressionSegment = new SubqueryExpressionSegment(new SubquerySegment(0, 0, subSelectStatement, ""));
        WhereSegment whereSegment = new WhereSegment(0, 0, subqueryExpressionSegment);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subSelectStatement, "");
        SubqueryProjectionSegment projectionSegment = new SubqueryProjectionSegment(subquerySegment, "");
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(projectionSegment);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).where(whereSegment).projections(projectionsSegment).build();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        assertTrue(new SelectStatementContext(selectStatement, metaData, "foo_db", Collections.emptyList()).isContainsSubquery());
    }
    
    @Test
    void assertFindColumnBoundInfoHandlesColumnsAndSubquery() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("id")));
        projectionsSegment.getProjections().add(columnProjectionSegment);
        ProjectionsSegment innerProjectionsSegment = new ProjectionsSegment(0, 0);
        ColumnProjectionSegment innerColumnProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("sub_id")));
        innerProjectionsSegment.getProjections().add(innerColumnProjection);
        SelectStatement innerSelectStatement = SelectStatement.builder().databaseType(databaseType).projections(innerProjectionsSegment).build();
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, innerSelectStatement, "");
        SubqueryProjectionSegment subqueryProjectionSegment = new SubqueryProjectionSegment(subquerySegment, "");
        projectionsSegment.getProjections().add(subqueryProjectionSegment);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).build();
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertTrue(selectStatementContext.findColumnBoundInfo(1).isPresent());
        assertThat(selectStatementContext.findColumnBoundInfo(1).get().getOriginalColumn().getValue(), is("id"));
        assertTrue(selectStatementContext.findColumnBoundInfo(2).isPresent());
        assertThat(selectStatementContext.findColumnBoundInfo(2).get().getOriginalColumn().getValue(), is("sub_id"));
        assertFalse(selectStatementContext.findColumnBoundInfo(3).isPresent());
    }
    
    @Test
    void assertContainsDollarParameterMarker() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ParameterMarkerExpressionSegment(0, 0, 0, ParameterMarkerType.DOLLAR));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).build();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, metaData, "foo_db", Collections.emptyList());
        assertTrue(selectStatementContext.isContainsDollarParameterMarker());
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setCondition(new ParameterMarkerExpressionSegment(0, 0, 0, ParameterMarkerType.DOLLAR));
        SelectStatement selectStatementWithFrom = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0)).from(joinTableSegment).build();
        selectStatementContext = new SelectStatementContext(selectStatementWithFrom, metaData, "foo_db", Collections.emptyList());
        assertTrue(selectStatementContext.isContainsDollarParameterMarker());
    }
    
    @Test
    void assertContainsPartialDistinctAggregation() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        projectionsSegment.getProjections().add(new AggregationDistinctProjectionSegment(0, 10, AggregationType.COUNT, "COUNT(1)", "distinctExpression"));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).build();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext selectStatementContext = new SelectStatementContext(selectStatement, metaData, "foo_db", Collections.emptyList());
        assertTrue(selectStatementContext.isContainsPartialDistinctAggregation());
    }
    
    @Test
    void assertJoinHavingCombineAndDelegatedGetters() {
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("left_id"));
        ColumnSegment right = new ColumnSegment(0, 0, new IdentifierValue("right_id"));
        BinaryOperationExpression joinCondition = new BinaryOperationExpression(0, 0, left, right, "=", "");
        CombineSegment combineSegment = new CombineSegment(
                0, 0, new SubquerySegment(0, 0, createSubSelectStatement(), ""), CombineType.UNION, new SubquerySegment(0, 0, createSubSelectStatement(), ""));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0)).from(joinTableSegment)
                .where(new WhereSegment(0, 0, joinCondition)).having(new HavingSegment(0, 0, new LiteralExpressionSegment(0, 0, 1))).combine(combineSegment).build();
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        assertTrue(selectStatementContext.isContainsJoinQuery());
        assertTrue(selectStatementContext.isContainsHaving());
        assertTrue(selectStatementContext.isContainsCombine());
        assertThat(selectStatementContext.getJoinConditions(), is(Collections.singletonList(joinCondition)));
        assertThat(selectStatementContext.getColumnSegments().size(), is(2));
        assertThat(selectStatementContext.getSqlStatement(), is(selectStatement));
        assertNotNull(selectStatementContext.getProjectionsContext());
    }
    
    @Test
    void assertBindParametersPopulatePaginationContext() {
        LimitSegment limitSegment = new LimitSegment(0, 0,
                new ParameterMarkerLimitValueSegment(0, 0, 0), new NumberLiteralLimitValueSegment(1, 1, 5L));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0)).limit(limitSegment).build();
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        selectStatementContext.bindParameters(Collections.singletonList(7L));
        PaginationContext paginationContext = selectStatementContext.getPaginationContext();
        assertTrue(paginationContext.getOffsetParameterIndex().isPresent());
        assertThat(paginationContext.getOffsetParameterIndex().get(), is(0));
        assertThat(paginationContext.getActualOffset(), is(7L));
        assertFalse(paginationContext.getRowCountParameterIndex().isPresent());
        assertThat(paginationContext.getActualRowCount(), is(Optional.of(5L)));
    }
    
    @Test
    void assertBindParametersPopulatePaginationContextWithByteArrayParameters() {
        LimitSegment limitSegment = new LimitSegment(0, 0,
                new ParameterMarkerLimitValueSegment(0, 0, 0), new NumberLiteralLimitValueSegment(1, 1, 5L));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0)).limit(limitSegment).build();
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        selectStatementContext.bindParameters(Collections.singletonList("7".getBytes(StandardCharsets.UTF_8)));
        PaginationContext paginationContext = selectStatementContext.getPaginationContext();
        assertTrue(paginationContext.getOffsetParameterIndex().isPresent());
        assertThat(paginationContext.getOffsetParameterIndex().get(), is(0));
        assertThat(paginationContext.getActualOffset(), is(7L));
        assertFalse(paginationContext.getRowCountParameterIndex().isPresent());
        assertThat(paginationContext.getActualRowCount(), is(Optional.of(5L)));
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private OrderByItemSegment createOrderByItemSegment(final String type) {
        switch (type) {
            case INDEX_ORDER_BY:
                return new IndexOrderByItemSegment(0, 0, 4, OrderDirection.ASC, NullsOrderType.FIRST);
            case COLUMN_ORDER_BY_WITH_OWNER:
                ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
                OwnerSegment owner = new OwnerSegment(0, 0, new IdentifierValue("table"));
                owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
                columnSegment.setOwner(owner);
                return new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST);
            case COLUMN_ORDER_BY_WITH_ALIAS:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("n")), OrderDirection.ASC, NullsOrderType.FIRST);
            default:
                return new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("id")), OrderDirection.ASC, NullsOrderType.FIRST);
        }
    }
    
    private ProjectionsSegment createProjectionsSegment() {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.setDistinctRow(true);
        result.getProjections().addAll(Arrays.asList(getColumnProjectionSegmentWithoutOwner(), getColumnProjectionSegmentWithoutOwner(true), getColumnProjectionSegmentWithoutOwner(false)));
        return result;
    }
    
    private ProjectionSegment getColumnProjectionSegmentWithoutOwner() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        OwnerSegment owner = new OwnerSegment(0, 0, new IdentifierValue("table"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        columnSegment.setOwner(owner);
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
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).from(new SimpleTableSegment(tableNameSegment)).build();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext actual = new SelectStatementContext(selectStatement, metaData, "foo_db", Collections.emptyList());
        assertTrue(actual.isContainsEnhancedTable());
        assertTrue(actual.containsDerivedProjections());
    }
    
    @Test
    void assertContainsEnhancedTable() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0))
                .from(new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, createSubSelectStatement(), ""))).build();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext actual = new SelectStatementContext(selectStatement, metaData, "foo_db", Collections.emptyList());
        assertTrue(actual.containsTableSubquery());
    }
    
    private SelectStatement createSubSelectStatement() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        return SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).from(new SimpleTableSegment(tableNameSegment)).build();
    }
}
