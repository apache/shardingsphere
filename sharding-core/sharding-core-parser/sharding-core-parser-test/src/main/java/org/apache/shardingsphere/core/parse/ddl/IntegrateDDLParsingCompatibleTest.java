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

package org.apache.shardingsphere.core.parse.ddl;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.integrate.asserts.ParserResultSetLoader;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssert;
import org.apache.shardingsphere.core.parse.integrate.engine.AbstractBaseIntegrateSQLParsingTest;
import org.apache.shardingsphere.core.parsing.antlr.AntlrParsingEngine;
import org.apache.shardingsphere.core.parsing.antlr.parser.SQLParserFactory;
import org.apache.shardingsphere.core.parsing.api.SQLParser;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class IntegrateDDLParsingCompatibleTest extends AbstractBaseIntegrateSQLParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static ParserResultSetLoader parserResultSetLoader = ParserResultSetLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameterized.Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        sqlCasesLoader.switchSQLCase("sql/ddl");
        parserResultSetLoader.switchResult("prior_parser_for_antlr");
        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    @Test
    public void parsingSupportedSQL() throws Exception {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, Collections.emptyList());
        SQLParser sqlParser = SQLParserFactory.newInstance(databaseType, sql);
        Method addErrorListener = sqlParser.getClass().getMethod("addErrorListener", ANTLRErrorListener.class);
        addErrorListener.invoke(sqlParser, new BaseErrorListener() {
        
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException ex) {
                throw new RuntimeException();
            }
        });
        sqlParser.execute();
    }
    
    @Test
    public void assertSupportedSQL() {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserResultSetLoader.getParserResult(sqlCaseId).getParameters());
        DatabaseType execDatabaseType = databaseType;
        if (DatabaseType.H2 == databaseType) {
            execDatabaseType = DatabaseType.MySQL;
        }
        new SQLStatementAssert(new AntlrParsingEngine(
                execDatabaseType, sql, AbstractBaseIntegrateSQLParsingTest.getShardingRule(), 
                AbstractBaseIntegrateSQLParsingTest.getShardingTableMetaData()).parse(), sqlCaseId, sqlCaseType, sqlCasesLoader, parserResultSetLoader).assertSQLStatement();
    }
}
