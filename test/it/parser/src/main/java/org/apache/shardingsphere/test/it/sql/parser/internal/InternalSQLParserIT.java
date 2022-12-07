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

import org.apache.shardingsphere.distsql.parser.engine.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.SQLParserTestCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.registry.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.SQLCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.registry.SQLCasesRegistry;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class InternalSQLParserIT {
    
    private static final SQLCases SQL_CASES = SQLCasesRegistry.getInstance().getCases();
    
    private static final SQLParserTestCases SQL_PARSER_TEST_CASES = SQLParserTestCasesRegistry.getInstance().getCases();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    public InternalSQLParserIT(final InternalSQLParserTestParameter testParam) {
        sqlCaseId = testParam.getSqlCaseId();
        databaseType = testParam.getDatabaseType();
        sqlCaseType = testParam.getSqlCaseType();
    }
    
    protected static Collection<InternalSQLParserTestParameter> getTestParameters(final String... databaseTypes) {
        Collection<InternalSQLParserTestParameter> result = new LinkedList<>();
        for (InternalSQLParserTestParameter each : SQL_CASES.generateTestParameters(Arrays.stream(databaseTypes).collect(Collectors.toSet()))) {
            if (!isPlaceholderWithoutParameter(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static boolean isPlaceholderWithoutParameter(final InternalSQLParserTestParameter testParam) {
        return SQLCaseType.Placeholder == testParam.getSqlCaseType() && SQL_PARSER_TEST_CASES.get(testParam.getSqlCaseId()).getParameters().isEmpty();
    }
    
    @Test
    public final void assertSupportedSQL() {
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES.get(sqlCaseId).getParameters());
        SQLStatement actual = parseSQLStatement("H2".equals(databaseType) ? "MySQL" : databaseType, sql);
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES.get(sqlCaseId);
        SQLStatementAssert.assertIs(new SQLCaseAssertContext(sqlCaseId, sql, expected.getParameters(), sqlCaseType), actual, expected);
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        return "ShardingSphere".equals(databaseType)
                ? new DistSQLStatementParserEngine().parse(sql)
                : new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false));
    }
}
