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

package org.apache.shardingsphere.test.it.optimizer.converter;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.SQLNodeConverterEngine;
import org.apache.shardingsphere.sqlfederation.optimizer.sql.SQLDialectFactory;
import org.apache.shardingsphere.test.it.optimizer.converter.cases.SQLNodeConverterTestCases;
import org.apache.shardingsphere.test.it.optimizer.converter.cases.SQLNodeConverterTestCasesRegistry;
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
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
class SQLNodeConverterEngineIT {
    
    private static final SQLCases SQL_CASES = SQLCasesRegistry.getInstance().getCases();
    
    private static final SQLParserTestCases SQL_PARSER_TEST_CASES = SQLParserTestCasesRegistry.getInstance().getCases();
    
    private static final SQLNodeConverterTestCases SQL_NODE_CONVERTER_TEST_CASES = SQLNodeConverterTestCasesRegistry.getInstance().getCases();
    
    private static final String SELECT_STATEMENT_PREFIX = "SELECT";
    
    private static final String DELETE_STATEMENT_PREFIX = "DELETE";
    
    private static final String EXPLAIN_STATEMENT_PREFIX = "EXPLAIN";
    
    private static final String UPDATE_STATEMENT_PREFIX = "UPDATE";
    
    private static final String INSERT_STATEMENT_PREFIX = "INSERT";
    
    private static final String MERGE_STATEMENT_PREFIX = "MERGE";
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertConvert(final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        String expected;
        try {
            expected = SQL_NODE_CONVERTER_TEST_CASES.get(sqlCaseId, sqlCaseType, databaseType).getExpectedSQL();
        } catch (final IllegalStateException ex) {
            log.warn(ex.getMessage());
            return;
        }
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES.get(sqlCaseId).getParameters());
        String actual = SQLNodeConverterEngine.convert(parseSQLStatement(databaseType, sql)).toSqlString(SQLDialectFactory.getSQLDialect(databaseType)).getSql().replace("\n", " ").replace("\r", "");
        assertThat(actual, is(expected));
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        return new SQLStatementVisitorEngine(databaseType).visit(new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return getTestParameters("MySQL", "PostgreSQL", "openGauss", "Oracle", "SQLServer").stream();
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
            return testParam.getSqlCaseId().toUpperCase().startsWith(SELECT_STATEMENT_PREFIX)
                    || testParam.getSqlCaseId().toUpperCase().startsWith(DELETE_STATEMENT_PREFIX)
                    || testParam.getSqlCaseId().toUpperCase().startsWith(EXPLAIN_STATEMENT_PREFIX)
                    || testParam.getSqlCaseId().toUpperCase().startsWith(UPDATE_STATEMENT_PREFIX)
                    || testParam.getSqlCaseId().toUpperCase().startsWith(INSERT_STATEMENT_PREFIX)
                    || testParam.getSqlCaseId().toUpperCase().startsWith(MERGE_STATEMENT_PREFIX);
        }
    }
}
