package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.sqlserver.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.GeneralLiterals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.SpecialLiterals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Token;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class SQLServerLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        SQLServerLexer lexer = new SQLServerLexer("SELECT @x1:=1, @@global.x1 FROM TABLE_XXX");
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) SpecialLiterals.VARIANT));
        assertThat(lexer.getLiterals(), is("@x1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.COLON_EQ));
        assertThat(lexer.getLiterals(), is(":="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) GeneralLiterals.INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.COMMA));
        assertThat(lexer.getLiterals(), is(","));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) SpecialLiterals.VARIANT));
        assertThat(lexer.getLiterals(), is("@@global.x1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) GeneralLiterals.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Assist.EOF));
    }
}
