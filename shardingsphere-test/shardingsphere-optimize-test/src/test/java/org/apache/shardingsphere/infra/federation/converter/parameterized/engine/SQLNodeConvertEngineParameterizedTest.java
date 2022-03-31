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

package org.apache.shardingsphere.infra.federation.converter.parameterized.engine;

import lombok.RequiredArgsConstructor;
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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.dialect.OptimizerSQLDialectBuilderFactory;
import org.apache.shardingsphere.infra.federation.optimizer.converter.SQLNodeConverterEngine;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.CasesRegistry;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.SQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.loader.SQLCasesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class SQLNodeConvertEngineParameterizedTest {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = CasesRegistry.getInstance().getSqlCasesLoader();
    
    private static final SQLParserTestCasesRegistry SQL_PARSER_TEST_CASES_REGISTRY = SQLParserTestCasesRegistryFactory.getInstance().getRegistry();
    
    private static final String SELECT_STATEMENT_PREFIX = "SELECT";
    
    private static final Set<String> SUPPORTED_SQL_CASE_IDS = new HashSet<>();
    
    static {
        SUPPORTED_SQL_CASE_IDS.add("select_with_join_table_subquery");
        SUPPORTED_SQL_CASE_IDS.add("select_with_projection_subquery");
        SUPPORTED_SQL_CASE_IDS.add("select_with_in_subquery_condition");
        SUPPORTED_SQL_CASE_IDS.add("select_with_between_and_subquery_condition");
        SUPPORTED_SQL_CASE_IDS.add("select_with_exist_subquery_condition");
        SUPPORTED_SQL_CASE_IDS.add("select_with_not_exist_subquery_condition");
        SUPPORTED_SQL_CASE_IDS.add("select_with_simple_table");
        SUPPORTED_SQL_CASE_IDS.add("select_pagination_with_limit_offset_and_row_count");
        SUPPORTED_SQL_CASE_IDS.add("select_pagination_with_limit_row_count");
        SUPPORTED_SQL_CASE_IDS.add("select_group_by_with_limit");
        SUPPORTED_SQL_CASE_IDS.add("select_left_outer_join_related_with_alias");
        SUPPORTED_SQL_CASE_IDS.add("select_right_outer_join_related_with_alias");
        SUPPORTED_SQL_CASE_IDS.add("select_alias_as_keyword");
        SUPPORTED_SQL_CASE_IDS.add("select_avg");
        SUPPORTED_SQL_CASE_IDS.add("select_between_with_single_table");
        SUPPORTED_SQL_CASE_IDS.add("select_distinct_with_single_count_group_by");
        SUPPORTED_SQL_CASE_IDS.add("select_bit_xor");
        SUPPORTED_SQL_CASE_IDS.add("select_position");
        SUPPORTED_SQL_CASE_IDS.add("select_constant_without_table");
        SUPPORTED_SQL_CASE_IDS.add("select_with_schema");
        SUPPORTED_SQL_CASE_IDS.add("select_with_union");
        SUPPORTED_SQL_CASE_IDS.add("select_cast_function");
        SUPPORTED_SQL_CASE_IDS.add("select_with_same_table_name_and_alias");
        SUPPORTED_SQL_CASE_IDS.add("select_count_like_concat");
    }
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return getTestParameters("MySQL");
    }
    
    private static Collection<Object[]> getTestParameters(final String... databaseTypes) {
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : SQL_CASES_LOADER.getTestParameters(Arrays.asList(databaseTypes))) {
            if (!isPlaceholderWithoutParameter(each) && isSupportedSQLCase(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static boolean isPlaceholderWithoutParameter(final Object[] sqlTestParameter) {
        return SQLCaseType.Placeholder == sqlTestParameter[2] && SQL_PARSER_TEST_CASES_REGISTRY.get(sqlTestParameter[0].toString()).getParameters().isEmpty();
    }
    
    private static boolean isSupportedSQLCase(final Object[] sqlTestParameter) {
        String sqlCaseId = sqlTestParameter[0].toString();
        return sqlCaseId.toUpperCase().startsWith(SELECT_STATEMENT_PREFIX) && SUPPORTED_SQL_CASE_IDS.contains(sqlCaseId);
    }
    
    @Test
    public void assertConvertToSQLNode() {
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        String sql = SQL_CASES_LOADER.getCaseValue(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId).getParameters(), databaseType);
        SQLStatement sqlStatement = parseSQLStatement(databaseType, sql);
        SqlNode actual = SQLNodeConverterEngine.convertToSQLNode(sqlStatement);
        SqlNode expected = parseSqlNode(databaseType, sql);
        assertTrue(actual.equalsDeep(expected, Litmus.THROW));
    }
    
    @Test
    public void assertConvertToSQLStatement() {
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId);
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        String sql = SQL_CASES_LOADER.getCaseValue(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId).getParameters(), databaseType);
        SqlNode sqlNode = parseSqlNode(databaseType, sql);
        SQLStatement actual = SQLNodeConverterEngine.convertToSQLStatement(sqlNode);
        SQLStatementAssert.assertIs(new SQLCaseAssertContext(SQL_CASES_LOADER, sqlCaseId, sqlCaseType, databaseType), actual, expected);
    }
    
    @SneakyThrows(SqlParseException.class)
    private SqlNode parseSqlNode(final String databaseType, final String sql) {
        return SqlParser.create(sql, createConfig(DatabaseTypeRegistry.getActualDatabaseType(databaseType))).parseQuery();
    }
    
    private Config createConfig(final DatabaseType databaseType) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(createSQLDialectProperties(databaseType));
        return SqlParser.config().withLex(connectionConfig.lex()).withUnquotedCasing(Casing.UNCHANGED)
                .withIdentifierMaxLength(SqlParser.DEFAULT_IDENTIFIER_MAX_LENGTH).withConformance(connectionConfig.conformance()).withParserFactory(SqlParserImpl.FACTORY);
    }
    
    private Properties createSQLDialectProperties(final DatabaseType databaseType) {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        result.putAll(OptimizerSQLDialectBuilderFactory.build(databaseType, result));
        return result;
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        CacheOption cacheOption = new CacheOption(128, 1024L, 4);
        return new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(new SQLParserEngine(databaseType, cacheOption).parse(sql, false));
    }
}
