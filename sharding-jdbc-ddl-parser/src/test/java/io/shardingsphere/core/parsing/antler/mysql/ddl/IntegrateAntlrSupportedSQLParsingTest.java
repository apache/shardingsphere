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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.parser.antlr.MySQLStatementLexer;
import io.shardingsphere.parser.antlr.MySQLStatementParser;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class IntegrateAntlrSupportedSQLParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameterized.Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getAntlrSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    @Test
    public void assertUnsupportedSQL() {
        CodePointCharStream cs = CharStreams.fromString(sqlCasesLoader.getAntlrSupportedSQL(sqlCaseId, sqlCaseType, Collections.emptyList()));
        switch (databaseType) {
            case MySQL:
                MySQLStatementLexer lexer = new MySQLStatementLexer(cs);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                MySQLStatementParser parser = new MySQLStatementParser(tokens);
                parser.addErrorListener(new BaseErrorListener() {
                    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                        throw new RuntimeException();
                    }
                });
                parser.execute();
                break;
            case Oracle:
                break;
            case PostgreSQL:
                break;
            case SQLServer:
                break;
            default:
                break;
        }
    }
}
