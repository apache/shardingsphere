package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Token;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class PostgreSQLLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        PostgreSQLLexer lexer = new PostgreSQLLexer("SELECT @@x1 FROM TABLE_XXX");
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Assist.ERROR));
    }
}
