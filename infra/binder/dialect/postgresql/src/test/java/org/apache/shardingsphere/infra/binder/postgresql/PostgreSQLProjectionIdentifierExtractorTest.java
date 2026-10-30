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

package org.apache.shardingsphere.infra.binder.postgresql;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine.ProjectionEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.DialectProjectionIdentifierExtractor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLProjectionIdentifierExtractorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DialectProjectionIdentifierExtractor extractor = DatabaseTypedSPILoader.getService(DialectProjectionIdentifierExtractor.class, databaseType);
    
    @Test
    void assertGetIdentifierValue() {
        assertThat(extractor.getIdentifierValue(new IdentifierValue("Data", QuoteCharacter.NONE)), is("data"));
    }
    
    @Test
    void assertGetIdentifierValueWithUnicodeQuotedAlias() {
        assertThat(extractor.getIdentifierValue(new IdentifierValue("U&\"MyAlias\"", QuoteCharacter.NONE)), is("MyAlias"));
    }
    
    @Test
    void assertGetColumnNameFromFunction() {
        assertThat(extractor.getColumnNameFromFunction("Function", "FunctionExpression"), is("function"));
    }
    
    @Test
    void assertGetColumnNameFromExpression() {
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "expression")), is("?column?"));
    }
    
    @Test
    void assertGetColumnNameFromExpressionWithNonExpressionProjection() {
        assertThat(extractor.getColumnNameFromExpression(new ParameterMarkerExpressionSegment(0, 0, 0)), is("?column?"));
    }
    
    @Test
    void assertGetColumnNameFromFunctionExpression() {
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "SUM(ID)", new FunctionSegment(0, 0, "SUM", "SUM(ID)"))), is("sum"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNativeAndCastedExpressions")
    void assertGetColumnNameFromNativeAndCastedExpression(final String name, final ExpressionProjectionSegment projection, final String expected) {
        assertThat(extractor.getColumnNameFromExpression(projection), is(expected));
    }
    
    private static Stream<Arguments> getNativeAndCastedExpressions() {
        ExpressionProjectionSegment nativeLabel = new ExpressionProjectionSegment(7, 8, "42");
        nativeLabel.setDefaultColumnLabel(new IdentifierValue("column1"));
        String functionExpression = "ARRAY_AGG(ID)::text";
        TypeCastExpression functionCast = new TypeCastExpression(0, 18, functionExpression, new FunctionSegment(0, 12, "ARRAY_AGG", "ARRAY_AGG(ID)"), "text");
        String aggregationExpression = "MAX(ID)::text::varchar";
        AggregationProjectionSegment aggregation = new AggregationProjectionSegment(0, 6, AggregationType.MAX, "MAX(ID)");
        TypeCastExpression innerCast = new TypeCastExpression(0, 12, "MAX(ID)::text", aggregation, "text");
        TypeCastExpression outerCast = new TypeCastExpression(0, 21, aggregationExpression, innerCast, "varchar");
        String quotedFunctionExpression = "\"MyFunc\"(ID)::text";
        FunctionSegment function = new FunctionSegment(0, 11, "\"MyFunc\"", "\"MyFunc\"(ID)");
        TypeCastExpression quotedFunctionCast = new TypeCastExpression(0, 17, quotedFunctionExpression, function, "text");
        return Stream.of(Arguments.of("native VALUES label", nativeLabel, "column1"),
                Arguments.of("casted function", new ExpressionProjectionSegment(0, 18, functionExpression, functionCast), "array_agg"),
                Arguments.of("nested casted aggregate", new ExpressionProjectionSegment(0, 21, aggregationExpression, outerCast), "max"),
                Arguments.of("casted quoted function", new ExpressionProjectionSegment(0, 17, quotedFunctionExpression, quotedFunctionCast), "MyFunc"));
    }
    
    @Test
    void assertGetColumnNameFromQuotedFunctionExpression() {
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "\"MyFunc\"(ID)", new FunctionSegment(0, 0, "\"MyFunc\"", "\"MyFunc\"(ID)"))), is("MyFunc"));
    }
    
    @Test
    void assertGetColumnNameFromUnicodeQuotedFunctionExpression() {
        assertThat(extractor.getColumnNameFromExpression(
                new ExpressionProjectionSegment(0, 0, "U&\"MyFunc\"(ID)", new FunctionSegment(0, 0, "U&\"MyFunc\"", "U&\"MyFunc\"(ID)"))), is("MyFunc"));
    }
    
    @Test
    void assertGetColumnNameFromUnicodeQuotedFunctionExpressionWithDefaultEscape() {
        assertThat(extractor.getColumnNameFromExpression(
                new ExpressionProjectionSegment(0, 0, "U&\"d\\0061t\\+000041\"(ID)", new FunctionSegment(0, 0, "U&\"d\\0061t\\+000041\"", "U&\"d\\0061t\\+000041\"(ID)"))), is("datA"));
    }
    
    @Test
    void assertGetColumnNameFromUnicodeQuotedFunctionExpressionWithUescapeClause() {
        assertThat(extractor.getColumnNameFromExpression(
                new ExpressionProjectionSegment(0, 0, "u&\"d!0061t\" UESCAPE '!'(ID)", new FunctionSegment(0, 0, "u&\"d!0061t\"UESCAPE'!'", "u&\"d!0061t\" UESCAPE '!'(ID)"))), is("dat"));
    }
    
    @Test
    void assertGetColumnNameFromFunctionExpressionWithNonAsciiName() {
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "ÄBC(ID)", new FunctionSegment(0, 0, "ÄBC", "ÄBC(ID)"))), is("Äbc"));
    }
    
    @Test
    void assertGetColumnNameFromFunctionExpressionWithTurkishDefaultLocale() {
        Locale originalLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
        try {
            assertThat(extractor.getIdentifierValue(new IdentifierValue("INITCAP", QuoteCharacter.NONE)), is("initcap"));
            assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "INITCAP(ID)", new FunctionSegment(0, 0, "INITCAP", "INITCAP(ID)"))), is("initcap"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
    
    @Test
    void assertGetColumnLabelFromWindowAggregationProjection() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 0, AggregationType.MAX, "pg_catalog.max(ref_0.c36) over ()");
        aggregationProjectionSegment.setWindow(new WindowItemSegment(19, 27));
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(aggregationProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ExpressionProjection.class));
        assertThat(actual.get().getColumnLabel(), is("max"));
    }
    
    private ExpressionProjectionSegment createAliasedProjection(final IdentifierValue alias) {
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(0, 0, "1");
        result.setAlias(new AliasSegment(0, 0, alias));
        return result;
    }
    
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SubqueryOutputLabels {
        
        @ParameterizedTest(name = "{0}")
        @MethodSource("getSubqueryProjections")
        void assertGetColumnNameFromSubquery(final String name, final ProjectionSegment projection, final String expected) {
            ProjectionsSegment projections = new ProjectionsSegment(0, 0);
            projections.getProjections().add(projection);
            SelectStatement select = SelectStatement.builder().databaseType(databaseType).projections(projections).build();
            assertThat(extractor.getColumnNameFromSubquery(new SubqueryProjectionSegment(new SubquerySegment(0, 0, select, "SELECT 1"), "(SELECT 1)")), is(expected));
        }
        
        private Stream<Arguments> getSubqueryProjections() {
            ExpressionProjectionSegment aliased = createAliasedProjection(new IdentifierValue("Foo_Label"));
            ExpressionProjectionSegment quoted = createAliasedProjection(new IdentifierValue("Foo_Label", QuoteCharacter.QUOTE));
            ColumnProjectionSegment column = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("Foo_Column")));
            ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(0, 0);
            shorthand.getActualProjectionSegments().add(column);
            ExpressionProjectionSegment nativeLabel = new ExpressionProjectionSegment(0, 0, "1");
            nativeLabel.setDefaultColumnLabel(new IdentifierValue("column1"));
            ProjectionsSegment nestedProjections = new ProjectionsSegment(0, 0);
            nestedProjections.getProjections().add(aliased);
            SelectStatement nested = SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")).projections(nestedProjections).build();
            ProjectionSegment other = mock(ProjectionSegment.class);
            when(other.getColumnLabel()).thenReturn("foo_label");
            return Stream.of(Arguments.of("explicit alias", aliased, "foo_label"), Arguments.of("quoted alias", quoted, "Foo_Label"),
                    Arguments.of("column", column, "foo_column"), Arguments.of("bound shorthand", shorthand, "foo_column"),
                    Arguments.of("native VALUES label", nativeLabel, "column1"), Arguments.of("unnamed expression", new ExpressionProjectionSegment(0, 0, "1"), "?column?"),
                    Arguments.of("aggregate", new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"), "count"),
                    Arguments.of("distinct aggregate", new AggregationDistinctProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT id)", "id"), "count"),
                    Arguments.of("nested scalar subquery", new SubqueryProjectionSegment(new SubquerySegment(0, 0, nested, "SELECT 1 AS Foo_Label"), "(SELECT 1 AS Foo_Label)"), "foo_label"),
                    Arguments.of("projection label", other, "foo_label"));
        }
    }
    
}
