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

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class InsertStatementBindingContextTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getParameterFrameArguments")
    void assertInsertValueParameterFrames(final String name, final List<ParameterMarkerSegment> markers, final List<ExpressionSegment> values, final List<Object> params,
                                          final List<List<Object>> expectedRows, final List<Integer> expectedOffsets) {
        InsertStatement statement = InsertStatement.builder().values(Collections.singletonList(new InsertValuesSegment(30, 60, values))).build();
        statement.addParameterMarkers(markers);
        InsertStatementBaseContext baseContext = mock(InsertStatementBaseContext.class, RETURNS_DEEP_STUBS);
        when(baseContext.getSqlStatement()).thenReturn(statement);
        when(baseContext.getValueExpressions()).thenReturn(Collections.singletonList(values));
        InsertStatementBindingContext actual = new InsertStatementBindingContext(baseContext, params, mock(ShardingSphereMetaData.class), "foo_db");
        List<List<Object>> actualRows = actual.getInsertValueContexts().stream().map(InsertValueContext::getParameters).collect(Collectors.toList());
        List<Integer> actualOffsets = actual.getInsertValueContexts().stream().map(InsertValueContext::getParametersOffset).collect(Collectors.toList());
        assertThat(actualRows, is(expectedRows));
        assertThat(actualOffsets, is(expectedOffsets));
    }
    
    private static Stream<Arguments> getParameterFrameArguments() {
        ParameterMarkerExpressionSegment first = new ParameterMarkerExpressionSegment(40, 40, 0);
        ParameterMarkerExpressionSegment second = new ParameterMarkerExpressionSegment(50, 50, 1);
        ParameterMarkerExpressionSegment prefixedFirst = new ParameterMarkerExpressionSegment(40, 40, 2);
        ParameterMarkerExpressionSegment prefixedSecond = new ParameterMarkerExpressionSegment(50, 50, 3);
        ParameterMarkerExpressionSegment laterWhen = new ParameterMarkerExpressionSegment(10, 10, 2);
        ParameterMarkerExpressionSegment laterElseValue = new ParameterMarkerExpressionSegment(40, 40, 4);
        List<Object> row = Arrays.asList("foo_value", "bar_value");
        List<Object> paramsWithPrefix = Arrays.asList("foo_cte", "bar_cte", "foo_value", "bar_value");
        List<ParameterMarkerSegment> prefixMarkers = Arrays.asList(new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(10, 10, 1));
        List<ExpressionSegment> literalValues = Arrays.asList(new LiteralExpressionSegment(40, 40, "foo_value"), new LiteralExpressionSegment(50, 50, "bar_value"));
        return Stream.of(
                Arguments.of("ordinary values", Arrays.asList(first, second), Arrays.asList(first, second), row, Collections.singletonList(row), Collections.singletonList(0)),
                Arguments.of("WITH prefix", Arrays.asList(prefixMarkers.get(0), prefixMarkers.get(1), prefixedFirst, prefixedSecond), Arrays.asList(prefixedFirst, prefixedSecond),
                        paramsWithPrefix, Collections.singletonList(row), Collections.singletonList(2)),
                Arguments.of("child global indexes", Arrays.asList(prefixedFirst, prefixedSecond), Arrays.asList(prefixedFirst, prefixedSecond),
                        paramsWithPrefix, Collections.singletonList(row), Collections.singletonList(2)),
                Arguments.of("conditional ELSE marker gap", Arrays.asList(prefixMarkers.get(0), prefixedFirst), Collections.singletonList(prefixedFirst),
                        Arrays.asList("foo_when", "foo_first", "foo_else"), Collections.singletonList(Collections.singletonList("foo_else")), Collections.singletonList(2)),
                Arguments.of("later WHEN marker gap", Arrays.asList(prefixMarkers.get(0), prefixedSecond), Collections.singletonList(prefixedSecond),
                        Arrays.asList("foo_when", "foo_first", "bar_when", "foo_second"), Collections.singletonList(Collections.singletonList("foo_second")), Collections.singletonList(3)),
                Arguments.of("later ELSE marker gap", Arrays.asList(laterWhen, laterElseValue), Collections.singletonList(laterElseValue),
                        Arrays.asList("foo_when", "foo_first", "bar_when", "foo_second", "foo_else"), Collections.singletonList(Collections.singletonList("foo_else")),
                        Collections.singletonList(4)),
                Arguments.of("literal values after WITH", prefixMarkers, literalValues, Arrays.asList("foo_cte", "bar_cte"),
                        Collections.singletonList(Collections.emptyList()), Collections.singletonList(2)),
                Arguments.of("literal values", Collections.emptyList(), literalValues, Collections.emptyList(),
                        Collections.singletonList(Collections.emptyList()), Collections.singletonList(0)));
    }
    
    @Test
    void assertInsertSelectWithClausePreservesAllValuesRows() {
        LiteralExpressionSegment first = new LiteralExpressionSegment(30, 30, 1);
        SelectStatement nested = SelectStatement.builder().projections(new ProjectionsSegment(44, 44)).build();
        SubqueryExpressionSegment second = new SubqueryExpressionSegment(new SubquerySegment(36, 45, nested, "SELECT 2"));
        List<InsertValuesSegment> rows = Arrays.asList(new InsertValuesSegment(29, 31, Collections.singletonList(first)), new InsertValuesSegment(35, 46, Collections.singletonList(second)));
        SelectStatement source = SelectStatement.builder().projections(new ProjectionsSegment(29, 46)).values(rows).build();
        WithSegment with = new WithSegment(0, 18, Collections.emptyList());
        InsertStatement statement = InsertStatement.builder().table(new SimpleTableSegment(new TableNameSegment(20, 22, new IdentifierValue("foo"))))
                .with(with).insertSelect(new SubquerySegment(29, 46, source, "VALUES (1), ((SELECT 2))")).build();
        InsertStatementBaseContext base = mock(InsertStatementBaseContext.class, RETURNS_DEEP_STUBS);
        when(base.getSqlStatement()).thenReturn(statement);
        try (
                MockedConstruction<SelectStatementContext> ignored = mockConstruction(SelectStatementContext.class,
                        (context, construction) -> when(context.getSqlStatement()).thenReturn((SelectStatement) construction.arguments().get(0)))) {
            InsertStatementBindingContext actual = new InsertStatementBindingContext(base, Collections.emptyList(), mock(ShardingSphereMetaData.class), "foo_db");
            SelectStatement actualSelect = actual.getInsertSelectContext().getSelectStatementContext().getSqlStatement();
            assertThat(actualSelect, not(sameInstance(source)));
            assertThat(actualSelect.getWith().get(), sameInstance(with));
            assertThat(actualSelect.getValues().size(), is(2));
            assertThat(actualSelect.getValues(), contains(rows.get(0), rows.get(1)));
            assertThat(source.getValues().size(), is(2));
            assertFalse(source.getWith().isPresent());
        }
    }
    
    @Test
    void assertInsertSelectWithOuterAndInnerWithClauses() {
        ParameterMarkerExpressionSegment outerParameter = new ParameterMarkerExpressionSegment(15, 15, 0);
        ParameterMarkerExpressionSegment innerParameter = new ParameterMarkerExpressionSegment(45, 45, 1);
        WithSegment outer = new WithSegment(0, 26, Collections.emptyList());
        WithSegment inner = new WithSegment(38, 60, Collections.emptyList());
        SelectStatement source = SelectStatement.builder().projections(new ProjectionsSegment(70, 75)).with(inner).build();
        source.addParameterMarkers(Collections.singletonList(innerParameter));
        InsertStatement statement = InsertStatement.builder().table(new SimpleTableSegment(new TableNameSegment(27, 29, new IdentifierValue("foo"))))
                .with(outer).insertSelect(new SubquerySegment(38, 75, source, "WITH foo_inner AS (SELECT ?) SELECT 1")).build();
        statement.addParameterMarkers(Arrays.asList(outerParameter, innerParameter));
        InsertStatementBaseContext base = mock(InsertStatementBaseContext.class, RETURNS_DEEP_STUBS);
        when(base.getSqlStatement()).thenReturn(statement);
        try (
                MockedConstruction<SelectStatementContext> ignored = mockConstruction(SelectStatementContext.class,
                        (context, construction) -> when(context.getSqlStatement()).thenReturn((SelectStatement) construction.arguments().get(0)))) {
            InsertStatementBindingContext actual = new InsertStatementBindingContext(base, Arrays.asList("foo_outer", "bar_inner"), mock(ShardingSphereMetaData.class), "foo_db");
            SelectStatement actualSelect = actual.getInsertSelectContext().getSelectStatementContext().getSqlStatement();
            assertThat(actualSelect.getWith().orElse(null), sameInstance(inner));
            assertThat(actualSelect.getParameterMarkers(), contains(outerParameter, innerParameter));
            assertThat(actual.getInsertSelectContext().getParameters(), is(Arrays.asList("foo_outer", "bar_inner")));
        }
    }
}
