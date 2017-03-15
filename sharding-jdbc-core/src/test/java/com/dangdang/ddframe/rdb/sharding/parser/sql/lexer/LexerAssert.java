package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import lombok.NoArgsConstructor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@NoArgsConstructor
public final class LexerAssert {
    
    public static void assertNextToken(final Lexer lexer, final TokenType expectedTokenType, final String expectedLiterals) {
        lexer.nextToken();
        Token actualToken = lexer.getToken();
        assertThat(actualToken.getType(), is(expectedTokenType));
        assertThat(actualToken.getLiterals(), is(expectedLiterals));
    }
}
