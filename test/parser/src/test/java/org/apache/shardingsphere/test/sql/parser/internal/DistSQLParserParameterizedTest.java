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

package org.apache.shardingsphere.test.sql.parser.internal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.engine.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.AbstractSQLStatementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.CasesRegistry;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.SQLParserTestCasesRegistry;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.SQLParserTestCasesRegistryFactory;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.distsql.loader.DistSQLCasesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class DistSQLParserParameterizedTest {
    
    private static final DistSQLCasesLoader DIST_SQL_CASES_LOADER = CasesRegistry.getInstance().getDistSQLCasesLoader();
    
    private static final SQLParserTestCasesRegistry SQL_PARSER_TEST_CASES_REGISTRY = SQLParserTestCasesRegistryFactory.getInstance().getRegistry();
    
    private static final DistSQLStatementParserEngine ENGINE = new DistSQLStatementParserEngine();
    
    private final String sqlCaseId;
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> getTestParameters() {
        return DIST_SQL_CASES_LOADER.getTestParameters(null);
    }
    
    @Test
    public void assertDistSQL() {
        SQLParserTestCase expected = SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId);
        String sql = DIST_SQL_CASES_LOADER.getCaseValue(sqlCaseId, null, SQL_PARSER_TEST_CASES_REGISTRY.get(sqlCaseId).getParameters(), null);
        SQLStatement actual = ENGINE.parse(sql);
        AbstractSQLStatementAssert.assertIs(new SQLCaseAssertContext(DIST_SQL_CASES_LOADER, sqlCaseId, null, null), actual, expected);
    }
}
