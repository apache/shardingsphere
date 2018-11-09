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

package io.shardingsphere.core.parsing.antlr.ast;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.ast.dialect.MySQLStatementASTParser;
import io.shardingsphere.core.parsing.antlr.ast.dialect.OracleStatementASTParser;
import io.shardingsphere.core.parsing.antlr.ast.dialect.PostgreSQLStatementASTParser;
import io.shardingsphere.core.parsing.antlr.ast.dialect.SQLServerStatementASTParser;
import io.shardingsphere.core.parsing.antlr.autogen.MySQLStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.OracleStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.PostgreSQLStatementLexer;
import io.shardingsphere.core.parsing.antlr.autogen.SQLServerStatementLexer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;

/**
 * SQL AST parser factory.
 * 
 * @author duhongjun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLASTParserFactory {
    
    /** 
     * New instance of SQL statement parser.
     * 
     * @param databaseType database type
     * @param sql SQL
     * @return SQL statement parser
     */
    public static SQLASTParser newInstance(final DatabaseType databaseType, final String sql) {
        return newParser(databaseType, newLexer(databaseType, sql));
    }
    
    private static Lexer newLexer(final DatabaseType databaseType, final String sql) {
        CharStream sqlCharStream = CharStreams.fromString(sql);
        switch (databaseType) {
            case H2:
            case MySQL:
                return new MySQLStatementLexer(sqlCharStream);
            case PostgreSQL:
                return new PostgreSQLStatementLexer(sqlCharStream);
            case SQLServer:
                return new SQLServerStatementLexer(sqlCharStream);
            case Oracle:
                return new OracleStatementLexer(sqlCharStream);
            default:
                throw new UnsupportedOperationException(String.format("Can not support database type [%s].", databaseType));
        }
    }
    
    private static SQLASTParser newParser(final DatabaseType databaseType, final Lexer lexer) {
        TokenStream tokenStream = new CommonTokenStream(lexer);
        switch (databaseType) {
            case H2:
            case MySQL:
                return new MySQLStatementASTParser(tokenStream);
            case PostgreSQL:
                return new PostgreSQLStatementASTParser(tokenStream);
            case SQLServer:
                return new SQLServerStatementASTParser(tokenStream);
            case Oracle:
                return new OracleStatementASTParser(tokenStream);
            default:
                throw new UnsupportedOperationException(String.format("Can not support database type [%s].", databaseType));
        }
    }
}
