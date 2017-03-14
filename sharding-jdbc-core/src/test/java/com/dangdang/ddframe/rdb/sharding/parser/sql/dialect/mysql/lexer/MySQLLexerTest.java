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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Token;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class MySQLLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        MySQLLexer lexer = new MySQLLexer("SELECT @x1:=1 FROM TABLE_XXX WHERE COLUMN_XXX=  @@global.x1");
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.VARIANT));
        assertThat(lexer.getLiterals(), is("@x1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.COLON_EQ));
        assertThat(lexer.getLiterals(), is(":="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("COLUMN_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.VARIANT));
        assertThat(lexer.getLiterals(), is("@@global.x1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForComments() {
        MySQLLexer lexer = new MySQLLexer("SELECT * FROM TABLE_XXX # xxx ");
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.COMMENT));
        assertThat(lexer.getLiterals(), is(" # xxx "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForCommentsAndMultipleLines() {
        MySQLLexer lexer = new MySQLLexer("SELECT * FROM TABLE_XXX # comment 1 \n #comment 2 \r\n WHERE XXX=1");
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.COMMENT));
        assertThat(lexer.getLiterals(), is(" # comment 1 "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.COMMENT));
        assertThat(lexer.getLiterals(), is(" #comment 2 \r"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Assist.EOF));
    }
    
    @Test
    public void assertNextTokenForHint() {
        MySQLLexer lexer = new MySQLLexer("SELECT * FROM TABLE_XXX /*! hint 1 \n xxx */ WHERE XXX=1 /*!hint 2*/");
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.HINT));
        assertThat(lexer.getLiterals(), is(" hint 1 \n xxx "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Literals.HINT));
        assertThat(lexer.getLiterals(), is("hint 2"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Assist.EOF));
    }
}
