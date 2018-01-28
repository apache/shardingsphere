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

package io.shardingjdbc.core.parsing.lexer.dialect.mysql;

import io.shardingjdbc.core.parsing.lexer.LexerAssert;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Literals;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
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

    @Test
    public void assertNextTokenForInsert() {
        MySQLLexer lexer = new MySQLLexer("insert into t_good_xxx (id, name, price, count, create_time) values (1, 'Sharding-JDBC Cook Book', 23.50, 999, '2018-01-28 08:15:37')");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.INSERT, "insert");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.INTO, "into");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "t_good_xxx");
        LexerAssert.assertNextToken(lexer, Symbol.LEFT_PAREN, "(");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "id");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "name");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "price");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");

        // 'COUNT' is in DefaultKeyword.COUNT
        // is 'count' here a SQL Keyword or a Identifier ?
        // maybe it's LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "count"); ?
        LexerAssert.assertNextToken(lexer, DefaultKeyword.COUNT, "count");

        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "create_time");
        LexerAssert.assertNextToken(lexer, Symbol.RIGHT_PAREN, ")");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.VALUES, "values");
        LexerAssert.assertNextToken(lexer, Symbol.LEFT_PAREN, "(");
        LexerAssert.assertNextToken(lexer, Literals.INT, "1");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "Sharding-JDBC Cook Book");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.FLOAT, "23.50");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.INT, "999");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "2018-01-28 08:15:37");
        LexerAssert.assertNextToken(lexer, Symbol.RIGHT_PAREN, ")");
        LexerAssert.assertNextToken(lexer, Assist.END, "");
    }

    @Test
    public void assertNextTokenForUpdate() {
        MySQLLexer lexer = new MySQLLexer("UPDATE t_good_xxx SET name='Sharding-JDBC: The Definitive Guide', price=27.35, count=900, create_time='20180128 19:34:48' where id=1");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.UPDATE, "UPDATE");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "t_good_xxx");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SET, "SET");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "name");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "Sharding-JDBC: The Definitive Guide");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "price");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.FLOAT, "27.35");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");

        // 'COUNT' is in DefaultKeyword.COUNT
        // is 'count' here a SQL Keyword or a Identifier ?
        // maybe it's LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "count"); ?
        LexerAssert.assertNextToken(lexer, DefaultKeyword.COUNT, "count");

        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.INT, "900");
        LexerAssert.assertNextToken(lexer, Symbol.COMMA, ",");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "create_time");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.CHARS, "20180128 19:34:48");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.WHERE, "where");
        LexerAssert.assertNextToken(lexer, Literals.IDENTIFIER, "id");
        LexerAssert.assertNextToken(lexer, Symbol.EQ, "=");
        LexerAssert.assertNextToken(lexer, Literals.INT, "1");
    }
}
