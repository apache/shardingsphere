package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
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
        if (lexerEngine.equalAny(DefaultKeyword.HAVING)) {
            throw new SQLParsingUnsupportedException(DefaultKeyword.HAVING);
        }
    }
}
