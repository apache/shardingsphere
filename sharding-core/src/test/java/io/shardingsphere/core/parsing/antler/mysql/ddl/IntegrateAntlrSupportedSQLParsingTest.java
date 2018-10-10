/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.antler.mysql.ddl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antler.parser.factory.StatementFactory;
import io.shardingsphere.core.parsing.integrate.asserts.AntlrParserResultSetLoader;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssert;
import io.shardingsphere.core.parsing.integrate.engine.AbstractBaseIntegrateSQLParsingTest;
import io.shardingsphere.parser.antlr.MySQLStatementLexer;
import io.shardingsphere.parser.antlr.MySQLStatementParser;
import io.shardingsphere.parser.antlr.PostgreStatementLexer;
import io.shardingsphere.parser.antlr.PostgreStatementParser;
import io.shardingsphere.test.sql.AntlrSQLCasesLoader;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class IntegrateAntlrSupportedSQLParsingTest extends AbstractBaseIntegrateSQLParsingTest{
    
    private static SQLCasesLoader sqlCasesLoader = AntlrSQLCasesLoader.getInstance();
    
    private static AntlrParserResultSetLoader parserResultSetLoader = AntlrParserResultSetLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameterized.Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    @Test
    public void assertUnsupportedSQL() {
        CodePointCharStream cs = CharStreams.fromString(sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, Collections.emptyList()));
        switch (databaseType) {
            case MySQL:
                MySQLStatementLexer mysqlStatementLexer = new MySQLStatementLexer(cs);
                MySQLStatementParser mysqlStatementParser = new MySQLStatementParser(new CommonTokenStream(mysqlStatementLexer));
                mysqlStatementParser.addErrorListener(new BaseErrorListener() {
                    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                        throw new RuntimeException();
                    }
                });
                mysqlStatementParser.execute();
                break;
            case Oracle:
                break;
            case PostgreSQL:
                PostgreStatementLexer postgreStatementLexer = new PostgreStatementLexer(cs);
                PostgreStatementParser postgreStatementParser = new PostgreStatementParser(new CommonTokenStream(postgreStatementLexer));
                postgreStatementParser.addErrorListener(new BaseErrorListener() {
                    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                        throw new RuntimeException();
                    }
                });
                postgreStatementParser.execute();
                break;
            case SQLServer:
                break;
            default:
                break;
        }
    }
    
    @Test
    public void assertSupportedSQL() {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserResultSetLoader.getParserResult(sqlCaseId).getParameters());
        new SQLStatementAssert(StatementFactory.getStatement(databaseType, null, getShardingRule(), sql, getShardingTableMetaData()), sqlCaseId, sqlCaseType, AntlrSQLCasesLoader.getInstance(), AntlrParserResultSetLoader.getInstance()).assertSQLStatement();
    }
}
