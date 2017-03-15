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

package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class LexerTest {
    
    private final Dictionary dictionary = new Dictionary();
    
    @Test
    public void assertNextTokenForWhitespace() {
        Lexer lexer = new Lexer("Select  \t \n * from \r\n TABLE_XXX \t", dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("Select"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("from"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForOrderBy() {
        Lexer lexer = new Lexer("SELECT * FROM ORDER  ORDER \t  BY XX DESC", dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("ORDER"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.ORDER));
        assertThat(lexer.getToken().getLiterals(), is("ORDER"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.BY));
        assertThat(lexer.getToken().getLiterals(), is("BY"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.DESC));
        assertThat(lexer.getToken().getLiterals(), is("DESC"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForGroupBy() {
        Lexer lexer = new Lexer("SELECT * FROM GROUP  Group \n  By XX DESC", dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("GROUP"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.GROUP));
        assertThat(lexer.getToken().getLiterals(), is("Group"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.BY));
        assertThat(lexer.getToken().getLiterals(), is("By"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.DESC));
        assertThat(lexer.getToken().getLiterals(), is("DESC"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForNumber() {
        assertNextTokenForNumber("0x1e", Literals.HEX);
        assertNextTokenForNumber("0x-1e", Literals.HEX);
        assertNextTokenForNumber("123", Literals.INT);
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
    
    private void assertNextTokenForNumber(final String number, final TokenType expectedTokenType) {
        Lexer lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE WHERE XX=%s AND YY=%s", number, number), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.WHERE));
        assertThat(lexer.getToken().getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.EQ));
        assertThat(lexer.getToken().getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is(expectedTokenType));
        assertThat(lexer.getToken().getLiterals(), is(number));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.AND));
        assertThat(lexer.getToken().getLiterals(), is("AND"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("YY"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.EQ));
        assertThat(lexer.getToken().getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is(expectedTokenType));
        assertThat(lexer.getToken().getLiterals(), is(number));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForString() {
        assertNextTokenForString("'xxx'");
        assertNextTokenForString("'x''x''''x'");
    }
    
    private void assertNextTokenForString(final String str) {
        Lexer lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE WHERE XX=%s AND YY=%s", str, str), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.WHERE));
        assertThat(lexer.getToken().getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.EQ));
        assertThat(lexer.getToken().getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.CHARS));
        assertThat(lexer.getToken().getLiterals(), is(str.substring(1, str.length() - 1)));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.AND));
        assertThat(lexer.getToken().getLiterals(), is("AND"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("YY"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.EQ));
        assertThat(lexer.getToken().getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.CHARS));
        assertThat(lexer.getToken().getLiterals(), is(str.substring(1, str.length() - 1)));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForAlias() {
        Lexer lexer = new Lexer("SELECT * FROM XXX_TABLE AS \"xyz\"", dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.AS));
        assertThat(lexer.getToken().getLiterals(), is("AS"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.ALIAS));
        assertThat(lexer.getToken().getLiterals(), is("\"xyz\""));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForSingleLineComment() {
        assertNextTokenForSingleLineComment("--xyz");
        assertNextTokenForSingleLineComment("//xyz");
    }
    
    private void assertNextTokenForSingleLineComment(final String comment) {
        Lexer lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE %s \n WHERE XX=1 %s", comment, comment), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.WHERE));
        assertThat(lexer.getToken().getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.EQ));
        assertThat(lexer.getToken().getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.INT));
        assertThat(lexer.getToken().getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForMultipleLineComment() {
        assertNextTokenForMultipleLineComment("/*--xyz", "//xyz*/");
    }
    
    private void assertNextTokenForMultipleLineComment(final String commentStart, final String commentEnd) {
        Lexer lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE %s \n WHERE XX=1 %s WHERE YY>2 %s %s", commentStart, commentEnd, commentStart, commentEnd), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.STAR));
        assertThat(lexer.getToken().getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.WHERE));
        assertThat(lexer.getToken().getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("YY"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.GT));
        assertThat(lexer.getToken().getLiterals(), is(">"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.INT));
        assertThat(lexer.getToken().getLiterals(), is("2"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
}
