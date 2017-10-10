package io.shardingjdbc.core.parsing.lexer.dialect.oracle;

import io.shardingjdbc.core.parsing.lexer.LexerAssert;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import org.junit.Test;

public final class OracleLexerTest {
    
    @Test
    public void assertNextTokenForVariable() {
        OracleLexer lexer = new OracleLexer("SELECT @x1:=1 FROM TABLE_XXX");
        LexerAssert.assertNextToken(lexer, DefaultKeyword.SELECT, "SELECT");
        LexerAssert.assertNextToken(lexer, Assist.ERROR, "");
    }
}
