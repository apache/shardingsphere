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

package org.apache.shardingsphere.test.sql.parser.internal.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.engine.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.registry.CasesRegistry;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.registry.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.registry.SQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.loader.SQLCasesLoader;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

@RequiredArgsConstructor
public abstract class SQLParserParameterizedTest {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = CasesRegistry.getInstance().getSqlCasesLoader();
    
    private static final SQLParserTestCasesRegistry SQL_PARSER_TEST_CASES_REGISTRY = SQLParserTestCasesRegistryFactory.getInstance().getRegistry();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    protected static Collection<Object[]> getTestParameters(final String... databaseTypes) {
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : SQL_CASES_LOADER.getTestParameters(Arrays.asList(databaseTypes))) {
            if (!isPlaceholderWithoutParameter(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static boolean isPlaceholderWithoutParameter(final Object[] sqlTestParam) {
        return SQLCaseType.Placeholder == sqlTestParam[2] && SQL_PARSER_TEST_CASES_REGISTRY.get(sqlTestParam[0].toString()).getParameters().isEmpty();
    }
    
    @Test
    public final void assertSupportedSQL() {
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId);
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        String sql = SQL_CASES_LOADER.getCaseValue(sqlCaseId, sqlCaseType, SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId).getParameters(), databaseType);
        SQLStatement actual = parseSQLStatement(databaseType, sql);
        SQLStatementAssert.assertIs(new SQLCaseAssertContext(SQL_CASES_LOADER, sqlCaseId, sqlCaseType, databaseType), actual, expected);
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        return "ShardingSphere".equals(databaseType)
                ? new DistSQLStatementParserEngine().parse(sql)
                : new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false));
    }
}
