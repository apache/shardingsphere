/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.lexer;

import io.shardingjdbc.core.parsing.lexer.analyzer.Dictionary;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Literals;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import org.junit.Test;

public final class LexerTest {
    
    private final Dictionary dictionary = new Dictionary();
    
    @Test
    public void assertNextTokenForWhitespace() {
        Lexer lexer = new Lexer("Select  \t \n * from \r\n TABLE_XXX \t", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "Select");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "from");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "TABLE_XXX");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForOrderBy() {
        Lexer lexer = new Lexer("SELECT * FROM ORDER  ORDER \t  BY XX DESC", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "ORDER");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.ORDER, "ORDER");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.BY, "BY");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.DESC, "DESC");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForGroupBy() {
        Lexer lexer = new Lexer("SELECT * FROM GROUP  Group \n  By XX DESC", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "GROUP");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.GROUP, "Group");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.BY, "By");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.DESC, "DESC");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForNumber() {
        assertNextTokenForNumber("0x1e", Literals.HEX);
        assertNextTokenForNumber("0x-1e", Literals.HEX);
        assertNextTokenForNumber("123", Literals.INT);
        assertNextTokenForNumber("-123", Literals.INT);
        assertNextTokenForNumber("-.123", Literals.FLOAT);
        assertNextTokenForNumber("123.0", Literals.FLOAT);
        assertNextTokenForNumber("123e4", Literals.FLOAT);
        assertNextTokenForNumber("123E4", Literals.FLOAT);
        assertNextTokenForNumber("123e+4", Literals.FLOAT);
        assertNextTokenForNumber("123E+4", Literals.FLOAT);
        assertNextTokenForNumber("123e-4", Literals.FLOAT);
        assertNextTokenForNumber("123E-4", Literals.FLOAT);
        assertNextTokenForNumber(".5", Literals.FLOAT);
        assertNextTokenForNumber("123f", Literals.FLOAT);
        assertNextTokenForNumber("123F", Literals.FLOAT);
        assertNextTokenForNumber(".5F", Literals.FLOAT);
        assertNextTokenForNumber("123d", Literals.FLOAT);
        assertNextTokenForNumber("123D", Literals.FLOAT);
    }
    
    private void assertNextTokenForNumber(final String expectedNumber, final TokenType expectedTokenType) {
        Lexer lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE WHERE XX=%s AND YY=%s", expectedNumber, expectedNumber), dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, expectedTokenType, expectedNumber);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.AND, "AND");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "YY");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, expectedTokenType, expectedNumber);
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForString() {
        Lexer lexer = new Lexer("SELECT * FROM XXX_TABLE WHERE XX IN ('xxx','x''x''''x',\"xyz\",\"x\"\"yz\")", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.IN, "IN");
        LexerAssert.assertNextToken(lexer, Symbol.LEFT_PAREN, "(");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "xxx");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "x''x''''x");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "xyz");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "x\"\"yz");
        LexerAssert.assertNextToken(lexer, Symbol.RIGHT_PAREN, ")");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForSingleLineComment() {
        Lexer lexer = new Lexer("SELECT * FROM XXX_TABLE --x\"y`z \n WHERE XX=1 //x\"y'z", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.INT, "1");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNextTokenForMultipleLineComment() {
        Lexer lexer = new Lexer("SELECT * FROM XXX_TABLE /*--xyz \n WHERE XX=1 //xyz*/ WHERE YY>2 /*--xyz //xyz*/", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "YY");
        LexerAssert.assertNextToken(lexer, Symbol.GT, ">");
        LexerAssert.assertNextToken(lexer, Literals.INT, "2");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }
    
    @Test
    public void assertNChar() {
        Lexer lexer = new Lexer("SELECT * FROM XXX_TABLE WHERE XX=N'xx'", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Symbol.STAR, "*");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.FROM, "FROM");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XXX_TABLE");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "WHERE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "XX");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "N");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "xx");
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertSyntaxErrorForUnclosedChar() {
        Lexer lexer = new Lexer("UPDATE product p SET p.title='Title's',s.description='中文' WHERE p.product_id=?", dictionary);
        LexerAssert.assertNextToken(lexer, DefaultKeyword.UPDATE, "UPDATE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "product");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "p");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SET, "SET");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "p");
        LexerAssert.assertNextToken(lexer, Symbol.DOT, ".");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "title");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "Title");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "s");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, ",s.description=");
        lexer.nextToken();
    }
}
