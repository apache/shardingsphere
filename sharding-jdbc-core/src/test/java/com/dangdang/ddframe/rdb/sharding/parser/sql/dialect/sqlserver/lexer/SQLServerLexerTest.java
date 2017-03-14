package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.sqlserver.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.TokenType;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class SQLServerLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        SQLServerLexer lexer = new SQLServerLexer("SELECT @x1:=1, @@global.x1 FROM TABLE_XXX");
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.SELECT));
        assertThat(lexer.getToken().getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.VARIABLE));
        assertThat(lexer.getToken().getLiterals(), is("@x1"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.COLON_EQ));
        assertThat(lexer.getToken().getLiterals(), is(":="));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.INT));
        assertThat(lexer.getToken().getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Symbol.COMMA));
        assertThat(lexer.getToken().getLiterals(), is(","));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.VARIABLE));
        assertThat(lexer.getToken().getLiterals(), is("@@global.x1"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) DefaultKeyword.FROM));
        assertThat(lexer.getToken().getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Literals.IDENTIFIER));
        assertThat(lexer.getToken().getLiterals(), is("TABLE_XXX"));
        assertThat(lexer.getToken().getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken().getType(), is((TokenType) Assist.EOF));
    }
}
