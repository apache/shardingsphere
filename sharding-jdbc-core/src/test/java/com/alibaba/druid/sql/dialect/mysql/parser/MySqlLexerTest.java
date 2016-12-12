package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.lexer.Token;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class MySqlLexerTest {
    
    private MySqlLexer lexer;
    
    @Test
    public void assertNextTokenForComments() {
        lexer = new MySqlLexer("SELECT * FROM TABLE_XXX # xxx ");
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.LINE_COMMENT));
        assertThat(lexer.getLiterals(), is(" # xxx "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.EOF));
    }
    
    @Test
    public void assertNextTokenForCommentsAndMultplieLines() {
        lexer = new MySqlLexer("SELECT * FROM TABLE_XXX # comment 1 \n #comment 2 \r\n WHERE XXX=1");
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.LINE_COMMENT));
        assertThat(lexer.getLiterals(), is(" # comment 1 "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.LINE_COMMENT));
        assertThat(lexer.getLiterals(), is(" #comment 2 \r"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.LITERAL_INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.EOF));
    }
    
    @Test
    public void assertNextTokenForHint() {
        lexer = new MySqlLexer("SELECT * FROM TABLE_XXX /*! hint 1 \n xxx */ WHERE XXX=1 /*!hint 2*/");
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.HINT));
        assertThat(lexer.getLiterals(), is(" hint 1 \n xxx "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.LITERAL_INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.HINT));
        assertThat(lexer.getLiterals(), is("hint 2"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(Token.EOF));
    }
}