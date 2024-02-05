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
    private static final Collection<String> IGNORE_TEST_CASES = new HashSet<>(Arrays.asList("alter_diskgroup_verify", "alter_global_user", "alter_materialized_view_refresh_complete_refresh",
            "alter_external_user", "alter_audit_policy_modify", "alter_database_open_readonly", "alter_table_add_primary_foreign_key", "alter_table_move_compress_for_oltp",
            "alter_type_add_static_procedure", "alter_type_add_static_procedure_is", "alter_type_drop_static_procedure", "alter_type_drop_static_procedure_is", "alter_user_account",
            "alter_user_default_role", "alter_user_expire_with_options", "create_function_call_spec_java", "alter_user_grant_proxy", "alter_user_grant_proxy_with_option",
            "alter_user_identified_without_hostname", "alter_user_password_with_lock_option", "alter_user_revoke_proxy", "alter_user_with_password", "alter_user_with_quota_option",
            "alter_user_with_tablespace_option", "create_cluster_number_size_hashkeys", "alter_user_proxys", "create_cluster_set_size", "create_cluster_size_initial_next", "create_external_role",
            "alter_user_with_container", "create_external_user", "create_control_file", "create_global_role", "create_global_user", "create_java", "create_materialized_view_log_with_including_new",
            "create_materialized_view_log_with_pctfree_storage_purge_repeat", "create_materialized_view_log_with_row_id_sequence_including_new", "create_materialized_view_log_with_tablespace",
            "create_no_identified_role", "create_role", "create_role_identified_by", "create_role_with_container", "create_role_with_identified_by_password",
            "create_table_with_out_of_line_constraints_oracle", "create_table_with_xmltype_column_clob_oracle", "create_table_with_xmltype_column_oracle", "create_tablespace_with_blocksize",
            "create_tablespace_with_temporary_tablespace_group", "create_tablespace_with_temporary_tempfile_spec_extent_management", "create_tablespace_with_undo_tablespace_spec",
            "create_user_identified_by_without_hostname", "create_user_with_lock_option", "create_user_with_password", "create_user_with_password_expire_lock", "create_user_with_password_option",
            "create_user_with_quota_option", "create_user_with_tablespace", "drop_database_including_backups_noprompt", "drop_index_with_quota", "drop_user_with_hostname", "drop_user_with_ip",
            "grant_all_object_privileges", "grant_all_system_privileges", "grant_object_privilege", "grant_object_privilege_to_users", "grant_object_privilege_column", "grant_object_privileges",
            "grant_program", "grant_role", "grant_roles_to_programs", "grant_roles_to_users", "grant_system_privilege", "grant_system_privilege_to_users", "grant_system_privileges",
            "grant_user_with_admin", "grant_user_with_grant", "grant_user_without_hostname", "revoke_all_system_privileges", "revoke_all_object_privileges", "revoke_object_privilege",
            "revoke_object_privilege_column", "revoke_object_privilege_from_users", "revoke_object_privileges", "revoke_program", "revoke_role", "revoke_role_from_user", "revoke_roles_from_programs",
            "revoke_system_privilege_from_users", "revoke_system_privilege", "revoke_system_privileges", "revoke_user_from", "revoke_user_without_hostname", "select_with_expressions_in_projection",
            "select_with_model_in", "set_all_expect_roles", "set_all_expect_role"));
    // CHECKSTYLE:ON
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertSupportedSQL(final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        if (IGNORE_TEST_CASES.contains(sqlCaseId)) {
            return;
        }
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES.get(sqlCaseId).getParameters());
        SQLStatement actual = parseSQLStatement("H2".equals(databaseType) ? "MySQL" : databaseType, sql);
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES.get(sqlCaseId);
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
