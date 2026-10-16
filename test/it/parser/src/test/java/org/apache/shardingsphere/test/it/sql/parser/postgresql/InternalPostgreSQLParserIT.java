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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
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
