package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Token;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class OracleLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        OracleLexer lexer = new OracleLexer("SELECT @x1:=1 FROM TABLE_XXX");
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Assist.ERROR));
    }
}
