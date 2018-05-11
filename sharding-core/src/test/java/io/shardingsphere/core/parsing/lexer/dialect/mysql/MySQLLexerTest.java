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

package io.shardingsphere.core.parsing.lexer.dialect.mysql;

import io.shardingsphere.core.parsing.lexer.LexerAssert;
import io.shardingsphere.core.parsing.lexer.token.Assist;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Literals;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import org.junit.Test;

public final class MySQLLexerTest {
    
    @Test
    public void assertNextTokenForHint() {
        MySQLLexer lexer = new MySQLLexer("SELECT * FROM XXX_TABLE /*! hint 1 \n xxx */ WHERE XX>1 /*!hint 2*/");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, Symbol.GT, ">");
        LexerAssert.assertNextToken(lexer, Literals.INT, "1");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForComment() {
        MySQLLexer lexer = new MySQLLexer("SELECT * FROM XXX_TABLE # xxx ");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForMultipleLinesComment() {
        MySQLLexer lexer = new MySQLLexer("SELECT * FROM XXX_TABLE # comment 1 \n #comment 2 \r\n WHERE XX<=1");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, Symbol.LT_EQ, "<=");
        LexerAssert.assertNextToken(lexer, Literals.INT, "1");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForVariable() {
        MySQLLexer lexer = new MySQLLexer("SELECT @x1:=1 FROM XXX_TABLE WHERE XX=  @@global.x1");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Literals.VARIABLE, "@x1");
        LexerAssert.assertNextToken(lexer, Symbol.COLON_EQ, ":=");
        LexerAssert.assertNextToken(lexer, Literals.INT, "1");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.VARIABLE, "@@global.x1");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
}
