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

package org.apache.shardingsphere.test.it.optimizer.sqlnode.converter;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.SQLNodeConverterEngine;
import org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.SQLDialectFactory;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.SQLNodeConverterTestCases;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.registry.converter.SQLNodeConverterTestCasesRegistry;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.registry.sql.SQLConverterCasesRegistry;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.SQLCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class SQLNodeConverterEngineIT {
    
    private static final SQLCases SQL_CASES = SQLConverterCasesRegistry.getInstance().getCases();
    
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
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, SQL_NODE_CONVERTER_TEST_CASES.get(sqlCaseId, sqlCaseType, databaseType).getParameters());
        String actual = SQLNodeConverterEngine.convert(parseSQLStatement(databaseType, sql)).toSqlString(SQLDialectFactory.getSQLDialect(databaseType)).getSql().replace("\n", " ").replace("\r", "");
        String expected = SQL_NODE_CONVERTER_TEST_CASES.get(sqlCaseId, sqlCaseType, databaseType).getExpectedSQL();
        assertThat(actual, is(expected));
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        return new SQLStatementVisitorEngine(databaseType).visit(new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            SQLNodeConverterEngineITSettings settings = context.getRequiredTestClass().getAnnotation(SQLNodeConverterEngineITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation SQLBinderITSettings is required.");
            return getTestParameters(settings.value()).stream();
        }
        
        private Collection<Arguments> getTestParameters(final String... databaseTypes) {
            Collection<Arguments> result = new LinkedList<>();
            for (InternalSQLParserTestParameter each : SQL_CASES.generateTestParameters(Arrays.stream(databaseTypes).collect(Collectors.toSet()))) {
                if (null != SQL_NODE_CONVERTER_TEST_CASES.get(each.getSqlCaseId(), each.getSqlCaseType(), each.getDatabaseType()) && !isPlaceholderWithoutParameter(each) && isSupportedSQLCase(each)) {
                    result.add(Arguments.of(each.getSqlCaseId(), each.getSqlCaseType(), "H2".equals(each.getDatabaseType()) ? "MySQL" : each.getDatabaseType()));
                }
            }
            return result;
        }
        
        private boolean isPlaceholderWithoutParameter(final InternalSQLParserTestParameter testParam) {
            return SQLCaseType.PLACEHOLDER == testParam.getSqlCaseType()
                    && SQL_NODE_CONVERTER_TEST_CASES.get(testParam.getSqlCaseId(), testParam.getSqlCaseType(), testParam.getDatabaseType()).getParameters().isEmpty();
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
