package io.shardingjdbc.core.parsing.lexer;

import io.shardingjdbc.core.parsing.lexer.token.Token;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import lombok.NoArgsConstructor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@NoArgsConstructor
public final class LexerAssert {
    
    public static void assertNextToken(final Lexer lexer, final TokenType expectedTokenType, final String expectedLiterals) {
        lexer.nextToken();
        Token actualToken = lexer.getCurrentToken();
        assertThat(actualToken.getType(), is(expectedTokenType));
        assertThat(actualToken.getLiterals(), is(expectedLiterals));
    }
}
