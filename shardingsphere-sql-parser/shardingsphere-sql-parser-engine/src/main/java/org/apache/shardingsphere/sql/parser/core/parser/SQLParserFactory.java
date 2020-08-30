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

package org.apache.shardingsphere.sql.parser.core.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.core.SQLParserConfigurationRegistry;
import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;

import java.nio.CharBuffer;

/**
 * SQL parser factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserFactory {
    
    /** 
     * New instance of SQL parser.
     * 
     * @param databaseTypeName name of database type
     * @param sql SQL
     * @return SQL parser
     */
    public static SQLParser newInstance(final String databaseTypeName, final String sql) {
        return createSQLParser(sql, SQLParserConfigurationRegistry.getInstance().getSQLParserConfiguration(databaseTypeName));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static SQLParser createSQLParser(final String sql, final SQLParserConfiguration configuration) {
        CodePointBuffer buffer = CodePointBuffer.withChars(CharBuffer.wrap(sql.toCharArray()));
        CodePointCharStream codePointCharStream = CodePointCharStream.fromBuffer(buffer);
        Lexer lexer = (Lexer) configuration.getLexerClass().getConstructor(CharStream.class).newInstance(codePointCharStream);
        return configuration.getParserClass().getConstructor(TokenStream.class).newInstance(new CommonTokenStream(lexer));
    }
}
