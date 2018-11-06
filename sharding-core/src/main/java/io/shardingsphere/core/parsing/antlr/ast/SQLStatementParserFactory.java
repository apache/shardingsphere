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
import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;

/**
 * SQL statement parser factory.
 * 
 * @author duhongjun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementParserFactory {
    
    /** 
     * New instance of SQL statement parser.
     * 
     * @param databaseType database type
     * @param sql SQL
     * @return SQL statement parser
     */
    public static SQLStatementParser newInstance(final DatabaseType databaseType, final String sql) {
        return newParser(databaseType, newLexer(databaseType, sql));
    }
    
    private static Lexer newLexer(final DatabaseType databaseType, final String sql) {
        try {
            return SQLStatementParserRegistry.getLexerClass(databaseType).getConstructor(CharStream.class).newInstance(CharStreams.fromString(sql));
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingException(ex);
        }
    }
    
    private static SQLStatementParser newParser(final DatabaseType databaseType, final Lexer lexer) {
        try {
            return SQLStatementParserRegistry.getParserClass(databaseType).getConstructor(TokenStream.class).newInstance(new CommonTokenStream(lexer));
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingException(ex);
        }
    }
}
