package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.dialect.mysql.lexer.MySQLLexer;
import com.alibaba.druid.sql.lexer.DataType;
import com.alibaba.druid.sql.lexer.DefaultKeyword;
import com.alibaba.druid.sql.lexer.Symbol;
import com.alibaba.druid.sql.lexer.Token;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class MySQLLexerTest {
    
    private MySQLLexer lexer;
    
    @Test
    public void assertNextTokenForComments() {
        lexer = new MySQLLexer("SELECT * FROM TABLE_XXX # xxx ");
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
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LINE_COMMENT));
        assertThat(lexer.getLiterals(), is(" # xxx "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForCommentsAndMultplieLines() {
        lexer = new MySQLLexer("SELECT * FROM TABLE_XXX # comment 1 \n #comment 2 \r\n WHERE XXX=1");
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
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LINE_COMMENT));
        assertThat(lexer.getLiterals(), is(" # comment 1 "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LINE_COMMENT));
        assertThat(lexer.getLiterals(), is(" #comment 2 \r"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LITERAL_INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForHint() {
        lexer = new MySQLLexer("SELECT * FROM TABLE_XXX /*! hint 1 \n xxx */ WHERE XXX=1 /*!hint 2*/");
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
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.HINT));
        assertThat(lexer.getLiterals(), is(" hint 1 \n xxx "));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LITERAL_INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.HINT));
        assertThat(lexer.getLiterals(), is("hint 2"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
}
