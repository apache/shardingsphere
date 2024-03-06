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

package org.apache.shardingsphere.test.it.sql.parser.internal;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.distsql.parser.engine.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.SQLParserTestCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class InternalSQLParserIT {
    
    private static final SQLCases SQL_CASES = SQLCasesRegistry.getInstance().getCases();
    
    private static final SQLParserTestCases SQL_PARSER_TEST_CASES = SQLParserTestCasesRegistry.getInstance().getCases();
    
    // TODO fix these sql parser cases after add eof in OracleStatement.g4
    // CHECKSTYLE:OFF
    private static final Collection<String> IGNORE_TEST_CASES = new HashSet<>(Arrays.asList(
            "create_function_call_spec_java", "create_cluster_number_size_hashkeys", "create_cluster_set_size", "create_cluster_size_initial_next", "create_external_role",
            "create_external_user", "create_control_file", "create_global_role", "create_global_user", "create_java", "create_materialized_view_log_with_including_new",
            "create_materialized_view_log_with_pctfree_storage_purge_repeat", "create_materialized_view_log_with_row_id_sequence_including_new", "create_materialized_view_log_with_tablespace",
            "create_no_identified_role", "create_role", "create_role_identified_by", "create_role_with_container", "create_role_with_identified_by_password",
            "create_table_with_out_of_line_constraints_oracle", "create_table_with_xmltype_column_clob_oracle", "create_table_with_xmltype_column_oracle", "create_tablespace_with_blocksize",
            "create_tablespace_with_temporary_tablespace_group", "create_tablespace_with_temporary_tempfile_spec_extent_management", "create_tablespace_with_undo_tablespace_spec"));
    // CHECKSTYLE:ON
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertSupportedSQL(final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        if (IGNORE_TEST_CASES.contains(sqlCaseId)) {
            return;
        }
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES.get(sqlCaseId).getParameters());
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES.get(sqlCaseId);
        SQLStatement actual = parseSQLStatement("H2".equals(databaseType) ? "MySQL" : databaseType, sql);
        SQLStatementAssert.assertIs(new SQLCaseAssertContext(sqlCaseId, sql, expected.getParameters(), sqlCaseType), actual, expected);
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        return "ShardingSphere".equals(databaseType)
                ? new DistSQLStatementParserEngine().parse(sql)
                : new SQLStatementVisitorEngine(databaseType).visit(new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            InternalSQLParserITSettings settings = extensionContext.getRequiredTestClass().getAnnotation(InternalSQLParserITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation InternalSQLParserITSettings is required.");
            return getTestParameters(settings.value()).stream();
        }
        
        private Collection<Arguments> getTestParameters(final String... databaseTypes) {
            Collection<Arguments> result = new LinkedList<>();
            for (InternalSQLParserTestParameter each : SQL_CASES.generateTestParameters(Arrays.stream(databaseTypes).collect(Collectors.toSet()))) {
                if (!isPlaceholderWithoutParameter(each)) {
                    result.add(Arguments.arguments(each.getSqlCaseId(), each.getSqlCaseType(), each.getDatabaseType()));
                }
            }
            return result;
        }
        
        private boolean isPlaceholderWithoutParameter(final InternalSQLParserTestParameter testParam) {
            return SQLCaseType.PLACEHOLDER == testParam.getSqlCaseType() && SQL_PARSER_TEST_CASES.get(testParam.getSqlCaseId()).getParameters().isEmpty();
        }
    }
}
