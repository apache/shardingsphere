package io.shardingjdbc.core.parsing.parser.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import lombok.RequiredArgsConstructor;

/**
 * Having clause parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class HavingClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse having.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(DefaultKeyword.HAVING);
    }
}
