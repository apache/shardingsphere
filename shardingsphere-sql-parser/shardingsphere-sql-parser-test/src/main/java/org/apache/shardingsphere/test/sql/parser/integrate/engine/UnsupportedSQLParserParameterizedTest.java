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

package org.apache.shardingsphere.test.sql.parser.integrate.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.engine.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.sql.loader.UnsupportedSQLCasesRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class UnsupportedSQLParserParameterizedTest {
    
    private static final SQLCasesLoader SQL_CASES_LOADER = UnsupportedSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private final String sqlCaseId;
    
    private final String databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return SQL_CASES_LOADER.getSQLTestParameters(Collections.singleton("MySQL"));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertUnsupportedSQL() {
        String sql = SQL_CASES_LOADER.getSQL(sqlCaseId, sqlCaseType, Collections.emptyList());
        SQLParserEngineFactory.getSQLParserEngine("H2".equals(databaseType) ? "MySQL" : databaseType).parse(sql, false);
    }
}
