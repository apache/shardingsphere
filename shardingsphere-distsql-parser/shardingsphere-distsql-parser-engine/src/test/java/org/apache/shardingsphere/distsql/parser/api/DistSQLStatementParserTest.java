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

package org.apache.shardingsphere.distsql.parser.api;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.SQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.distsql.parser.api.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.SQLStatementAssert;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.sql.loader.SQLCasesRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.fail;

@AllArgsConstructor
@RunWith(Parameterized.class)
public final class DistSQLStatementParserTest {

    private static final SQLCasesLoader SQL_CASES_LOADER = SQLCasesRegistry.getInstance().getSqlCasesLoader();

    private static final SQLParserTestCasesRegistry SQL_PARSER_TEST_CASES_REGISTRY = SQLParserTestCasesRegistryFactory.getInstance().getRegistry();

    private final String sqlCaseId;

    private static void checkTestCases() {
        Collection<String> allSQLCaseIDs = new HashSet<>(SQL_CASES_LOADER.getAllSQLCaseIDs());
        if (allSQLCaseIDs.size() != SQL_PARSER_TEST_CASES_REGISTRY.getAllSQLCaseIDs().size()) {
            allSQLCaseIDs.removeAll(SQL_PARSER_TEST_CASES_REGISTRY.getAllSQLCaseIDs());
            fail(String.format("The count of SQL cases and SQL parser cases are mismatched, missing cases are: %s", allSQLCaseIDs));
        }
    }

    @Test
    public void assertDistSQL() {
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId);
        String sql = SQL_CASES_LOADER.getSQL(sqlCaseId);
        SQLStatement actual = parseSQLStatement(sql);
        SQLStatementAssert.assertIs(new SQLCaseAssertContext(sqlCaseId), actual, expected);
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        checkTestCases();
        return SQL_CASES_LOADER.getSQLTestParameters();
    }

    private SQLStatement parseSQLStatement(final String sql) {
        return new DistSQLStatementParserEngine().parse(sql);
    }
}
