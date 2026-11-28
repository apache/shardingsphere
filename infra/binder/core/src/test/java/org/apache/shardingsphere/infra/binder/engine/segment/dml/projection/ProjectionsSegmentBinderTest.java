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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.projection;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.ColumnProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.ShorthandProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ProjectionsSegmentBinderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("shorthandScopeCases")
    void assertBindShorthandScope(final String name, final String localAlias, final boolean qualified, final String expectedLabel) {
        ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(7, 18);
        if (qualified) {
            shorthand.setOwner(new OwnerSegment(7, 16, new IdentifierValue("foo_source")));
        }
        ProjectionsSegment projections = new ProjectionsSegment(7, 18);
        projections.getProjections().add(shorthand);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> local = LinkedHashMultimap.create();
        if (!localAlias.isEmpty()) {
            local.put(CaseInsensitiveString.of(localAlias), createSourceContext("foo_local"));
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        outer.put(CaseInsensitiveString.of("foo_source"), createSourceContext("foo_outer"));
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        when(context.getSqlStatement()).thenReturn(SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")).projections(projections).build());
        SimpleTableSegment boundTable = localAlias.isEmpty() ? null : mock(SimpleTableSegment.class);
        if (null != boundTable) {
            TableNameSegment tableName = mock(TableNameSegment.class);
            when(tableName.getIdentifier()).thenReturn(new IdentifierValue(localAlias));
            when(boundTable.getTableName()).thenReturn(tableName);
        }
        ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, context, boundTable, local, outer);
        ShorthandProjectionSegment actualShorthand = (ShorthandProjectionSegment) actual.getProjections().iterator().next();
        assertThat(actualShorthand.getActualProjectionSegments().size(), is(1));
        assertThat(actualShorthand.getActualProjectionSegments().iterator().next().getColumnLabel(), is(expectedLabel));
    }
    
    private static Stream<Arguments> shorthandScopeCases() {
        return Stream.of(
                Arguments.of("correlated star without local FROM", "", true, "foo_outer"),
                Arguments.of("unrelated local alias", "bar_source", true, "foo_outer"),
                Arguments.of("local alias shadows outer", "foo_source", true, "foo_local"),
                Arguments.of("unqualified star stays local", "foo_source", false, "foo_local"));
    }
    
    private TableSegmentBinderContext createSourceContext(final String column) {
        TableSegmentBinderContext result = mock(TableSegmentBinderContext.class);
        when(result.getProjectionSegments()).thenReturn(Collections.singleton(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue(column)))));
        return result;
    }
    
    @Test
    void assertBindValuesRowsPreservesProjectionShape() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 15);
        LiteralExpressionSegment firstValue = new LiteralExpressionSegment(8, 8, 1);
        projections.getProjections().add(new ExpressionProjectionSegment(8, 8, "1", firstValue));
        RowExpression first = new RowExpression(7, 9, "(1)");
        first.getItems().add(firstValue);
        projections.getValuesRows().add(first);
        RowExpression second = new RowExpression(12, 14, "(2)");
        LiteralExpressionSegment secondValue = new LiteralExpressionSegment(13, 13, 2);
        second.getItems().add(secondValue);
        projections.getValuesRows().add(second);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        when(context.getSqlStatement()).thenReturn(SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")).projections(projections).build());
        ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, context, null, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertThat(actual.getProjections().size(), is(1));
        assertThat(actual.getValuesRows().size(), is(2));
        assertThat(actual.getValuesRows().get(1), not(sameInstance(second)));
        assertThat(actual.getValuesRows().get(1).getText(), is("(2)"));
        assertThat(actual.getValuesRows().get(1).getStartIndex(), is(12));
        assertThat(actual.getValuesRows().get(1).getStopIndex(), is(14));
        assertThat(actual.getValuesRows().get(1).getItems().iterator().next(), sameInstance(secondValue));
    }
    
    @ParameterizedTest(name = "local={0}, expected local={1}")
    @CsvSource({"foo,true", "bar,false", "'',false"})
    void assertBindParenthesizedRowAlias(final String localAlias, final boolean expectedLocal) {
        ColumnSegment column = new ColumnSegment(1, 3, new IdentifierValue("foo"));
        CommonExpressionSegment expression = new CommonExpressionSegment(0, 6, "(foo).*", Collections.singletonList(column));
        expression.setRowExpansion(true);
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(0, 6, "(foo).*", expression);
        AliasSegment alias = new AliasSegment(8, 16, new IdentifierValue("foo_alias"));
        projection.setAlias(alias);
        ProjectionsSegment projections = new ProjectionsSegment(0, 16);
        projections.getProjections().add(projection);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        if (!localAlias.isEmpty()) {
            tables.put(CaseInsensitiveString.of(localAlias), mock(TableSegmentBinderContext.class));
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        outer.put(CaseInsensitiveString.of("foo"), mock(TableSegmentBinderContext.class));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> expectedContexts = expectedLocal ? tables : outer;
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class, RETURNS_DEEP_STUBS);
        when(context.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        DialectDatabaseMetaData metadata = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(metadata.getSQLOption().isSupportWholeRowProjection()).thenReturn(true);
        try (
                MockedStatic<ExpressionSegmentBinder> expressions = mockStatic(ExpressionSegmentBinder.class);
                MockedStatic<ShorthandProjectionSegmentBinder> shorthands = mockStatic(ShorthandProjectionSegmentBinder.class);
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class,
                        (registry, construction) -> when(registry.getDialectDatabaseMetaData()).thenReturn(metadata))) {
            expressions.when(() -> ExpressionSegmentBinder.bind(same(expression), eq(SegmentType.PROJECTION), same(context), any(), same(outer)))
                    .thenThrow(new ColumnNotFoundException("foo", "field list"));
            shorthands.when(() -> ShorthandProjectionSegmentBinder.bind(any(), eq(null), same(expectedContexts))).thenAnswer(invocation -> invocation.getArgument(0));
            ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, context, null, tables, outer);
            ShorthandProjectionSegment actualProjection = (ShorthandProjectionSegment) actual.getProjections().iterator().next();
            assertThat(actualProjection.getStartIndex(), is(0));
            assertThat(actualProjection.getStopIndex(), is(16));
            assertThat(actualProjection.getOwner().get().getIdentifier(), is(new IdentifierValue("foo")));
            assertThat(actualProjection.getAliasSegment().get(), sameInstance(alias));
            assertThat(projection.getExpr(), sameInstance(expression));
            assertTrue(expression.isRowExpansion());
        }
    }
    
    @Test
    void assertBindCompositeColumnBeforeRowAlias() {
        ColumnSegment column = new ColumnSegment(1, 3, new IdentifierValue("foo"));
        CommonExpressionSegment expression = new CommonExpressionSegment(0, 6, "(foo).*", Collections.singletonList(column));
        expression.setRowExpansion(true);
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(0, 6, "(foo).*", expression);
        ProjectionsSegment projections = new ProjectionsSegment(0, 6);
        projections.getProjections().add(projection);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class, RETURNS_DEEP_STUBS);
        when(context.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        tables.put(CaseInsensitiveString.of("foo"), mock(TableSegmentBinderContext.class));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        CommonExpressionSegment boundExpression = new CommonExpressionSegment(0, 6, "(foo).*", Collections.singletonList(column));
        boundExpression.setRowExpansion(true);
        try (MockedStatic<ExpressionSegmentBinder> expressions = mockStatic(ExpressionSegmentBinder.class)) {
            expressions.when(() -> ExpressionSegmentBinder.bind(expression, SegmentType.PROJECTION, context, tables, outer)).thenReturn(boundExpression);
            ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, context, null, tables, outer);
            assertThat(((ExpressionProjectionSegment) actual.getProjections().iterator().next()).getExpr(), sameInstance(boundExpression));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("rowExpansionErrorArguments")
    void assertBindRowExpansionPreservesBindingFailure(final String caseName, final ProjectionSegment projection, final boolean supportWholeRow, final boolean visibleAlias) {
        ProjectionsSegment projections = new ProjectionsSegment(0, 20);
        projections.getProjections().add(projection);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class, RETURNS_DEEP_STUBS);
        when(context.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        if (visibleAlias) {
            tables.put(CaseInsensitiveString.of("foo"), mock(TableSegmentBinderContext.class));
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        DialectDatabaseMetaData metadata = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(metadata.getSQLOption().isSupportWholeRowProjection()).thenReturn(supportWholeRow);
        ColumnNotFoundException failure = new ColumnNotFoundException("foo", "field list");
        try (
                MockedStatic<ExpressionSegmentBinder> expressions = mockStatic(ExpressionSegmentBinder.class);
                MockedStatic<ColumnProjectionSegmentBinder> columns = mockStatic(ColumnProjectionSegmentBinder.class);
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class,
                        (registry, construction) -> when(registry.getDialectDatabaseMetaData()).thenReturn(metadata))) {
            expressions.when(() -> ExpressionSegmentBinder.bind(any(), eq(SegmentType.PROJECTION), same(context), any(), same(outer))).thenThrow(failure);
            columns.when(() -> ColumnProjectionSegmentBinder.bind(any(), same(context), any(), same(outer))).thenThrow(failure);
            ColumnNotFoundException actual = assertThrows(ColumnNotFoundException.class, () -> ProjectionsSegmentBinder.bind(projections, context, null, tables, outer));
            assertThat(actual, sameInstance(failure));
        }
    }
    
    private static Stream<Arguments> rowExpansionErrorArguments() {
        ColumnSegment column = new ColumnSegment(1, 3, new IdentifierValue("foo"));
        ColumnSegment qualified = new ColumnSegment(11, 13, new IdentifierValue("foo"));
        qualified.setOwner(new OwnerSegment(1, 9, new IdentifierValue("foo_table")));
        FunctionSegment function = new FunctionSegment(0, 13, "LENGTH", "LENGTH(foo)");
        function.getParameters().add(column);
        RowExpression row = new RowExpression(1, 8, "ROW(foo)");
        row.getItems().add(column);
        return Stream.of(
                Arguments.of("bare column", new ColumnProjectionSegment(column), false, false),
                Arguments.of("function expression", new ExpressionProjectionSegment(0, 13, function.getText(), function), true, true),
                Arguments.of("parenthesized column without star", createRowProjection(column, false), true, true),
                Arguments.of("dialect without whole row projection", createRowProjection(column, true), false, true),
                Arguments.of("qualified composite column", createRowProjection(qualified, true), true, true),
                Arguments.of("row constructor with missing column", createRowProjection(row, true), true, true),
                Arguments.of("invisible alias", createRowProjection(column, true), true, false));
    }
    
    private static ExpressionProjectionSegment createRowProjection(final ExpressionSegment child, final boolean rowExpansion) {
        CommonExpressionSegment expression = new CommonExpressionSegment(0, 20, "foo_expression", Collections.singletonList(child));
        expression.setRowExpansion(rowExpansion);
        return new ExpressionProjectionSegment(0, 20, expression.getText(), expression);
    }
    
}
