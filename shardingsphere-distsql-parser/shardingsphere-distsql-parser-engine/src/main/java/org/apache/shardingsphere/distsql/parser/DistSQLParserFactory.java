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

package org.apache.shardingsphere.distsql.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementLexer;
import org.apache.shardingsphere.distsql.parser.sql.parser.DistSQLParser;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;

import java.nio.CharBuffer;

/**
 * Dist SQL parser factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLParserFactory {
    
    /**
     * New instance of Dist SQL parser.
     *
     * @param sql SQL
     * @return Dist SQL parser
     */
    public static SQLParser newInstance(final String sql) {
        return new DistSQLParser(createTokenStream(sql));
    }
    
    private static TokenStream createTokenStream(final String sql) {
        Lexer lexer = new DistSQLStatementLexer(getSQLCharStream(sql));
        return new CommonTokenStream(lexer);
    }
    
    private static CharStream getSQLCharStream(final String sql) {
        CodePointBuffer buffer = CodePointBuffer.withChars(CharBuffer.wrap(sql.toCharArray()));
        return CodePointCharStream.fromBuffer(buffer);
    }
}
