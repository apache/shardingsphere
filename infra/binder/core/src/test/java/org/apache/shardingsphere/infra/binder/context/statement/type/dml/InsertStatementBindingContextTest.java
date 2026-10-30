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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class InsertStatementBindingContextTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInsertSelectStatements")
    void assertInsertSelectRetainsWithAndValues(final String name, final WithSegment outerWith, final SelectStatement select, final Collection<String> expectedAliases) {
        InsertStatement statement = InsertStatement.builder().with(outerWith).insertSelect(new SubquerySegment(40, 100, select, "SELECT 1")).build();
        InsertStatementBaseContext baseContext = mock(InsertStatementBaseContext.class, RETURNS_DEEP_STUBS);
        when(baseContext.getSqlStatement()).thenReturn(statement);
        int expectedInnerCteCount = select.getWith().map(optional -> optional.getCommonTableExpressions().size()).orElse(0);
        int expectedValuesCount = select.getValues().size();
        List<SelectStatement> capturedStatements = new ArrayList<>(1);
        try (MockedConstruction<SelectStatementContext> contexts = mockConstruction(SelectStatementContext.class, (context, construction) -> {
            SelectStatement sqlStatement = (SelectStatement) construction.arguments().get(0);
            capturedStatements.add(sqlStatement);
            when(context.getSqlStatement()).thenReturn(sqlStatement);
        })) {
            InsertStatementBindingContext actual = new InsertStatementBindingContext(baseContext, Collections.emptyList(), mock(ShardingSphereMetaData.class), "foo_db");
            assertThat(actual.getInsertSelectContext().getSelectStatementContext(), sameInstance(contexts.constructed().get(0)));
            SelectStatement actualSelect = capturedStatements.get(0);
            Collection<String> actualAliases = actualSelect.getWith().map(optional -> optional.getCommonTableExpressions().stream()
                    .map(each -> each.getAliasName().get()).collect(Collectors.toList())).orElse(Collections.emptyList());
            assertThat(actualAliases, is(expectedAliases));
            assertThat(actualSelect.getValues(), is(select.getValues()));
            assertThat(actualSelect.getValues().size(), is(expectedValuesCount));
            assertThat(select.getWith().map(optional -> optional.getCommonTableExpressions().size()).orElse(0), is(expectedInnerCteCount));
            assertThat(outerWith.getCommonTableExpressions().size(), is(1));
        }
    }
    
    private static Stream<Arguments> getInsertSelectStatements() {
        SelectStatement cteSelect = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build();
        CommonTableExpressionSegment outerCte = new CommonTableExpressionSegment(0, 20, new AliasSegment(5, 11, new IdentifierValue("foo_cte")),
                new SubquerySegment(12, 20, cteSelect, "SELECT 1"), null);
        CommonTableExpressionSegment innerCte = new CommonTableExpressionSegment(40, 60, new AliasSegment(45, 51, new IdentifierValue("bar_cte")),
                new SubquerySegment(52, 60, cteSelect, "SELECT 2"), null);
        WithSegment outerWith = new WithSegment(0, 20, Collections.singletonList(outerCte));
        WithSegment innerWith = new WithSegment(40, 60, Collections.singletonList(innerCte));
        Collection<InsertValuesSegment> values = Arrays.asList(new InsertValuesSegment(70, 72, Collections.singletonList(new LiteralExpressionSegment(71, 71, 1))),
                new InsertValuesSegment(74, 76, Collections.singletonList(new LiteralExpressionSegment(75, 75, 2))));
        return Stream.of(Arguments.of("inner WITH", outerWith, SelectStatement.builder().with(innerWith).projections(new ProjectionsSegment(70, 80)).build(),
                Arrays.asList("foo_cte", "bar_cte")),
                Arguments.of("inner WITH and VALUES", outerWith, SelectStatement.builder().with(innerWith).values(values).projections(new ProjectionsSegment(70, 80)).build(),
                        Arrays.asList("foo_cte", "bar_cte")),
                Arguments.of("outer WITH and VALUES", outerWith,
                        SelectStatement.builder().values(values).projections(new ProjectionsSegment(70, 80)).build(), Collections.singletonList("foo_cte")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getParameterFrameArguments")
    void assertInsertValueParameterFrames(final String name, final Collection<ParameterMarkerSegment> markers, final List<ExpressionSegment> values, final List<Object> params,
                                          final Collection<List<Object>> expectedRows, final Collection<Integer> expectedOffsets) {
        InsertStatement statement = InsertStatement.builder().values(Collections.singletonList(new InsertValuesSegment(30, 60, values))).build();
        statement.addParameterMarkers(markers);
        InsertStatementBaseContext baseContext = mock(InsertStatementBaseContext.class, RETURNS_DEEP_STUBS);
        when(baseContext.getSqlStatement()).thenReturn(statement);
        when(baseContext.getValueExpressions()).thenReturn(Collections.singletonList(values));
        InsertStatementBindingContext actual = new InsertStatementBindingContext(baseContext, params, mock(ShardingSphereMetaData.class), "foo_db");
        Collection<List<Object>> actualRows = actual.getInsertValueContexts().stream().map(InsertValueContext::getParameters).collect(Collectors.toList());
        Collection<Integer> actualOffsets = actual.getInsertValueContexts().stream().map(InsertValueContext::getParametersOffset).collect(Collectors.toList());
        assertThat(actualRows, is(expectedRows));
        assertThat(actualOffsets, is(expectedOffsets));
    }
    
    private static Stream<Arguments> getParameterFrameArguments() {
        ParameterMarkerExpressionSegment first = new ParameterMarkerExpressionSegment(40, 40, 0);
        ParameterMarkerExpressionSegment second = new ParameterMarkerExpressionSegment(50, 50, 1);
        ParameterMarkerExpressionSegment prefixedFirst = new ParameterMarkerExpressionSegment(40, 40, 2);
        ParameterMarkerExpressionSegment prefixedSecond = new ParameterMarkerExpressionSegment(50, 50, 3);
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
                Arguments.of("literal values after WITH", prefixMarkers, literalValues, Arrays.asList("foo_cte", "bar_cte"),
                        Collections.singletonList(Collections.emptyList()), Collections.singletonList(2)),
                Arguments.of("literal values", Collections.emptyList(), literalValues, Collections.emptyList(),
                        Collections.singletonList(Collections.emptyList()), Collections.singletonList(0)));
    }
    
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SelectParameterFrames {
        
        @ParameterizedTest(name = "{0}")
        @MethodSource("getSelectParameters")
        void assertInsertSelectParameters(final String name, final Collection<ParameterMarkerSegment> markers, final List<Object> params,
                                          final Collection<Integer> expectedIndexes, final Collection<Object> expectedParams) {
            SelectStatement select = SelectStatement.builder().projections(new ProjectionsSegment(40, 60)).build();
            InsertStatement statement = InsertStatement.builder().with(new WithSegment(0, 20, Collections.emptyList()))
                    .insertSelect(new SubquerySegment(40, 60, select, "SELECT foo_value")).build();
            statement.addParameterMarkers(markers);
            InsertStatementBaseContext baseContext = mock(InsertStatementBaseContext.class, RETURNS_DEEP_STUBS);
            when(baseContext.getSqlStatement()).thenReturn(statement);
            try (
                    MockedConstruction<SelectStatementContext> contexts = mockConstruction(SelectStatementContext.class,
                            (context, construction) -> when(context.getSqlStatement()).thenReturn((SelectStatement) construction.arguments().get(0)))) {
                InsertStatementBindingContext actual = new InsertStatementBindingContext(baseContext, params, mock(ShardingSphereMetaData.class), "foo_db");
                assertThat(actual.getInsertSelectContext().getSelectStatementContext(), sameInstance(contexts.constructed().get(0)));
                assertThat(actual.getInsertSelectContext().getSelectStatementContext().getSqlStatement().getParameterMarkers().stream()
                        .map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedIndexes));
                assertThat(actual.getInsertSelectContext().getParameters(), is(expectedParams));
                assertThat(select.getParameterCount(), is(0));
            }
        }
        
        private Stream<Arguments> getSelectParameters() {
            return Stream.of(
                    Arguments.of("WITH and SELECT markers", Arrays.asList(new ParameterMarkerExpressionSegment(10, 10, 0),
                            new ParameterMarkerExpressionSegment(50, 50, 1), new ParameterMarkerExpressionSegment(100, 100, 2)),
                            Arrays.asList("foo_with", "foo_select", "foo_returning"), Arrays.asList(0, 1), Arrays.asList("foo_with", "foo_select")),
                    Arguments.of("WITH marker only", Arrays.asList(new ParameterMarkerExpressionSegment(10, 10, 0), new ParameterMarkerExpressionSegment(100, 100, 1)),
                            Arrays.asList("foo_with", "foo_returning"), Collections.singletonList(0), Collections.singletonList("foo_with")),
                    Arguments.of("SELECT marker only", Arrays.asList(new ParameterMarkerExpressionSegment(50, 50, 0), new ParameterMarkerExpressionSegment(100, 100, 1)),
                            Arrays.asList("foo_select", "foo_returning"), Collections.singletonList(0), Collections.singletonList("foo_select")));
        }
    }
    
    @Nested
    class ConflictParameterFrames {
        
        @ParameterizedTest(name = "{0}")
        @CsvSource({"no target marker, 0", "predicate marker, 1", "expression and predicate markers, 2"})
        void assertOnConflictParameters(final String name, final int targetMarkerCount) {
            ParameterMarkerExpressionSegment value = new ParameterMarkerExpressionSegment(40, 40, 0);
            Collection<ParameterMarkerSegment> markers = new ArrayList<>();
            markers.add(value);
            List<Object> params = new ArrayList<>();
            params.add("foo_value");
            for (int i = 0; i < targetMarkerCount; i++) {
                markers.add(new ParameterMarkerExpressionSegment(80 + i, 80 + i, i + 1));
                params.add("foo_target_" + i);
            }
            ParameterMarkerExpressionSegment assignmentValue = new ParameterMarkerExpressionSegment(100, 100, targetMarkerCount + 1);
            markers.add(assignmentValue);
            markers.add(new ParameterMarkerExpressionSegment(140, 140, targetMarkerCount + 2));
            params.add("foo_assignment");
            params.add("foo_returning");
            ColumnAssignmentSegment assignment = new ColumnAssignmentSegment(90, 110,
                    Collections.singletonList(new ColumnSegment(90, 99, new IdentifierValue("foo_column"))), assignmentValue);
            InsertStatement statement = InsertStatement.builder().values(Collections.singletonList(new InsertValuesSegment(30, 50, Collections.singletonList(value))))
                    .onDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(70, 120, Collections.singletonList(assignment))).build();
            statement.addParameterMarkers(markers);
            InsertStatementBaseContext baseContext = mock(InsertStatementBaseContext.class, RETURNS_DEEP_STUBS);
            when(baseContext.getSqlStatement()).thenReturn(statement);
            when(baseContext.getValueExpressions()).thenReturn(Collections.singletonList(Collections.singletonList(value)));
            InsertStatementBindingContext actual = new InsertStatementBindingContext(baseContext, params, mock(ShardingSphereMetaData.class), "foo_db");
            assertThat(actual.getInsertValueContexts().iterator().next().getParameters(), is(Collections.singletonList("foo_value")));
            assertThat(actual.getOnDuplicateKeyUpdateValueContext().getParameters(), is(Collections.singletonList("foo_assignment")));
            assertThat(actual.getOnDuplicateKeyUpdateValueContext().getParameterMarkerExpressions().iterator().next().getParameterIndex(), is(targetMarkerCount + 1));
        }
    }
}
