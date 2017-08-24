package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import lombok.RequiredArgsConstructor;

/**
 * Having从句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class HavingClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * 解析Having.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(DefaultKeyword.HAVING);
    }
}
