package com.dangdang.ddframe.rdb.sharding.parser.dialect.postgresql.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.lexer.LexerAssert;
import org.junit.Test;

public final class PostgreSQLLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        PostgreSQLLexer lexer = new PostgreSQLLexer("SELECT @@x1 FROM TABLE_XXX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Assist.ERROR, "");
    }
}
