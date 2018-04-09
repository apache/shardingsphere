/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.integrate.engine;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.integrate.asserts.ParserResultSetLoader;
import io.shardingjdbc.core.parsing.integrate.asserts.SQLStatementAssert;
import io.shardingjdbc.test.sql.SQLCaseType;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RequiredArgsConstructor
public final class IntegrateSupportedSQLParsingTest extends AbstractBaseIntegrateSQLParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static ParserResultSetLoader parserResultSetLoader = ParserResultSetLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    @Test
    public void assertSupportedSQL() {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserResultSetLoader.getParserResult(sqlCaseId).getParameters());
        new SQLStatementAssert(new SQLParsingEngine(databaseType, sql, getShardingRule()).parse(false), sqlCaseId, sqlCaseType).assertSQLStatement();
    }
}
