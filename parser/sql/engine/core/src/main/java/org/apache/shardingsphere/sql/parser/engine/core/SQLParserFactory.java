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

package org.apache.shardingsphere.sql.parser.engine.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;

import java.nio.CharBuffer;

/**
 * SQL parser factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserFactory {
    
    /**
     * Create new instance of SQL parser.
     *
     * @param sql SQL
     * @param lexerClass lexer class
     * @param parserClass parser class
     * @return created instance
     */
    public static SQLParser newInstance(final String sql, final Class<? extends SQLLexer> lexerClass, final Class<? extends SQLParser> parserClass) {
        return createSQLParser(createTokenStream(sql, lexerClass), parserClass);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static SQLParser createSQLParser(final TokenStream tokenStream, final Class<? extends SQLParser> parserClass) {
        SQLParser result = parserClass.getConstructor(TokenStream.class).newInstance(tokenStream);
        ((Parser) result).setErrorHandler(new BailErrorStrategy());
        ((Parser) result).removeErrorListener(ConsoleErrorListener.INSTANCE);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static TokenStream createTokenStream(final String sql, final Class<? extends SQLLexer> lexerClass) {
        Lexer lexer = (Lexer) lexerClass.getConstructor(CharStream.class).newInstance(getSQLCharStream(sql));
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        return new CommonTokenStream(lexer);
    }
    
    private static CharStream getSQLCharStream(final String sql) {
        CodePointBuffer buffer = CodePointBuffer.withChars(CharBuffer.wrap(sql.toCharArray()));
        return CodePointCharStream.fromBuffer(buffer);
    }
}
