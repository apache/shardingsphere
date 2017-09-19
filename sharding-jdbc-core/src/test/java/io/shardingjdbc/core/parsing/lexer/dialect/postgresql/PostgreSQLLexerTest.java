package io.shardingjdbc.core.parsing.lexer.dialect.postgresql;

import io.shardingjdbc.core.parsing.lexer.LexerAssert;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import org.junit.Test;

public final class PostgreSQLLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        PostgreSQLLexer lexer = new PostgreSQLLexer("SELECT @@x1 FROM TABLE_XXX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Assist.ERROR, "");
    }
}
