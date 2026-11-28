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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    
    @Test
    void assertGetNativeDefaultColumnLabel() {
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(7, 8, "42");
        projection.setDefaultColumnLabel(new IdentifierValue("column1"));
        assertThat(extractor.getColumnNameFromExpression(projection), is("column1"));
    }
    
    @Test
    void assertGetColumnNameFromCastedFunctionExpression() {
        String expression = "ARRAY_AGG(ID)::text";
        TypeCastExpression cast = new TypeCastExpression(0, 18, expression, new FunctionSegment(0, 12, "ARRAY_AGG", "ARRAY_AGG(ID)"), "text");
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 18, expression, cast)), is("array_agg"));
    }
    
    @Test
    void assertGetColumnNameFromNestedCastedAggregation() {
        String expression = "MAX(ID)::text::varchar";
        AggregationProjectionSegment aggregation = new AggregationProjectionSegment(0, 6, AggregationType.MAX, "MAX(ID)");
        TypeCastExpression innerCast = new TypeCastExpression(0, 12, "MAX(ID)::text", aggregation, "text");
        TypeCastExpression outerCast = new TypeCastExpression(0, 21, expression, innerCast, "varchar");
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 21, expression, outerCast)), is("max"));
    }
    
    @Test
    void assertGetColumnNameFromCastedQuotedFunctionExpression() {
        String expression = "\"MyFunc\"(ID)::text";
        FunctionSegment function = new FunctionSegment(0, 11, "\"MyFunc\"", "\"MyFunc\"(ID)");
        TypeCastExpression cast = new TypeCastExpression(0, 17, expression, function, "text");
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 17, expression, cast)), is("MyFunc"));
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
    
    @Test
    void assertGetColumnNameFromSubquery() {
        assertThat(extractor.getColumnNameFromSubquery(new SubqueryProjectionSegment(new SubquerySegment(0, 0, "text"), "text")), is("text"));
    }
}
