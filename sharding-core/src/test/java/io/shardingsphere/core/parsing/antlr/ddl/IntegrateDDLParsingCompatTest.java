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

package io.shardingsphere.core.parsing.antlr.ddl;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.AntlrParsingEngine;
import io.shardingsphere.core.parsing.antlr.parser.impl.dialect.MySQLParser;
import io.shardingsphere.core.parsing.antlr.parser.impl.dialect.OracleParser;
import io.shardingsphere.core.parsing.antlr.parser.impl.dialect.PostgreSQLParser;
import io.shardingsphere.core.parsing.antlr.parser.impl.dialect.SQLServerParser;
import io.shardingsphere.core.parsing.antlr.autogen.MySQLStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.OracleStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.PostgreSQLStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.SQLServerStatementLexer;
import io.shardingsphere.core.parsing.integrate.asserts.ParserResultSetLoader;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssert;
import io.shardingsphere.core.parsing.integrate.engine.AbstractBaseIntegrateSQLParsingTest;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class IntegrateDDLParsingCompatTest extends AbstractBaseIntegrateSQLParsingTest {
    
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
        CodePointCharStream charStream = CharStreams.fromString(sql);
        switch (databaseType) {
            case H2:
            case MySQL:
                execute(MySQLStatementLexer.class, MySQLParser.class, charStream);
                break;
            case Oracle:
                execute(OracleStatementLexer.class, OracleParser.class, charStream);
                break;
            case PostgreSQL:
                execute(PostgreSQLStatementLexer.class, PostgreSQLParser.class, charStream);
                break;
            case SQLServer:
                execute(SQLServerStatementLexer.class, SQLServerParser.class, charStream);
                break;
            default:
                break;
        }
    }
    
    @Test
    public void assertSupportedSQL() {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserResultSetLoader.getParserResult(sqlCaseId).getParameters());
        DatabaseType execDatabaseType = databaseType;
        if (DatabaseType.H2 == databaseType) {
            execDatabaseType = DatabaseType.MySQL;
        }
        new SQLStatementAssert(new AntlrParsingEngine(
                execDatabaseType, sql, getShardingRule(), getShardingTableMetaData()).parse(), sqlCaseId, sqlCaseType, sqlCasesLoader, parserResultSetLoader).assertSQLStatement();
    }
    
    /**
     * Execute.
     * @param lexerClass lexer class
     * @param parserClass parser class
     * @param charStream char stream
     * @throws Exception exception
     */
    public void execute(final Class<?> lexerClass, final Class<?> parserClass, final CharStream charStream) throws Exception {
        Constructor<?> lexerCon = lexerClass.getConstructor(CharStream.class);
        Lexer lexer = (Lexer) lexerCon.newInstance(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        Constructor<?> parserCon = parserClass.getConstructor(TokenStream.class);
        Object parser = parserCon.newInstance(tokenStream);
        Method addErrorListener = parserClass.getMethod("addErrorListener", ANTLRErrorListener.class);
        addErrorListener.invoke(parser, new BaseErrorListener() {
            
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException ex) {
                throw new RuntimeException();
            }
        });
        Method execute = parserClass.getMethod("execute");
        execute.invoke(parser);
    }
}
