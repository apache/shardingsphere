package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SQLClauseParser;
import lombok.RequiredArgsConstructor;

/**
 * Model clause parser for Oracle.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OracleModelClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse model.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(OracleKeyword.MODEL);
    }
}
