package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
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
