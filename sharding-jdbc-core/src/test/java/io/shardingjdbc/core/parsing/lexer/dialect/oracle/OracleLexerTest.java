package io.shardingjdbc.core.parsing.lexer.dialect.oracle;

import io.shardingjdbc.core.parsing.lexer.LexerAssert;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import org.junit.Test;

public final class OracleLexerTest {
    
    @Test(expected = SQLParsingException.class)
    public void assertNextTokenForVariable() {
        OracleLexer lexer = new OracleLexer("SELECT @x1:=1 FROM TABLE_XXX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        lexer.nextToken();
    }
}
