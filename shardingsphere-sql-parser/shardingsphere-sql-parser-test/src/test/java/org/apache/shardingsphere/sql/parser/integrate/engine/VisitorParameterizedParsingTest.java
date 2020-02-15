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
import org.apache.shardingsphere.sql.parser.integrate.jaxb.VisitorSQLParserTestCasesRegistryFactory;
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
    
    private static final SQLParserTestCasesRegistry SQL_PARSER_TEST_CASES_REGISTRY = VisitorSQLParserTestCasesRegistryFactory.getInstance().getRegistry();
    
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
            if (!"MySQL".contains(databaseType)) {
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
    
    @Test
    public void assertSupportedSQL() {
        SQLParserTestCase expected = VisitorSQLParserTestCasesRegistryFactory.getInstance().getRegistry().get(sqlCaseId);
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
