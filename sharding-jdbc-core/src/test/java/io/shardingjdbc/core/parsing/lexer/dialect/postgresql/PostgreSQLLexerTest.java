package io.shardingjdbc.core.parsing.lexer.dialect.postgresql;

import io.shardingjdbc.core.parsing.lexer.LexerAssert;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import org.junit.Test;

public final class PostgreSQLLexerTest {
    
    @Test(expected = SQLParsingException.class)
    public void assertNextTokenForVariable() {
        PostgreSQLLexer lexer = new PostgreSQLLexer("SELECT @@x1 FROM TABLE_XXX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        lexer.nextToken();
    }
}
