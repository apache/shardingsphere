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

package org.apache.shardingsphere.sql.parser.statement.core.extractor;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.DatetimeProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.join.OuterJoinExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ColumnExtractorTest {
    
    @Test
    void assertExtractColumnSegments() {
        assertThat(toColumnNames(ColumnExtractor.extractColumnSegments(createWhereSegments())), is(Arrays.asList("foo_name", "bar_pwd")));
    }
    
    @ParameterizedTest(name = "SelectStatement: {0}")
    @MethodSource("provideSelectStatements")
    void assertExtractFromSelectStatement(final String name, final SelectStatement selectStatement, final boolean containsSubQuery,
                                          final List<String> expectedColumnNames) {
        Collection<ColumnSegment> columnSegments = new LinkedList<>();
        ColumnExtractor.extractFromSelectStatement(columnSegments, selectStatement, containsSubQuery);
        assertThat(toColumnNames(columnSegments), is(expectedColumnNames));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExpressions")
    void assertExtract(final String name, final ExpressionSegment expression, final List<String> expectedColumnNames) {
        assertThat(toColumnNames(ColumnExtractor.extract(expression)), is(expectedColumnNames));
    }
    
    private static Stream<Arguments> provideSelectStatements() {
        return Stream.of(
                Arguments.of("FullSegments", createSelectStatementForExtraction(), true, Arrays.asList(
                        "foo_projection_column",
                        "foo_projection_agg_param",
                        "foo_datetime_left",
                        "foo_datetime_right",
                        "foo_expression_projection",
                        "foo_interval_left",
                        "foo_interval_right",
                        "foo_interval_minus",
                        "foo_subquery_projection_column",
                        "foo_collection_expr",
                        "foo_cte_subquery_column",
                        "foo_inner_join_left",
                        "bar_inner_join_right",
                        "foo_subquery_table_column",
                        "foo_join_left",
                        "bar_join_right",
                        "foo_using_column",
                        "bar_derived_using_column",
                        "foo_where_left",
                        "bar_where_right",
                        "foo_group_by_column",
                        "foo_group_by_expr_column",
                        "foo_having_left",
                        "bar_having_right",
                        "foo_order_by_column",
                        "bar_order_by_expr_column",
                        "foo_combine_left_column",
                        "bar_combine_right_column")),
                Arguments.of("SkipSubqueryFlagFalse", createSelectStatementSkippingSubquery(), false, Collections.emptyList()),
                Arguments.of("NoOptionalSegments", createSelectStatementWithoutOptionalSegments(), true, Collections.emptyList()));
    }
    
    private static Stream<Arguments> provideExpressions() {
        return Stream.of(
                Arguments.of("BinaryOperationColumns", createBinaryOperation("foo_left_col", "bar_right_col"), Arrays.asList("foo_left_col", "bar_right_col")),
                Arguments.of("BinaryOperationOuterJoin", createBinaryOperationWithOuterJoin(), Arrays.asList("foo_outer_left", "bar_outer_right")),
                Arguments.of("InExpressionWithColumnLeft", createInExpression(createColumnSegment("foo_in_left"),
                        createBinaryOperation("foo_in_right_left", "bar_in_right_right")), Arrays.asList("foo_in_left", "foo_in_right_left", "bar_in_right_right")),
                Arguments.of("InExpressionWithRowLeft", createInExpression(createRowExpression(),
                        createBinaryOperation("foo_row_right_left", "bar_row_right_right")), Arrays.asList("foo_row_item", "foo_row_right_left", "bar_row_right_right")),
                Arguments.of("InExpressionWithFunctionLeft",
                        createInExpression(createFunctionWithSingleColumnParameter("foo_function_left_column"), createBinaryOperation("foo_function_right_left", "bar_function_right_right")),
                        Arrays.asList("foo_function_left_column", "foo_function_right_left", "bar_function_right_right")),
                Arguments.of("BetweenExpressionWithColumns", createBetweenExpressionWithColumns(), Arrays.asList("foo_between_left", "foo_between_between", "foo_between_and")),
                Arguments.of("BetweenExpressionWithNonColumnOperands", createBetweenExpressionWithMixedOperands(),
                        Arrays.asList("foo_between_function_column", "foo_between_binary_left", "bar_between_binary_right")),
                Arguments.of("AggregationProjectionWithFunctionParameters", createAggregationProjectionExpression(),
                        Arrays.asList("foo_agg_direct", "foo_agg_func_direct", "foo_agg_func_binary_left", "bar_agg_func_binary_right")),
                Arguments.of("FunctionSegmentWithNestedBinary", createFunctionWithColumnAndBinaryParameter(),
                        Arrays.asList("foo_function_direct", "foo_function_binary_left", "bar_function_binary_right")));
    }
    
    private static Collection<WhereSegment> createWhereSegments() {
        BinaryOperationExpression leftExpression = new BinaryOperationExpression(10, 24,
                new ColumnSegment(10, 13, new IdentifierValue("foo_name")), new LiteralExpressionSegment(18, 22, "LiLei"), "=", "foo_name = 'LiLei'");
        BinaryOperationExpression rightExpression = new BinaryOperationExpression(30, 44,
                new ColumnSegment(30, 32, new IdentifierValue("bar_pwd")), new LiteralExpressionSegment(40, 45, "123456"), "=", "bar_pwd = '123456'");
        return Collections.singleton(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, leftExpression, rightExpression, "AND", "foo_name = 'LiLei' AND bar_pwd = '123456'")));
    }
    
    private static SelectStatement createSelectStatementForExtraction() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(createColumnSegment("foo_projection_column")));
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(foo_projection_agg_param)");
        aggregationProjectionSegment.getParameters().add(createColumnSegment("foo_projection_agg_param"));
        projections.getProjections().add(aggregationProjectionSegment);
        projections.getProjections().add(new DatetimeProjectionSegment(0, 0, createColumnSegment("foo_datetime_left"), createColumnSegment("foo_datetime_right"), "date_add"));
        projections.getProjections().add(new ExpressionProjectionSegment(0, 0, "foo_expression_projection", createColumnSegment("foo_expression_projection")));
        projections.getProjections().add(new IntervalExpressionProjection(0, 0, createColumnSegment("foo_interval_left"),
                createColumnSegment("foo_interval_minus"), createColumnSegment("foo_interval_right"), "interval expr"));
        projections.getProjections().add(new SubqueryProjectionSegment(new SubquerySegment(0, 0, createSelectStatementWithProjection("foo_subquery_projection_column"), ""), "subquery"));
        SelectStatement result = new SelectStatement(mock(DatabaseType.class));
        result.setProjections(projections);
        result.setFrom(createJoinTableForExtraction());
        result.setWhere(new WhereSegment(0, 0, createBinaryOperation("foo_where_left", "bar_where_right")));
        result.setGroupBy(createGroupBySegment());
        result.setHaving(new HavingSegment(0, 0, createBinaryOperation("foo_having_left", "bar_having_right")));
        result.setOrderBy(createOrderBySegment());
        result.setCombine(createCombineSegment());
        return result;
    }
    
    private static SelectStatement createSelectStatementWithoutOptionalSegments() {
        SelectStatement result = new SelectStatement(mock(DatabaseType.class));
        result.setProjections(new ProjectionsSegment(0, 0));
        return result;
    }
    
    private static SelectStatement createSelectStatementSkippingSubquery() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new SubqueryProjectionSegment(new SubquerySegment(0, 0, createSelectStatementWithProjection("foo_skip_projection"), ""), "sub"));
        SelectStatement result = new SelectStatement(mock(DatabaseType.class));
        result.setProjections(projectionsSegment);
        result.setFrom(new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, createSelectStatementWithProjection("bar_skip_from"), "")));
        return result;
    }
    
    private static GroupBySegment createGroupBySegment() {
        Collection<OrderByItemSegment> groupByItems = new LinkedList<>();
        groupByItems.add(new ColumnOrderByItemSegment(createColumnSegment("foo_group_by_column"), OrderDirection.ASC, NullsOrderType.FIRST));
        groupByItems.add(new ExpressionOrderByItemSegment(0, 0, "group_by_expr", OrderDirection.ASC, NullsOrderType.FIRST, createColumnSegment("foo_group_by_expr_column")));
        return new GroupBySegment(0, 0, groupByItems);
    }
    
    private static OrderBySegment createOrderBySegment() {
        Collection<OrderByItemSegment> orderByItems = new LinkedList<>();
        orderByItems.add(new ColumnOrderByItemSegment(createColumnSegment("foo_order_by_column"), OrderDirection.DESC, NullsOrderType.LAST));
        orderByItems.add(new ExpressionOrderByItemSegment(0, 0, "order_by_expr", OrderDirection.DESC, NullsOrderType.LAST, createColumnSegment("bar_order_by_expr_column")));
        return new OrderBySegment(0, 0, orderByItems);
    }
    
    private static CombineSegment createCombineSegment() {
        SubquerySegment left = new SubquerySegment(0, 0, createSelectStatementWithProjection("foo_combine_left_column"), "");
        SubquerySegment right = new SubquerySegment(0, 0, createSelectStatementWithProjection("bar_combine_right_column"), "");
        return new CombineSegment(0, 0, left, CombineType.UNION, right);
    }
    
    private static JoinTableSegment createJoinTableForExtraction() {
        JoinTableSegment innerJoin = new JoinTableSegment();
        innerJoin.setLeft(new CollectionTableSegment(createColumnSegment("foo_collection_expr")));
        CommonTableExpressionSegment commonTableExpressionSegment = new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("cte_alias")),
                new SubquerySegment(0, 0, createSelectStatementWithProjection("foo_cte_subquery_column"), ""));
        innerJoin.setRight(commonTableExpressionSegment);
        innerJoin.setCondition(createBinaryOperation("foo_inner_join_left", "bar_inner_join_right"));
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(innerJoin);
        result.setRight(new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, createSelectStatementWithProjection("foo_subquery_table_column"), "")));
        result.setCondition(createBinaryOperation("foo_join_left", "bar_join_right"));
        result.setUsing(Collections.singletonList(createColumnSegment("foo_using_column")));
        result.setDerivedUsing(Collections.singletonList(createColumnSegment("bar_derived_using_column")));
        return result;
    }
    
    private static SelectStatement createSelectStatementWithProjection(final String columnName) {
        SelectStatement result = new SelectStatement(mock(DatabaseType.class));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(createColumnSegment(columnName)));
        result.setProjections(projections);
        return result;
    }
    
    private static BinaryOperationExpression createBinaryOperation(final String leftColumnName, final String rightColumnName) {
        return new BinaryOperationExpression(0, 0, createColumnSegment(leftColumnName), createColumnSegment(rightColumnName), "=", leftColumnName + " = " + rightColumnName);
    }
    
    private static BinaryOperationExpression createBinaryOperationWithOuterJoin() {
        OuterJoinExpression left = new OuterJoinExpression(0, 0, createColumnSegment("foo_outer_left"), "=", "foo_outer_left");
        OuterJoinExpression right = new OuterJoinExpression(0, 0, createColumnSegment("bar_outer_right"), "=", "bar_outer_right");
        return new BinaryOperationExpression(0, 0, left, right, "=", "outer join");
    }
    
    private static InExpression createInExpression(final ExpressionSegment left, final ExpressionSegment right) {
        return new InExpression(0, 0, left, right, false);
    }
    
    private static RowExpression createRowExpression() {
        RowExpression result = new RowExpression(0, 0, "(row)");
        result.getItems().add(createColumnSegment("foo_row_item"));
        result.getItems().add(new LiteralExpressionSegment(0, 0, 1));
        return result;
    }
    
    private static BetweenExpression createBetweenExpressionWithColumns() {
        return new BetweenExpression(0, 0, createColumnSegment("foo_between_left"), createColumnSegment("foo_between_between"), createColumnSegment("foo_between_and"), false);
    }
    
    private static BetweenExpression createBetweenExpressionWithMixedOperands() {
        return new BetweenExpression(0, 0,
                new LiteralExpressionSegment(0, 0, 1), createFunctionWithSingleColumnParameter("foo_between_function_column"),
                createBinaryOperation("foo_between_binary_left", "bar_between_binary_right"), false);
    }
    
    private static AggregationProjectionSegment createAggregationProjectionExpression() {
        AggregationProjectionSegment result = new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(expr)");
        result.getParameters().add(createColumnSegment("foo_agg_direct"));
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "MAX", "max");
        functionSegment.getParameters().add(createColumnSegment("foo_agg_func_direct"));
        functionSegment.getParameters().add(createBinaryOperation("foo_agg_func_binary_left", "bar_agg_func_binary_right"));
        result.getParameters().add(functionSegment);
        return result;
    }
    
    private static FunctionSegment createFunctionWithSingleColumnParameter(final String columnName) {
        FunctionSegment result = new FunctionSegment(0, 0, "FUNC", "func");
        result.getParameters().add(createColumnSegment(columnName));
        return result;
    }
    
    private static FunctionSegment createFunctionWithColumnAndBinaryParameter() {
        FunctionSegment result = new FunctionSegment(0, 0, "FUNC", "func");
        result.getParameters().add(createColumnSegment("foo_function_direct"));
        result.getParameters().add(createBinaryOperation("foo_function_binary_left", "bar_function_binary_right"));
        return result;
    }
    
    private static ColumnSegment createColumnSegment(final String columnName) {
        return new ColumnSegment(0, 0, new IdentifierValue(columnName));
    }
    
    private static List<String> toColumnNames(final Collection<ColumnSegment> columnSegments) {
        return columnSegments.stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
}
