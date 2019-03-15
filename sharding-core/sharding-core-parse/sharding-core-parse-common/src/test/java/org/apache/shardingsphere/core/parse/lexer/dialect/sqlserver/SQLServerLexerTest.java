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

package org.apache.shardingsphere.core.parse.lexer.dialect.sqlserver;

import org.apache.shardingsphere.core.parse.lexer.LexerAssert;
import org.apache.shardingsphere.core.parse.lexer.token.Assist;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.lexer.token.Literals;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.junit.Test;

public final class SQLServerLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        SQLServerLexer lexer = new SQLServerLexer("SELECT @x1:=1, @@global.x1 FROM XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Literals.VARIABLE, "@x1");
        LexerAssert.assertNextToken(lexer, Symbol.COLON_EQ, ":=");
        LexerAssert.assertNextToken(lexer, Literals.INT, "1");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.VARIABLE, "@@global.x1");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNChar() {
        SQLServerLexer lexer = new SQLServerLexer("SELECT * FROM XXX_TABLE WHERE XX=N'xx'");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "xx");
    }
}
