package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SQLClauseParser;
import lombok.RequiredArgsConstructor;

/**
 * Oracle Model解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OracleModelClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * 解析Model语句.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(OracleKeyword.MODEL);
    }
}
