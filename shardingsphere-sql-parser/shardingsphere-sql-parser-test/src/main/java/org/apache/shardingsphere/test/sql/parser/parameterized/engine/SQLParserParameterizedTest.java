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

package org.apache.shardingsphere.test.sql.parser.parameterized.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.engine.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.SQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.loader.SQLCasesRegistry;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import static org.junit.Assert.fail;

@RequiredArgsConstructor
public abstract class SQLParserParameterizedTest {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = SQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private static final SQLParserTestCasesRegistry SQL_PARSER_TEST_CASES_REGISTRY = SQLParserTestCasesRegistryFactory.getInstance().getRegistry();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    protected static Collection<Object[]> getTestParameters(final String... databaseTypes) {
        checkTestCases();
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : SQL_CASES_LOADER.getSQLTestParameters(Arrays.asList(databaseTypes))) {
            if (!isPlaceholderWithoutParameter(each)) {
                result.add(each);
            }
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
    
    private static boolean isPlaceholderWithoutParameter(final Object[] sqlTestParameter) {
        return SQLCaseType.Placeholder == sqlTestParameter[2] && SQL_PARSER_TEST_CASES_REGISTRY.get(sqlTestParameter[0].toString()).getParameters().isEmpty();
    }
    
    @Test
    public final void assertSupportedSQL() {
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId);
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        String sql = SQL_CASES_LOADER.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId).getParameters());
        SQLStatement actual = SQLParserEngineFactory.getSQLParserEngine(databaseType).parse(sql, false);
        SQLStatementAssert.assertIs(new SQLCaseAssertContext(sqlCaseId, sqlCaseType), actual, expected);
    }
}
