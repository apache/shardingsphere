package com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerAssert;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import org.junit.Test;

public final class PostgreSQLLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        PostgreSQLLexer lexer = new PostgreSQLLexer("SELECT @@x1 FROM TABLE_XXX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Assist.ERROR, "");
    }
}
