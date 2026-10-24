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

package org.apache.shardingsphere.test.it.sql.parser.postgresql;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.PrivilegeObjectSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserIT;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserITSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@InternalSQLParserITSettings("PostgreSQL")
class InternalPostgreSQLParserIT extends InternalSQLParserIT {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getQuotedIdentifierCases")
    void assertQuotedIdentifierRoundTrip(final String name, final String identifier, final String expectedValue, final String expectedQuotedValue) {
        String sql = "SELECT " + identifier + " AS " + identifier + " FROM " + identifier + "." + identifier + " AS " + identifier;
        SelectStatement actual = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        ColumnProjectionSegment actualProjection = (ColumnProjectionSegment) actual.getProjections().getProjections().iterator().next();
        SimpleTableSegment actualTable = (SimpleTableSegment) actual.getFrom().get();
        ColumnSegment actualColumn = actualProjection.getColumn();
        assertThat(sql.substring(actualColumn.getStartIndex(), actualColumn.getStopIndex() + 1), is(identifier));
        assertThat(sql.substring(actualTable.getTableName().getStartIndex(), actualTable.getTableName().getStopIndex() + 1), is(identifier));
        Collection<IdentifierValue> actualIdentifiers = Arrays.asList(actualColumn.getIdentifier(), actualProjection.getAlias().get(), actualTable.getOwner().get().getIdentifier(),
                actualTable.getTableName().getIdentifier(), actualTable.getAlias().get());
        for (IdentifierValue each : actualIdentifiers) {
            assertThat(each.getValue(), is(expectedValue));
            assertThat(each.getValueWithQuoteCharacters(), is(expectedQuotedValue));
            String roundTripSQL = "SELECT " + each.getValueWithQuoteCharacters();
            SelectStatement actualRoundTrip = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL")
                    .visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(roundTripSQL, false));
            assertThat(((ColumnProjectionSegment) actualRoundTrip.getProjections().getProjections().iterator().next()).getColumn().getIdentifier().getValue(), is(expectedValue));
        }
    }
    
    private static Stream<Arguments> getQuotedIdentifierCases() {
        return Stream.of(
                Arguments.of("one embedded quote", "\"foo\"\"bar\"", "foo\"bar", "\"foo\"\"bar\""),
                Arguments.of("two adjacent embedded quotes", "\"foo\"\"\"\"bar\"", "foo\"\"bar", "\"foo\"\"\"\"bar\""),
                Arguments.of("default Unicode escape", "U&\"foo\\0061\"", "fooa", "\"fooa\""),
                Arguments.of("six digit Unicode escape", "U&\"foo\\+01F600\"", "foo😀", "\"foo😀\""),
                Arguments.of("surrogate pair Unicode escape", "U&\"foo\\D83D\\DE00\"", "foo😀", "\"foo😀\""),
                Arguments.of("literal Unicode escape character", "U&\"foo\\\\bar\"", "foo\\bar", "\"foo\\bar\""),
                Arguments.of("custom Unicode escape", "U&\"foo!0061\" UESCAPE '!'", "fooa", "\"fooa\""),
                Arguments.of("escaped custom Unicode escape", "U&\"foo!0061\" UESCAPE E'\\041'", "fooa", "\"fooa\""),
                Arguments.of("escaped backslash Unicode escape", "U&\"foo\\0061\" UESCAPE E'\\\\'", "fooa", "\"fooa\""),
                Arguments.of("Unicode embedded quote", "U&\"foo\\0022bar\"", "foo\"bar", "\"foo\"\"bar\""),
                Arguments.of("Unicode adjacent decoded quotes", "U&\"foo\\0022\\0022bar\"", "foo\"\"bar", "\"foo\"\"\"\"bar\""),
                Arguments.of("Unicode decoded and literal quotes", "U&\"foo!0022\"\"bar\" UESCAPE '!'", "foo\"\"bar", "\"foo\"\"\"\"bar\""));
    }
    
    @Test
    void assertCreateSchemaQuotedIdentifierRoundTrip() {
        String sql = "CREATE SCHEMA U&\"foo!0022\"\"schema\" UESCAPE '!'";
        CreateSchemaStatement actual = (CreateSchemaStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        IdentifierValue actualIdentifier = actual.getSchemaName().get();
        assertThat(actualIdentifier.getValue(), is("foo\"\"schema"));
        String roundTripSQL = "CREATE SCHEMA " + actualIdentifier.getValueWithQuoteCharacters();
        CreateSchemaStatement actualRoundTrip = (CreateSchemaStatement) new SQLStatementVisitorEngine("PostgreSQL")
                .visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(roundTripSQL, false));
        assertThat(actualRoundTrip.getSchemaName().get().getValue(), is("foo\"\"schema"));
    }
    
    @Test
    void assertCreateIndexQuotedColumnRoundTrip() {
        String identifier = "U&\"foo\\0022\\0022column\"";
        String sql = "CREATE INDEX foo_index ON foo_table (" + identifier + ")";
        CreateIndexStatement actual = (CreateIndexStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        ColumnSegment actualColumn = actual.getColumns().iterator().next();
        assertThat(actualColumn.getIdentifier().getValue(), is("foo\"\"column"));
        assertThat(sql.substring(actualColumn.getStartIndex(), actualColumn.getStopIndex() + 1), is(identifier));
        String roundTripSQL = "CREATE INDEX foo_index ON foo_table (" + actualColumn.getIdentifier().getValueWithQuoteCharacters() + ")";
        CreateIndexStatement actualRoundTrip = (CreateIndexStatement) new SQLStatementVisitorEngine("PostgreSQL")
                .visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(roundTripSQL, false));
        assertThat(actualRoundTrip.getColumns().iterator().next().getIdentifier().getValue(), is("foo\"\"column"));
    }
    
    @Test
    void assertCreateTriggerQuotedIdentifierRoundTrip() {
        String triggerClause = " BEFORE INSERT ON foo_table FOR EACH ROW EXECUTE FUNCTION foo_function()";
        String sql = "CREATE TRIGGER U&\"foo!0022!0022trigger\" UESCAPE '!'" + triggerClause;
        CreateTriggerStatement actual = (CreateTriggerStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        IdentifierValue actualIdentifier = actual.getTriggerName().get().getIdentifier();
        assertThat(actualIdentifier.getValue(), is("foo\"\"trigger"));
        String roundTripSQL = "CREATE TRIGGER " + actualIdentifier.getValueWithQuoteCharacters() + triggerClause;
        CreateTriggerStatement actualRoundTrip = (CreateTriggerStatement) new SQLStatementVisitorEngine("PostgreSQL")
                .visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(roundTripSQL, false));
        assertThat(actualRoundTrip.getTriggerName().get().getIdentifier().getValue(), is("foo\"\"trigger"));
    }
    
    @Test
    void assertAnalyzeQuotedTableRoundTrip() {
        String identifier = "U&\"foo!0022\"\"table\" UESCAPE '!'";
        String sql = "ANALYZE " + identifier;
        AnalyzeTableStatement actual = (AnalyzeTableStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        SimpleTableSegment actualTable = actual.getTables().iterator().next();
        assertThat(actualTable.getTableName().getIdentifier().getValue(), is("foo\"\"table"));
        assertThat(sql.substring(actualTable.getStartIndex(), actualTable.getStopIndex() + 1), is(identifier));
        String roundTripSQL = "ANALYZE " + actualTable.getTableName().getIdentifier().getValueWithQuoteCharacters();
        AnalyzeTableStatement actualRoundTrip = (AnalyzeTableStatement) new SQLStatementVisitorEngine("PostgreSQL")
                .visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(roundTripSQL, false));
        assertThat(actualRoundTrip.getTables().iterator().next().getTableName().getIdentifier().getValue(), is("foo\"\"table"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getQuotedPrivilegeObjectCases")
    void assertQuotedPrivilegeObjectRoundTrip(final String name, final String sqlPrefix, final String objectName, final String sqlSuffix, final Collection<String> expectedIdentifiers) {
        String sql = sqlPrefix + objectName + sqlSuffix;
        GrantStatement actual = (GrantStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        PrivilegeObjectSegment actualObject = actual.getPrivilegeObjects().iterator().next();
        assertThat(actualObject.getIdentifiers().stream().map(IdentifierValue::getValue).collect(Collectors.toList()), is(expectedIdentifiers));
        String roundTripSQL = sqlPrefix + actualObject.getIdentifiers().stream().map(IdentifierValue::getValueWithQuoteCharacters).collect(Collectors.joining(".")) + sqlSuffix;
        GrantStatement actualRoundTrip = (GrantStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(roundTripSQL, false));
        assertThat(actualRoundTrip.getPrivilegeObjects().iterator().next().getIdentifiers().stream().map(IdentifierValue::getValue).collect(Collectors.toList()), is(expectedIdentifiers));
    }
    
    private static Stream<Arguments> getQuotedPrivilegeObjectCases() {
        return Stream.of(
                Arguments.of("schema name", "GRANT ALL ON SCHEMA ", "U&\"foo\\0061\"", " TO foo_role", Collections.singletonList("fooa")),
                Arguments.of("type name and attribute", "GRANT ALL ON TYPE ", "U&\"foo\\0061\".U&\"bar!0022!0022\" UESCAPE '!'", " TO foo_role", Arrays.asList("fooa", "bar\"\"")),
                Arguments.of("function owner and name", "GRANT ALL ON FUNCTION ", "U&\"foo\\0061\".U&\"bar!0062\" UESCAPE '!'", "(integer) TO foo_role", Arrays.asList("fooa", "barb")),
                Arguments.of("function indirection", "GRANT ALL ON FUNCTION ", "U&\"foo\\0061\".U&\"bar!0062\" UESCAPE '!'", " TO foo_role", Arrays.asList("fooa", "barb")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUnsupportedFilterExpressions")
    void assertUnsupportedFilter(final String expression) {
        String sql = "SELECT " + expression;
        assertThrows(SQLParsingException.class, () -> new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false)));
    }
    
    private static Stream<String> getUnsupportedFilterExpressions() {
        return Stream.of(
                "SUM(id) FILTER (WHERE TRUE) FROM foo_table",
                "COUNT(*) FILTER (WHERE FALSE)",
                "COUNT(DISTINCT ?) FILTER (WHERE ?)",
                "SUM(?) FILTER (WHERE ?) OVER (PARTITION BY ? ORDER BY ? ROWS ? PRECEDING)",
                "JSON_ARRAYAGG(?) FILTER (WHERE ?)",
                "JSON_ARRAYAGG(?) FILTER (WHERE ?) OVER ()",
                "JSON_OBJECTAGG('foo' VALUE ?) FILTER (WHERE ?)",
                "SUM((SELECT ?)) FILTER (WHERE EXISTS (SELECT ?))",
                "ABS((SELECT SUM(?) FILTER (WHERE ?)))");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUnfilteredAggregateCases")
    void assertUnfilteredAggregate(final String name, final String expression, final Class<?> expectedType, final int expectedArgCount,
                                   final boolean expectedWindow, final Collection<Integer> expectedParams) {
        String sql = "SELECT " + expression + " AS aggregate_value FROM foo";
        SelectStatement actual = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        ProjectionSegment actualProjection = actual.getProjections().getProjections().iterator().next();
        ExpressionSegment actualExpression = actualProjection instanceof ExpressionProjectionSegment
                ? ((ExpressionProjectionSegment) actualProjection).getExpr()
                : (ExpressionSegment) actualProjection;
        assertThat(actualProjection.getColumnLabel(), is("aggregate_value"));
        assertThat(actualExpression.getClass(), is(expectedType));
        assertThat(sql.substring(actualExpression.getStartIndex(), actualExpression.getStopIndex() + 1), is(actualExpression.getText()));
        if (actualExpression instanceof FunctionSegment) {
            FunctionSegment actualFunction = (FunctionSegment) actualExpression;
            assertThat(actualFunction.getParameters().size(), is(expectedArgCount));
            assertThat(actualFunction.getWindow().isPresent(), is(expectedWindow));
        } else {
            AggregationProjectionSegment actualAggregation = (AggregationProjectionSegment) actualExpression;
            assertThat(actualAggregation.getParameters().size(), is(expectedArgCount));
            assertThat(actualAggregation.getWindow().isPresent(), is(expectedWindow));
        }
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedParams));
    }
    
    private static Stream<Arguments> getUnfilteredAggregateCases() {
        return Stream.of(
                Arguments.of("ordinary aggregate", "SUM(?)", AggregationProjectionSegment.class, 1, false, Collections.singletonList(0)),
                Arguments.of("star aggregate", "COUNT(*)", AggregationProjectionSegment.class, 0, false, Collections.emptyList()),
                Arguments.of("distinct aggregate", "COUNT(DISTINCT ?)", AggregationDistinctProjectionSegment.class, 1, false, Collections.singletonList(0)),
                Arguments.of("aggregate window", "SUM(?) OVER (PARTITION BY ? ORDER BY ? ROWS ? PRECEDING)", AggregationProjectionSegment.class, 1, true, Arrays.asList(0, 1, 2, 3)),
                Arguments.of("distinct window", "COUNT(DISTINCT ?) OVER (ORDER BY ?)", AggregationDistinctProjectionSegment.class, 1, true, Arrays.asList(0, 1)),
                Arguments.of("JSON aggregate", "JSON_ARRAYAGG(?)", FunctionSegment.class, 1, false, Collections.singletonList(0)),
                Arguments.of("JSON window", "JSON_ARRAYAGG(?) OVER (PARTITION BY ? ORDER BY ? ROWS ? PRECEDING)", FunctionSegment.class, 1, true, Arrays.asList(0, 1, 2, 3)),
                Arguments.of("function named filter", "filter(?)", FunctionSegment.class, 1, false, Collections.singletonList(0)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getQuantifiedPatternSubqueryCases")
    void assertQuantifiedPatternSubqueryMarkers(final String name, final String sql, final Collection<Integer> expectedParams, final Collection<Integer> expectedSubqueryParams) {
        InsertStatement actual = (InsertStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        List<ExpressionSegment> actualValues = actual.getValues().iterator().next().getValues();
        BinaryOperationExpression actualExpression = (BinaryOperationExpression) actualValues.get(1);
        SubqueryExpressionSegment actualSubquery = (SubqueryExpressionSegment) ((ListExpression) actualExpression.getRight()).getItems().iterator().next();
        SelectStatement actualSelect = actualSubquery.getSubquery().getSelect();
        assertThat(actual.getParameterCount(), is(expectedParams.size()));
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedParams));
        assertThat(actualSelect.getParameterCount(), is(expectedSubqueryParams.size()));
        assertThat(actualSelect.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedSubqueryParams));
        ParameterMarkerSegment actualMarker = actualSelect.getParameterMarkers().iterator().next();
        assertThat(actualMarker.getStartIndex(), is(sql.indexOf("SELECT ?") + 7));
        assertThat(actualMarker.getStopIndex(), is(actualMarker.getStartIndex()));
        assertThat(ExpressionExtractor.getParameterMarkerExpressions(actualValues).stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedParams));
    }
    
    private static Stream<Arguments> getQuantifiedPatternSubqueryCases() {
        return Stream.of(
                Arguments.of("like any", "INSERT INTO foo_table(foo_before, foo_match, foo_after) VALUES (?, ? LIKE ANY (SELECT ?), ?)", Arrays.asList(0, 1, 2, 3), Collections.singletonList(2)),
                Arguments.of("not like all", "INSERT INTO foo_table(foo_before, foo_match, foo_after) VALUES (?, ? NOT LIKE ALL (SELECT ?), ?)", Arrays.asList(0, 1, 2, 3),
                        Collections.singletonList(2)),
                Arguments.of("ilike any ordered markers", "INSERT INTO foo_table(foo_before, foo_match, foo_after) VALUES (?, ? ILIKE ANY (SELECT ? UNION ALL SELECT ?), ?)",
                        Arrays.asList(0, 1, 2, 3, 4), Arrays.asList(2, 3)));
    }
    
    @Test
    void assertQuantifiedPatternArrayMarkers() {
        String sql = "INSERT INTO foo_table(foo_before, foo_match, foo_after) VALUES (?, ? LIKE ANY (ARRAY[?]), ?)";
        InsertStatement actual = (InsertStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        assertThat(actual.getParameterCount(), is(4));
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), contains(0, 1, 2, 3));
        assertThat(ExpressionExtractor.getParameterMarkerExpressions(actual.getValues().iterator().next().getValues()).stream()
                .map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), contains(0, 1, 2, 3));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getJsonArraySubqueryCases")
    void assertJsonArraySubqueryMarkers(final String name, final String sql, final Collection<Integer> expectedParams, final Collection<Integer> expectedSubqueryParams) {
        InsertStatement actual = (InsertStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        List<ExpressionSegment> actualValues = actual.getValues().iterator().next().getValues();
        FunctionSegment actualExpression = (FunctionSegment) actualValues.get(1);
        SubqueryExpressionSegment actualSubquery = (SubqueryExpressionSegment) actualExpression.getParameters().iterator().next();
        SelectStatement actualSelect = actualSubquery.getSubquery().getSelect();
        assertThat(actualSelect.getParameterCount(), is(expectedSubqueryParams.size()));
        assertThat(actualSelect.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedSubqueryParams));
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedParams));
        assertThat(ExpressionExtractor.getParameterMarkerExpressions(actualValues).stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedParams));
    }
    
    private static Stream<Arguments> getJsonArraySubqueryCases() {
        return Stream.of(
                Arguments.of("json array scoped marker", "INSERT INTO foo_table(foo_before, foo_json, foo_after) VALUES (?, JSON_ARRAY(SELECT ?), ?)",
                        Arrays.asList(0, 1, 2), Collections.singletonList(1)),
                Arguments.of("json array ordered markers", "INSERT INTO foo_table(foo_before, foo_json, foo_after) VALUES (?, JSON_ARRAY(SELECT ? UNION ALL SELECT ?), ?)",
                        Arrays.asList(0, 1, 2, 3), Arrays.asList(1, 2)),
                Arguments.of("json array without markers", "INSERT INTO foo_table(foo_before, foo_json, foo_after) VALUES (1, JSON_ARRAY(SELECT 2), 3)",
                        Collections.emptyList(), Collections.emptyList()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCaseJsonArraySubqueryCases")
    void assertCaseJsonArraySubqueryMarkers(final String name, final String sql, final Collection<Integer> expectedSubqueryParams) {
        InsertStatement actual = (InsertStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        CaseWhenExpression actualExpression = (CaseWhenExpression) actual.getValues().iterator().next().getValues().get(0);
        FunctionSegment actualFunction = (FunctionSegment) ((TypeCastExpression) actualExpression.getCaseExpr()).getExpression();
        SubqueryExpressionSegment actualSubquery = (SubqueryExpressionSegment) actualFunction.getParameters().iterator().next();
        assertThat(actualSubquery.getSubquery().getSelect().getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedSubqueryParams));
        assertThat(ExpressionExtractor.getParameterMarkerExpressions(actual.getValues().iterator().next().getValues()).size(), is(actual.getParameterCount()));
    }
    
    private static Stream<Arguments> getCaseJsonArraySubqueryCases() {
        return Stream.of(
                Arguments.of("case json array marker", "INSERT INTO foo_table(foo_case, foo_after) VALUES (CASE (JSON_ARRAY(SELECT ?)::text) WHEN ? THEN ? ELSE ? END, ?)",
                        Collections.singletonList(0)),
                Arguments.of("case json array ordered markers", "INSERT INTO foo_table(foo_case, foo_after) VALUES (CASE (JSON_ARRAY(SELECT ? UNION ALL SELECT ?)::text) WHEN ? THEN ? ELSE ? END, ?)",
                        Arrays.asList(0, 1)),
                Arguments.of("case json array without markers", "INSERT INTO foo_table(foo_case, foo_after) VALUES (CASE (JSON_ARRAY(SELECT 1)::text) WHEN ? THEN ? ELSE ? END, ?)",
                        Collections.emptyList()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCaseParameterMarkerCases")
    void assertCaseParameterMarkers(final String name, final String sql, final Collection<String> expectedParams) {
        InsertStatement actual = (InsertStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        assertThat(actual.getParameterMarkers().stream().sorted(Comparator.comparingInt(ParameterMarkerSegment::getStartIndex))
                .map(each -> each.getStartIndex() + ":" + each.getParameterIndex()).collect(Collectors.toList()), is(expectedParams));
        assertThat(ExpressionExtractor.getParameterMarkerExpressions(actual.getValues().iterator().next().getValues()).stream().sorted(Comparator.comparingInt(ParameterMarkerSegment::getStartIndex))
                .map(each -> each.getStartIndex() + ":" + each.getParameterIndex()).collect(Collectors.toList()), is(expectedParams));
    }
    
    private static Stream<Arguments> getCaseParameterMarkerCases() {
        return Stream.of(
                Arguments.of("prefix and multiple branches", "INSERT INTO foo_table VALUES (?, CASE ? WHEN ? THEN ? WHEN ? THEN ? ELSE ? END, ?)",
                        Arrays.asList("30:0", "38:1", "45:2", "52:3", "59:4", "66:5", "73:6", "80:7")),
                Arguments.of("searched case", "INSERT INTO foo_table VALUES (CASE WHEN ? THEN ? ELSE ? END)", Arrays.asList("40:0", "47:1", "54:2")),
                Arguments.of("nested case", "INSERT INTO foo_table VALUES (CASE ? WHEN ? THEN CASE ? WHEN ? THEN ? ELSE ? END ELSE ? END)",
                        Arrays.asList("35:0", "42:1", "54:2", "61:3", "68:4", "75:5", "86:6")),
                Arguments.of("explicit dollar indices", "INSERT INTO foo_table VALUES (CASE $3 WHEN $1 THEN $5 ELSE $2 END, $4)", Arrays.asList("35:2", "43:0", "51:4", "59:1", "67:3")),
                Arguments.of("prefix and json subquery", "INSERT INTO foo_table VALUES (?, CASE (JSON_ARRAY(SELECT ? UNION ALL SELECT ?)::text) WHEN ? THEN ? ELSE ? END, ?)",
                        Arrays.asList("30:0", "57:1", "76:2", "91:3", "98:4", "105:5", "112:6")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getColumnExtractionCases")
    void assertExtractSelectColumns(final String name, final String sql, final Collection<String> expectedColumns) {
        SelectStatement selectStatement = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL")
                .visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        Collection<ColumnSegment> actual = new ArrayList<>(expectedColumns.size());
        ColumnExtractor.extractFromSelectStatement(actual, selectStatement, false);
        assertThat(actual.stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()), is(expectedColumns));
    }
    
    private static Stream<Arguments> getColumnExtractionCases() {
        return Stream.of(
                Arguments.of("Array projection column", "SELECT foo_array[1] FROM foo_table", Collections.singletonList("foo_array")),
                Arguments.of("Array where column", "SELECT 1 FROM foo_table WHERE foo_array[1] = 7", Collections.singletonList("foo_array")),
                Arguments.of("Row projection columns", "SELECT ROW(foo_a, foo_b) FROM foo_table", Arrays.asList("foo_a", "foo_b")),
                Arguments.of("Row where columns", "SELECT 1 FROM foo_table WHERE ROW(foo_a, foo_b) = ROW(1, 2)", Arrays.asList("foo_a", "foo_b")),
                Arguments.of("Unary projection column", "SELECT ~foo_a FROM foo_table", Collections.singletonList("foo_a")),
                Arguments.of("Unary where column", "SELECT 1 FROM foo_table WHERE ~foo_a > 0", Collections.singletonList("foo_a")),
                Arguments.of("Collate projection column", "SELECT foo_a COLLATE \"C\" FROM foo_table", Collections.singletonList("foo_a")),
                Arguments.of("Collate where column", "SELECT 1 FROM foo_table WHERE foo_a COLLATE \"C\" = 'foo_value'", Collections.singletonList("foo_a")),
                Arguments.of("Plain projection column", "SELECT foo_a FROM foo_table", Collections.singletonList("foo_a")),
                Arguments.of("Plain where column", "SELECT 1 FROM foo_table WHERE foo_a = 7", Collections.singletonList("foo_a")));
    }
    
    @Test
    void assertArrayAggregationTableOwner() {
        String sql = "SELECT ARRAY[SUM(foo_table.id)] FROM foo_table";
        SelectStatement actual = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(actual);
        assertThat(tableExtractor.getRewriteTables().stream().map(SimpleTableSegment::getStartIndex).collect(Collectors.toList()), contains(37, 17));
        assertThat(tableExtractor.getRewriteTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), contains("foo_table", "foo_table"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUpdateClauseMarkerCases")
    void assertUpdateClauseMarkers(final String name, final String sql, final Collection<String> expectedMarkers, final int expectedWhereIndex) {
        UpdateStatement actual = (UpdateStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        assertThat(actual.getParameterMarkers().stream().map(each -> each.getStartIndex() + ":" + each.getParameterIndex()).collect(Collectors.toList()), is(expectedMarkers));
        ParameterMarkerExpressionSegment actualWhereMarker = (ParameterMarkerExpressionSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getRight();
        assertThat(actualWhereMarker.getParameterMarkerIndex(), is(expectedWhereIndex));
    }
    
    private static Stream<Arguments> getUpdateClauseMarkerCases() {
        return Stream.of(
                Arguments.of("JSON table source", "UPDATE foo_table SET foo_column = ? FROM JSON_TABLE(?, '$' COLUMNS (foo_value int PATH '$')) foo_json WHERE foo_id = ?",
                        Arrays.asList("34:0", "52:1", "117:2"), 2),
                Arguments.of("XML table source", "UPDATE foo_table SET foo_column = ? FROM XMLTABLE('/row' PASSING ? COLUMNS foo_value int PATH '.') foo_xml WHERE foo_id = ?",
                        Arrays.asList("34:0", "65:1", "122:2"), 2),
                Arguments.of("ordinary subquery source", "UPDATE foo_table SET foo_column = ? FROM (SELECT ? AS foo_value) foo_source WHERE foo_id = ?",
                        Arrays.asList("34:0", "49:1", "91:2"), 2),
                Arguments.of("without source", "UPDATE foo_table SET foo_column = ? WHERE foo_id = ?", Arrays.asList("34:0", "51:1"), 1),
                Arguments.of("explicit dollar indices", "UPDATE foo_table SET foo_column = $3 FROM JSON_TABLE($1, '$' COLUMNS (foo_value int PATH '$')) foo_json WHERE foo_id = $2",
                        Arrays.asList("34:2", "53:0", "119:1"), 1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDeleteUsingMarkerCases")
    void assertDeleteUsingMarkers(final String name, final String sql, final Collection<String> expectedMarkers) {
        DeleteStatement actual = (DeleteStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        assertThat(actual.getParameterMarkers().stream().map(each -> each.getStartIndex() + ":" + each.getParameterIndex()).collect(Collectors.toList()), is(expectedMarkers));
    }
    
    private static Stream<Arguments> getDeleteUsingMarkerCases() {
        return Stream.of(
                Arguments.of("JSON table source", "DELETE FROM foo USING JSON_TABLE(?, '$' COLUMNS (id int PATH '$')) AS jt WHERE foo.id = ? RETURNING ?",
                        Arrays.asList("33:0", "88:1", "100:2")),
                Arguments.of("source without where or returning", "DELETE FROM foo USING JSON_TABLE(?, '$' COLUMNS (id int PATH '$')) AS jt", Collections.singletonList("33:0")),
                Arguments.of("multiple sources", "DELETE FROM foo USING JSON_TABLE(?, '$' COLUMNS (id int PATH '$')) AS jt, (SELECT ? AS id) AS src WHERE foo.id = ? RETURNING ?",
                        Arrays.asList("33:0", "82:1", "113:2", "125:3")),
                Arguments.of("explicit dollar indices", "DELETE FROM foo USING JSON_TABLE($3, '$' COLUMNS (id int PATH '$')) AS jt WHERE foo.id = $1 RETURNING $1",
                        Arrays.asList("33:2", "89:0", "102:0")),
                Arguments.of("with before using", "WITH seed AS (SELECT ?) DELETE FROM foo USING JSON_TABLE(?, '$' COLUMNS (id int PATH '$')) AS jt WHERE foo.id = ? RETURNING ?",
                        Arrays.asList("21:0", "57:1", "112:2", "124:3")),
                Arguments.of("ordinary subquery source", "DELETE FROM foo USING (SELECT ? AS id) AS src WHERE foo.id = ? RETURNING ?", Arrays.asList("30:0", "61:1", "73:2")));
    }
    
    @Test
    void assertDeleteUsingTables() {
        String sql = "DELETE FROM foo_schema.foo_table AS foo USING foo_source AS src, JSON_TABLE(?, '$' COLUMNS (id int PATH '$')) AS jt WHERE foo.id = jt.id RETURNING foo.id";
        DeleteStatement actual = (DeleteStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        DeleteMultiTableSegment actualTables = (DeleteMultiTableSegment) actual.getTable();
        assertThat(actualTables.getActualDeleteTables().size(), is(1));
        SimpleTableSegment actualTarget = actualTables.getActualDeleteTables().get(0);
        assertThat(actualTarget.getTableName().getIdentifier().getValue(), is("foo_table"));
        assertThat(actualTarget.getOwner().get().getIdentifier().getValue(), is("foo_schema"));
        assertThat(actualTarget.getAliasName().get(), is("foo"));
        JoinTableSegment actualRelation = (JoinTableSegment) actualTables.getRelationTable();
        assertThat(actualRelation.getJoinType(), is(JoinType.COMMA.name()));
        assertThat(actualRelation.getLeft().getAliasName().get(), is("foo"));
        JoinTableSegment actualSources = (JoinTableSegment) actualRelation.getRight();
        assertThat(((SimpleTableSegment) actualSources.getLeft()).getTableName().getIdentifier().getValue(), is("foo_source"));
        assertThat(actualSources.getLeft().getAliasName().get(), is("src"));
        FunctionTableSegment actualJsonTable = (FunctionTableSegment) actualSources.getRight();
        assertThat(actualJsonTable.getAliasName().get(), is("jt"));
        assertThat(actualJsonTable.getColumns().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()), contains("id"));
        assertThat(sql.substring(actualJsonTable.getStartIndex(), actualJsonTable.getStopIndex() + 1), is("JSON_TABLE(?, '$' COLUMNS (id int PATH '$')) AS jt"));
        FunctionSegment actualFunction = (FunctionSegment) actualJsonTable.getTableFunction();
        assertThat(actualFunction.getFunctionName(), is("JSON_TABLE"));
        assertThat(((ParameterMarkerExpressionSegment) actualFunction.getParameters().iterator().next()).getParameterMarkerIndex(), is(0));
    }
    
    @Test
    void assertDeleteWithoutUsing() {
        String sql = "DELETE FROM foo_schema.foo_table AS foo WHERE foo.id = ? RETURNING ?";
        DeleteStatement actual = (DeleteStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        SimpleTableSegment actualTable = (SimpleTableSegment) actual.getTable();
        assertThat(actualTable.getTableName().getIdentifier().getValue(), is("foo_table"));
        assertThat(actualTable.getOwner().get().getIdentifier().getValue(), is("foo_schema"));
        assertThat(actualTable.getAliasName().get(), is("foo"));
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), contains(0, 1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDeleteUsingJoinAliasPrefixes")
    void assertDeleteUsingJoinAliasAttributes(final String prefix) {
        String sql = prefix + "DELETE FROM foo_target USING JSON_TABLE(?, '$' COLUMNS (id int PATH '$')) AS jt JOIN foo_source USING (id) AS joined WHERE foo_target.id = joined.id";
        SQLStatement actual = new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        Collection<String> actualTables = actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue()).distinct().collect(Collectors.toList());
        assertThat(actualTables, contains("foo_target", "foo_source"));
    }
    
    private static Stream<String> getDeleteUsingJoinAliasPrefixes() {
        return Stream.of("PREPARE foo_stmt AS ", "EXPLAIN ");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUsingAliasTableMetadataCases")
    void assertUsingAliasTableMetadata(final String name, final String prefix, final String sql, final Collection<String> expectedTables) {
        SQLStatement actual = new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(prefix + sql, false));
        Collection<String> actualTables = actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue() + "@" + (each.getStartIndex() - prefix.length()) + ":" + (each.getStopIndex() - prefix.length()))
                .collect(Collectors.toList());
        assertThat(actualTables, is(expectedTables));
    }
    
    private static Stream<Arguments> getUsingAliasTableMetadataCases() {
        return Stream.of(
                Arguments.of("correlated LATERAL", "SELECT x.id FROM foo_left JOIN foo_right USING (id) AS merged, LATERAL (SELECT merged.id) AS x",
                        Arrays.asList("foo_left@17:24", "foo_right@31:39")),
                Arguments.of("quoted USING alias", "SELECT foo_outer.id FROM foo_outer, foo_left JOIN foo_right USING (id) AS \"FOO_OUTER\"",
                        Arrays.asList("foo_outer@25:33", "foo_left@36:43", "foo_right@50:58", "foo_outer@7:15")),
                Arguments.of("nested USING activation", "SELECT (SELECT foo_outer.id FROM foo_outer, foo_left JOIN foo_right USING (id) AS \"FOO_OUTER\")",
                        Arrays.asList("foo_outer@33:41", "foo_left@44:51", "foo_right@58:66", "foo_outer@15:23")),
                Arguments.of("USING below MERGE", "MERGE INTO foo_target USING foo_source ON EXISTS ("
                        + "SELECT foo_outer.id FROM foo_outer, foo_left JOIN foo_right USING (id) AS \"FOO_OUTER\") WHEN MATCHED THEN DO NOTHING",
                        Arrays.asList("foo_target@11:20", "foo_source@28:37", "foo_outer@75:83", "foo_left@86:93", "foo_right@100:108", "foo_outer@57:65")),
                Arguments.of("UPDATE FROM USING", "UPDATE foo_outer SET id = 1 FROM foo_left JOIN foo_right USING (id) AS \"FOO_OUTER\" WHERE foo_outer.id = 1",
                        Arrays.asList("foo_outer@7:15", "foo_left@33:40", "foo_right@47:55", "foo_outer@89:97")),
                Arguments.of("DELETE USING", "DELETE FROM foo_target USING foo_outer, foo_left JOIN foo_right USING (id) AS \"FOO_OUTER\" WHERE foo_target.id = foo_outer.id",
                        Arrays.asList("foo_target@12:21", "foo_target@12:21", "foo_outer@29:37", "foo_left@40:47", "foo_right@54:62", "foo_target@96:105", "foo_outer@112:120")),
                Arguments.of("regular JOIN alias", "SELECT u.id FROM u, (foo_left JOIN foo_right USING (id) AS u) AS j",
                        Arrays.asList("u@17:17", "foo_left@21:28", "foo_right@35:43", "u@7:7")),
                Arguments.of("enclosing JOIN alias", "SELECT u.id FROM u, (foo_left JOIN foo_right USING (id) AS u JOIN foo_third ON u.id = foo_third.id) AS j",
                        Arrays.asList("u@17:17", "foo_left@21:28", "foo_right@35:43", "foo_third@66:74", "foo_third@86:94", "u@7:7")),
                Arguments.of("wildcard hidden USING alias", "SELECT u.* FROM u, (foo_left JOIN foo_right USING (id) AS u) AS j",
                        Arrays.asList("u@16:16", "foo_left@20:27", "foo_right@34:42", "u@7:7")),
                Arguments.of("JOIN alias outside own ON scope", "SELECT (SELECT count(*) FROM ((foo_left JOIN foo_right USING (id) AS u) JOIN foo_third ON foo_third.id = j.id) AS j) FROM j",
                        Arrays.asList("j@122:122", "foo_left@31:38", "foo_right@45:53", "foo_third@77:85", "foo_third@90:98", "j@105:105")),
                Arguments.of("USING alias outside left ON scope", "SELECT * FROM foo_outer WHERE EXISTS (SELECT 1 FROM "
                        + "(foo_left JOIN bar_right ON foo_outer.id = bar_right.id) JOIN baz_right USING (id) AS foo_outer)",
                        Arrays.asList("foo_outer@14:22", "foo_left@53:60", "bar_right@67:75", "foo_outer@80:88", "bar_right@95:103", "baz_right@114:122")),
                Arguments.of("USING alias outside right LATERAL scope", "SELECT * FROM foo_outer WHERE EXISTS (SELECT 1 FROM "
                        + "foo_left JOIN LATERAL (SELECT foo_outer.id AS id) AS x USING (id) AS foo_outer)",
                        Arrays.asList("foo_outer@14:22", "foo_left@52:59", "foo_outer@82:90")),
                Arguments.of("derived alias outside own body", "SELECT * FROM foo_outer WHERE EXISTS (SELECT 1 FROM "
                        + "foo_left JOIN foo_right USING (id) AS merged, LATERAL (SELECT foo_outer.id) AS foo_outer)",
                        Arrays.asList("foo_outer@14:22", "foo_left@52:59", "foo_right@66:74", "foo_outer@114:122")))
                .flatMap(each -> Stream.of("PREPARE foo_stmt AS ", "EXPLAIN ").map(prefix -> Arguments.of(prefix + each.get()[0], prefix, each.get()[1], each.get()[2])));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUsingAliasRewriteCases")
    void assertUsingAliasRewriteDecision(final String name, final String sql, final boolean expectedRewrite) {
        SelectStatement actual = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(actual);
        ColumnSegment column = ((ColumnProjectionSegment) actual.getProjections().getProjections().iterator().next()).getColumn();
        assertThat(extractor.needRewrite(column.getOwner().get()), is(expectedRewrite));
    }
    
    private static Stream<Arguments> getUsingAliasRewriteCases() {
        return Stream.of(
                Arguments.of("distinct quoted alias", "SELECT foo_outer.id FROM foo_outer, foo_left JOIN foo_right USING (id) AS \"FOO_OUTER\"", true),
                Arguments.of("quoted alias match", "SELECT \"FOO_OUTER\".id FROM foo_left JOIN foo_right USING (id) AS \"FOO_OUTER\"", false),
                Arguments.of("unquoted alias folding", "SELECT FOO_OUTER.id FROM foo_left JOIN foo_right USING (id) AS foo_outer", false),
                Arguments.of("quoted lowercase alias", "SELECT foo_outer.id FROM foo_left JOIN foo_right USING (id) AS \"foo_outer\"", false),
                Arguments.of("regular JOIN alias hiding", "SELECT u.id FROM u, (foo_left JOIN foo_right USING (id) AS u) AS j", true),
                Arguments.of("enclosing JOIN alias hiding", "SELECT u.id FROM u, (foo_left JOIN foo_right USING (id) AS u JOIN foo_third ON u.id = foo_third.id) AS j", true));
    }
    
    @Test
    void assertUsingAliasAfterUnscopedStatement() {
        TableExtractor extractor = new TableExtractor();
        for (String each : Arrays.asList("SELECT j.id FROM (foo_left JOIN foo_right USING (id) AS u) AS j", "SELECT u.id FROM u")) {
            SelectStatement selectStatement = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(each, false));
            extractor.extractTablesFromSelect(selectStatement);
        }
        assertThat(extractor.getRewriteTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()), contains("foo_left", "foo_right", "u", "u"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getMergeTableAttributeCases")
    void assertMergeTableAttributes(final String name, final String sql, final Collection<String> expectedTables) {
        SQLStatement actual = new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        Collection<String> actualTables = actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue()).distinct().sorted().collect(Collectors.toList());
        assertThat(actualTables, is(expectedTables));
    }
    
    private static Stream<Arguments> getMergeTableAttributeCases() {
        String merge = "MERGE INTO foo_target USING foo_source ON foo_target.id = foo_source.id ";
        Collection<String> expectedTables = Arrays.asList("foo_nested", "foo_source", "foo_target");
        return Stream.of(
                Arguments.of("when predicate", merge + "WHEN MATCHED AND EXISTS (SELECT 1 FROM foo_nested) THEN DO NOTHING", expectedTables),
                Arguments.of("update assignment", merge + "WHEN MATCHED THEN UPDATE SET id = (SELECT MAX(id) FROM foo_nested)", expectedTables),
                Arguments.of("insert values", merge + "WHEN NOT MATCHED THEN INSERT (id) VALUES ((SELECT MAX(id) FROM foo_nested))", expectedTables),
                Arguments.of("returning projection", merge + "WHEN MATCHED THEN DO NOTHING RETURNING (SELECT MAX(id) FROM foo_nested)", expectedTables),
                Arguments.of("default returning row aliases", merge + "WHEN MATCHED THEN DO NOTHING RETURNING old.id, new.id, (SELECT MAX(id) FROM foo_nested)", expectedTables),
                Arguments.of("explicit returning row aliases", merge
                        + "WHEN MATCHED THEN DO NOTHING RETURNING WITH (OLD AS old_row, NEW AS new_row) old_row.id, new_row.id, (SELECT MAX(id) FROM foo_nested)", expectedTables),
                Arguments.of("local table shadows returning row alias", merge
                        + "WHEN MATCHED THEN DO NOTHING RETURNING WITH (OLD AS old_row) old_row.id, (SELECT MAX(old_row.id) FROM old_row)",
                        Arrays.asList("foo_source", "foo_target", "old_row")),
                Arguments.of("physical tables mask default returning aliases", "MERGE INTO old USING new ON old.id = new.id WHEN MATCHED THEN DO NOTHING RETURNING old.id, new.id",
                        Arrays.asList("new", "old")),
                Arguments.of("quoted table does not mask default returning alias", "MERGE INTO \"OLD\" USING foo_source ON true WHEN MATCHED THEN DO NOTHING RETURNING old.id",
                        Arrays.asList("OLD", "foo_source")),
                Arguments.of("quoted local table does not mask returning alias", merge
                        + "WHEN MATCHED THEN DO NOTHING RETURNING old.id, (SELECT old.id FROM \"OLD\")", Arrays.asList("OLD", "foo_source", "foo_target")),
                Arguments.of("derived query inherits returning aliases", merge
                        + "WHEN MATCHED THEN DO NOTHING RETURNING (SELECT x.id FROM LATERAL (SELECT old.id) AS x)", Arrays.asList("foo_source", "foo_target")),
                Arguments.of("returning respects source using alias", "MERGE INTO foo_target USING foo_left JOIN foo_right USING (id) AS foo_outer ON foo_target.id = foo_outer.id "
                        + "WHEN MATCHED THEN DO NOTHING RETURNING foo_outer.id, old.id", Arrays.asList("foo_left", "foo_right", "foo_target")),
                Arguments.of("merge with body", "WITH seed AS (SELECT id FROM foo_nested) MERGE INTO foo_target USING seed ON foo_target.id = seed.id WHEN MATCHED THEN DO NOTHING",
                        Arrays.asList("foo_nested", "foo_target", "seed")),
                Arguments.of("merge in with body", "WITH changed AS (" + merge
                        + "WHEN MATCHED AND EXISTS (SELECT 1 FROM foo_nested) THEN DO NOTHING RETURNING foo_target.id) SELECT 1", expectedTables),
                Arguments.of("on predicate", "MERGE INTO foo_target USING foo_source ON foo_target.id = foo_source.id AND EXISTS (SELECT 1 FROM foo_nested) WHEN MATCHED THEN DO NOTHING",
                        expectedTables),
                Arguments.of("correlated aliases", "MERGE INTO foo_target AS foo_t USING foo_source AS foo_s ON foo_t.id = foo_s.id "
                        + "WHEN MATCHED AND EXISTS (SELECT 1 FROM foo_nested WHERE foo_nested.id = foo_t.id) THEN UPDATE SET id = foo_s.id", expectedTables))
                .flatMap(each -> Stream.of("PREPARE foo_stmt AS ", "EXPLAIN ").map(prefix -> Arguments.of(prefix + each.get()[0], prefix + each.get()[1], each.get()[2])));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUnsupportedAssignmentTargets")
    void assertUnsupportedAssignmentTarget(final String target) {
        String sql = "UPDATE foo_table SET " + target + " = ?";
        assertThrows(SQLParsingException.class, () -> new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false)));
    }
    
    private static Stream<String> getUnsupportedAssignmentTargets() {
        return Stream.of(
                "foo_column.*",
                "foo_column . *",
                "foo_column.foo_field.*",
                "foo_column[?].*",
                "foo_column.*[?]",
                "foo_column[?:?].foo_field.*");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getArrayAssignmentTargetCases")
    void assertArrayAssignmentTarget(final String target, final Collection<String> expectedFields, final Collection<Integer> expectedParams) {
        String sql = "UPDATE foo_table SET " + target + " = ? WHERE foo_id = ?";
        UpdateStatement actual = (UpdateStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        ColumnAssignmentSegment actualAssignment = actual.getSetAssignment().getAssignments().iterator().next();
        ColumnSegment actualColumn = actualAssignment.getColumns().get(0);
        assertThat(actualColumn.getIdentifier().getValue(), is("foo_column"));
        if (expectedFields.isEmpty()) {
            assertNull(actualColumn.getNestedObjectAttributes());
        } else {
            assertThat(actualColumn.getNestedObjectAttributes().stream().map(IdentifierValue::getValue).collect(Collectors.toList()), is(expectedFields));
        }
        assertThat(actualColumn.getStartIndex(), is(sql.indexOf(target)));
        assertThat(actualColumn.getStopIndex(), is(sql.indexOf(target) + target.length() - 1));
        ParameterMarkerExpressionSegment actualValue = (ParameterMarkerExpressionSegment) actualAssignment.getValue();
        assertThat(actualValue.getParameterMarkerIndex(), is(expectedParams.size() - 2));
        assertThat(actualValue.getStartIndex(), is(sql.indexOf(" = ?") + 3));
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), is(expectedParams));
    }
    
    private static Stream<Arguments> getArrayAssignmentTargetCases() {
        return Stream.of(
                Arguments.of("foo_column[1]", Collections.emptyList(), Arrays.asList(0, 1)),
                Arguments.of("foo_column[?]", Collections.emptyList(), Arrays.asList(0, 1, 2)),
                Arguments.of("foo_column[1 * 2]", Collections.emptyList(), Arrays.asList(0, 1)),
                Arguments.of("foo_column[?].bar_field", Collections.singletonList("bar_field"), Arrays.asList(0, 1, 2)),
                Arguments.of("foo_column.bar_field[?]", Collections.singletonList("bar_field"), Arrays.asList(0, 1, 2)),
                Arguments.of("foo_column[?:?].bar_field", Collections.singletonList("bar_field"), Arrays.asList(0, 1, 2, 3)),
                Arguments.of("foo_column[:]", Collections.emptyList(), Arrays.asList(0, 1)),
                Arguments.of("foo_column[:?]", Collections.emptyList(), Arrays.asList(0, 1, 2)),
                Arguments.of("foo_column[?:]", Collections.emptyList(), Arrays.asList(0, 1, 2)),
                Arguments.of("foo_column[?][?]", Collections.emptyList(), Arrays.asList(0, 1, 2, 3)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getFieldAssignmentTargetCases")
    void assertFieldAssignmentTarget(final String name, final String target, final String expectedColumn, final Collection<String> expectedFields) {
        String sql = "UPDATE foo_table SET " + target + " = ? WHERE foo_id = ?";
        UpdateStatement actual = (UpdateStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        ColumnAssignmentSegment actualAssignment = actual.getSetAssignment().getAssignments().iterator().next();
        ColumnSegment actualColumn = actualAssignment.getColumns().get(0);
        assertThat(actualColumn.getIdentifier().getValueWithQuoteCharacters(), is(expectedColumn));
        assertThat(actualColumn.getNestedObjectAttributes().stream().map(IdentifierValue::getValueWithQuoteCharacters).collect(Collectors.toList()), is(expectedFields));
        assertThat(actualColumn.getQualifiedName(), is(String.join(".", expectedColumn, String.join(".", expectedFields))));
        assertThat(actualColumn.getStartIndex(), is(sql.indexOf(target)));
        assertThat(actualColumn.getStopIndex(), is(sql.indexOf(target) + target.length() - 1));
        assertThat(sql.substring(actualColumn.getStartIndex(), actualColumn.getStopIndex() + 1), is(target));
        ParameterMarkerExpressionSegment actualValue = (ParameterMarkerExpressionSegment) actualAssignment.getValue();
        assertThat(actualValue.getParameterMarkerIndex(), is(0));
        assertThat(actualValue.getStartIndex(), is(sql.indexOf('?')));
        assertThat(actualValue.getStopIndex(), is(sql.indexOf('?')));
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), contains(0, 1));
    }
    
    private static Stream<Arguments> getFieldAssignmentTargetCases() {
        return Stream.of(
                Arguments.of("single field", "foo_column.bar_field", "foo_column", Collections.singletonList("bar_field")),
                Arguments.of("field chain", "foo_column.bar_field.baz_field", "foo_column", Arrays.asList("bar_field", "baz_field")),
                Arguments.of("quoted field", "\"foo.column\".\"bar.field\"", "\"foo.column\"", Collections.singletonList("\"bar.field\"")),
                Arguments.of("quoted star field", "foo_column.\"*\"", "foo_column", Collections.singletonList("\"*\"")),
                Arguments.of("field source spacing", "foo_column . bar_field . baz_field", "foo_column", Arrays.asList("bar_field", "baz_field")));
    }
    
    @Test
    void assertTupleFieldAssignmentTargets() {
        String sql = "UPDATE foo_table SET (foo_column.bar_field, bar_column.baz_field) = ROW(?, ?) WHERE foo_id = ?";
        UpdateStatement actual = (UpdateStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        ColumnAssignmentSegment actualAssignment = actual.getSetAssignment().getAssignments().iterator().next();
        assertThat(actualAssignment.getColumns().stream().map(ColumnSegment::getQualifiedName).collect(Collectors.toList()), contains("foo_column.bar_field", "bar_column.baz_field"));
        for (ColumnSegment each : actualAssignment.getColumns()) {
            assertThat(sql.substring(each.getStartIndex(), each.getStopIndex() + 1), is(each.getQualifiedName()));
        }
        assertThat(ExpressionExtractor.getParameterMarkerExpressions(Collections.singleton(actualAssignment.getValue())).stream()
                .map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), contains(0, 1));
        assertThat(actual.getParameterMarkers().stream().map(ParameterMarkerSegment::getParameterIndex).collect(Collectors.toList()), contains(0, 1, 2));
    }
    
    @Test
    void assertPlainAssignmentTarget() {
        String sql = "UPDATE foo_table SET foo_column = ?";
        UpdateStatement actual = (UpdateStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        ColumnSegment actualColumn = actual.getSetAssignment().getAssignments().iterator().next().getColumns().get(0);
        assertThat(actualColumn.getIdentifier().getValue(), is("foo_column"));
        assertNull(actualColumn.getNestedObjectAttributes());
        assertThat(sql.substring(actualColumn.getStartIndex(), actualColumn.getStopIndex() + 1), is("foo_column"));
        assertThat(actual.getParameterCount(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCursorCountCases")
    void assertFetchCursorCount(final String name, final String count, final long expectedCount) {
        String sql = "FETCH FORWARD " + count + " FROM c";
        FetchStatement actual = (FetchStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        assertThat(actual.getDirection().get().getCount().get(), is(expectedCount));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getCursorCountCases")
    void assertMoveCursorCount(final String name, final String count, final long expectedCount) {
        String sql = "MOVE BACKWARD " + count + " FROM c";
        MoveStatement actual = (MoveStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false));
        assertThat(actual.getDirection().get().getCount().get(), is(expectedCount));
    }
    
    private static Stream<Arguments> getCursorCountCases() {
        return Stream.of(Arguments.of("Decimal count", "10", 10L), Arguments.of("Grouped count", "1_0", 10L), Arguments.of("Negative grouped count", "-1_0", -10L));
    }
}
