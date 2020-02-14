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

package org.apache.shardingsphere.sql.parser.integrate.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.ParseTreeVisitorFactory;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.SQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.loader.SQLCasesRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
@Slf4j
public final class VisitorParameterizedParsingTest {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = SQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private static final SQLParserTestCasesRegistry SQL_PARSER_TEST_CASES_REGISTRY = SQLParserTestCasesRegistryFactory.getInstance().getRegistry();
    
    private static final Properties PROPS = new Properties();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    static {
        try {
            PROPS.load(new FileInputStream(SQLParserParameterizedTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/runtime-config.properties"));
        } catch (final IOException ex) {
            log.warn("Can not find file `runtime-config.properties`, use default properties configuration.", ex);
        }
    }
    
    @Before
    public void setUp() {
        NewInstanceServiceLoader.register(SQLParserEntry.class);
    }
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        // TODO resume me after all test cases passed
        //        checkTestCases();
        // TODO remove me after all test cases passed
        return getSQLTestParameters(SQL_CASES_LOADER.getSQLTestParameters());
        // TODO resume me after all test cases passed 
//        return SQL_CASES_LOADER.getSQLTestParameters();
    }
    
    // TODO remove me after all test cases passed
    private static Collection<Object[]> getSQLTestParameters(final Collection<Object[]> sqlTestParameters) {
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlTestParameters) {
            String sqlCaseId = each[0].toString();
            String databaseType = each[1].toString();
            SQLCaseType sqlCaseType = (SQLCaseType) each[2];
            if (isPassedSqlCase(sqlCaseId)) {
                continue;
            }
            if (!"MySQL".contains(databaseType) && !"PostgreSQL".contains(databaseType) && !"Oracle".contains(databaseType)) {
//            if (!"MySQL".contains(databaseType) && !"PostgreSQL".contains(databaseType)) {
                continue;
            }
            try {
                SQL_CASES_LOADER.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId).getParameters());
            } catch (final IllegalStateException ex) {
                continue;
            }
            result.add(each);
        }
        return result;
    }
    
    private static void checkTestCases() {
        Collection<String> allSQLCaseIDs = new HashSet<>(SQL_CASES_LOADER.getAllSQLCaseIDs());
        if (allSQLCaseIDs.size() != SQL_PARSER_TEST_CASES_REGISTRY.getAllSQLCaseIDs().size()) {
            allSQLCaseIDs.removeAll(SQL_PARSER_TEST_CASES_REGISTRY.getAllSQLCaseIDs());
            fail(String.format("The count of SQL cases and SQL parser cases are mismatched, missing cases are: %s", allSQLCaseIDs));
        }
    }
    
    private static boolean isPassedSqlCase(final String sqlCaseId) {
        // On the one hand, visitor parser can generate correct parsing results for them, on the other hand old parser can not.
        Collection<String> sqlCases = new LinkedList<>();
        sqlCases.add("insert_on_duplicate_key_update_with_base64_aes_encrypt");
        sqlCases.add("insert_with_one_auto_increment_column");
        sqlCases.add("insert_without_columns_and_with_generate_key_column");
        sqlCases.add("insert_without_columns_and_without_generate_key_column");
        sqlCases.add("insert_without_columns_with_all_placeholders");
        sqlCases.add("show_index_with_indexes_with_table_and_database");
        sqlCases.add("show_index_with_database_back_quotes");
        sqlCases.add("show_index_with_table_back_quotes");
        // TODO cannot support insert all
        sqlCases.add("insert_all_with_all_placeholders");
        // TODO cannot parse create index behind pk in create table statement
        sqlCases.add("create_table_with_create_index");
        // TODO cannot parse using index behind pk in create table statement
        sqlCases.add("create_table_with_exist_index");
        // On the one hand, visitor parser can not give the right stop index for any sql with alias, on the other hand,  the old parser can not handle all sql cases correctly.
        sqlCases.add("select_with_expression");
        sqlCases.add("select_pagination_with_row_number");
        sqlCases.add("select_pagination_with_row_number_and_diff_group_by_and_order_by");
        sqlCases.add("select_pagination_with_row_number_and_diff_group_by_and_order_by");
        sqlCases.add("select_pagination_with_row_number_and_group_by_and_order_by");
        sqlCases.add("select_pagination_with_row_number_for_greater_than");
        sqlCases.add("select_pagination_with_row_number_for_greater_than_and_equal");
        return sqlCases.contains(sqlCaseId);
    }
    
    @Test
    public void assertSupportedSQL() {
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId);
        if (expected.isLongSQL() && Boolean.parseBoolean(PROPS.getProperty("long.sql.skip", Boolean.TRUE.toString()))) {
            return;
        }
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        String sql = SQL_CASES_LOADER.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId).getParameters());
        ParseTree parseTree = SQLParserFactory.newInstance(databaseType, sql).execute().getChild(0);
        SQLStatement actual = (SQLStatement) ParseTreeVisitorFactory.newInstance(databaseType, parseTree).visit(parseTree);
        if (!expected.isLongSQL()) {
            SQLStatementAssert.assertIs(new SQLCaseAssertContext(sqlCaseId, sqlCaseType), actual, expected);
        }
    }
}
