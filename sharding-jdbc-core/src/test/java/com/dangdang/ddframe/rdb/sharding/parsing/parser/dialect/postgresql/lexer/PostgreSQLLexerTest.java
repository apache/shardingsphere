package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.lexer;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerAssert;
import org.junit.Test;

public final class PostgreSQLLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        PostgreSQLLexer lexer = new PostgreSQLLexer("SELECT @@x1 FROM TABLE_XXX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Assist.ERROR, "");
    }
}
