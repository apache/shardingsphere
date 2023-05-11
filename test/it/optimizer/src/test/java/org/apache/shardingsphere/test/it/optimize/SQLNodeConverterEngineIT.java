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

package org.apache.shardingsphere.test.it.optimize;

import lombok.SneakyThrows;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.util.Litmus;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.dialect.OptimizerSQLDialectBuilder;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.SQLNodeConverterEngine;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.SQLParserTestCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.registry.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.SQLCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.registry.SQLCasesRegistry;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLNodeConverterEngineIT {
    
    private static final SQLCases SQL_CASES = SQLCasesRegistry.getInstance().getCases();
    
    private static final SQLParserTestCases SQL_PARSER_TEST_CASES = SQLParserTestCasesRegistry.getInstance().getCases();
    
    private static final String SELECT_STATEMENT_PREFIX = "SELECT";
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertConvert(final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES.get(sqlCaseId).getParameters());
        SqlNode actual = SQLNodeConverterEngine.convert(parseSQLStatement(databaseType, sql));
        SqlNode expected = parseSQLNode(databaseType, sql);
        assertTrue(actual.equalsDeep(expected, Litmus.THROW));
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        return new SQLStatementVisitorEngine(databaseType, true).visit(new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false));
    }
    
    @SneakyThrows(SqlParseException.class)
    private SqlNode parseSQLNode(final String databaseType, final String sql) {
        return SqlParser.create(sql, createConfiguration(databaseType)).parseQuery();
    }
    
    private Config createConfiguration(final String databaseType) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(createSQLDialectProperties(databaseType));
        return SqlParser.config().withLex(connectionConfig.lex()).withUnquotedCasing(Casing.UNCHANGED)
                .withIdentifierMaxLength(SqlParser.DEFAULT_IDENTIFIER_MAX_LENGTH).withConformance(connectionConfig.conformance()).withParserFactory(SqlParserImpl.FACTORY);
    }
    
    private Properties createSQLDialectProperties(final String databaseType) {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        result.putAll(TypedSPILoader.getService(OptimizerSQLDialectBuilder.class, databaseType).build());
        return result;
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        private final Collection<String> supportedSQLCaseIDs = getSupportedSQLCaseIDs();
        
        // TODO remove the method when all SQL statement support convert to SQL node
        // CHECKSTYLE:OFF
        private Collection<String> getSupportedSQLCaseIDs() {
            Collection<String> result = new HashSet<>();
            result.add("select_with_join_table_subquery");
            result.add("select_with_projection_subquery");
            result.add("select_with_in_subquery_condition");
            result.add("select_with_between_and_subquery_condition");
            result.add("select_with_exist_subquery_condition");
            result.add("select_with_not_exist_subquery_condition");
            result.add("select_with_simple_table");
            result.add("select_group_by_with_limit");
            result.add("select_left_outer_join_related_with_alias");
            result.add("select_right_outer_join_related_with_alias");
            result.add("select_alias_as_keyword");
            result.add("select_avg");
            result.add("select_between_with_single_table");
            result.add("select_distinct_with_single_count_group_by");
            result.add("select_bit_xor");
            result.add("select_with_schema");
            result.add("select_with_same_table_name_and_alias");
            result.add("select_count_like_concat");
            result.add("select_order_by_asc_and_index_desc");
            result.add("select_group_by_with_having_count");
            result.add("select_constant_without_table");
            result.add("select_count_with_binding_tables_with_join");
            result.add("select_join_using");
            result.add("select_count_with_escape_character");
            result.add("select_group_by_with_order_by_and_limit");
            result.add("select_count_with_sub");
            result.add("select_current_user");
            result.add("select_database");
            result.add("select_distinct_with_count_calculation");
            result.add("select_count_like_escape");
            result.add("select_with_projection_subquery_and_multiple_parameters");
            result.add("select_group_concat");
            result.add("select_cast_function");
            result.add("select_position");
            result.add("select_mod_function");
            result.add("select_pagination_with_offset");
            result.add("select_pagination_with_row_count");
            result.add("select_pagination_with_top");
            result.add("select_pagination_with_top_percent_with_ties");
            result.add("select_pagination_with_row_number");
            result.add("select_pagination_with_limit_with_back_quotes");
            result.add("select_pagination_with_limit_and_offset_keyword");
            result.add("select_pagination_with_offset_and_limit");
            result.add("select_pagination_with_offset_and_limit_all");
            result.add("select_pagination_with_top_for_greater_than");
            result.add("select_pagination_with_top_percent_with_ties_for_greater_than");
            result.add("select_pagination_with_top_for_greater_than_and_equal");
            result.add("select_pagination_with_top_percent_with_ties_for_greater_than_and_equal");
            result.add("select_pagination_with_row_number_for_greater_than");
            result.add("select_pagination_with_row_number_for_greater_than_and_equal");
            result.add("select_pagination_with_row_number_not_at_end");
            result.add("select_pagination_with_fetch_first_with_row_number");
            result.add("select_pagination_with_offset_fetch");
            result.add("select_pagination_with_limit_offset_and_row_count");
            result.add("select_pagination_with_limit_row_count");
            result.add("select_pagination_with_limit_fetch_count");
            result.add("select_with_null_keyword_in_projection");
            result.add("select_union");
            result.add("select_union_all");
            result.add("select_union_all_order_by");
            result.add("select_union_all_order_by_limit");
            result.add("select_intersect");
            result.add("select_intersect_order_by");
            result.add("select_intersect_order_by_limit");
            result.add("select_except");
            result.add("select_except_order_by");
            result.add("select_except_order_by_limit");
            result.add("select_minus");
            result.add("select_minus_order_by");
            result.add("select_minus_order_by_limit");
            result.add("select_union_intersect");
            result.add("select_union_except");
            result.add("select_union_intersect_except");
            result.add("select_except_union");
            result.add("select_except_intersect");
            result.add("select_except_intersect_union");
            result.add("select_sub_union");
            result.add("select_projections_with_expr");
            result.add("select_projections_with_only_expr");
            result.add("select_natural_join");
            result.add("select_natural_inner_join");
            result.add("select_natural_left_join");
            result.add("select_natural_right_join");
            result.add("select_natural_full_join");
            result.add("select_order_by_for_nulls_first");
            result.add("select_order_by_for_nulls_last");
            result.add("select_char");
            result.add("select_weight_string");
            result.add("select_trim");
            result.add("select_trim_with_both");
            result.add("select_with_trim_expr");
            result.add("select_with_trim_expr_and_both");
            result.add("select_with_trim_expr_from_expr");
            result.add("select_with_trim_expr_from_expr_and_both");
            result.add("select_extract");
            result.add("select_where_with_bit_expr_with_mod_sign");
            result.add("select_with_spatial_function");
            result.add("select_from_dual");
            result.add("select_substring");
            result.add("select_where_with_bit_expr_with_plus_interval");
            result.add("select_where_with_bit_expr_with_minus_interval");
            result.add("select_where_with_predicate_with_in_subquery");
            result.add("select_where_with_boolean_primary_with_is");
            result.add("select_where_with_boolean_primary_with_is_not");
            result.add("select_where_with_boolean_primary_with_comparison_subquery");
            result.add("select_not_between_with_single_table");
            result.add("select_not_in_with_single_table");
            return result;
        }
        // CHECKSTYLE:ON
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return getTestParameters("MySQL", "PostgreSQL", "openGauss").stream();
        }
        
        private Collection<Arguments> getTestParameters(final String... databaseTypes) {
            Collection<Arguments> result = new LinkedList<>();
            for (InternalSQLParserTestParameter each : SQL_CASES.generateTestParameters(Arrays.stream(databaseTypes).collect(Collectors.toSet()))) {
                if (!isPlaceholderWithoutParameter(each) && isSupportedSQLCase(each)) {
                    result.add(Arguments.of(each.getSqlCaseId(), each.getSqlCaseType(), "H2".equals(each.getDatabaseType()) ? "MySQL" : each.getDatabaseType()));
                }
            }
            return result;
        }
        
        private boolean isPlaceholderWithoutParameter(final InternalSQLParserTestParameter testParam) {
            return SQLCaseType.PLACEHOLDER == testParam.getSqlCaseType() && SQL_PARSER_TEST_CASES.get(testParam.getSqlCaseId()).getParameters().isEmpty();
        }
        
        private boolean isSupportedSQLCase(final InternalSQLParserTestParameter testParam) {
            return testParam.getSqlCaseId().toUpperCase().startsWith(SELECT_STATEMENT_PREFIX) && supportedSQLCaseIDs.contains(testParam.getSqlCaseId());
        }
    }
}
