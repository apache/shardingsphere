package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SQLClauseParser;
import lombok.RequiredArgsConstructor;

/**
 * Hierarchical query clause parser for Oracle.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OracleHierarchicalQueryClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse hierarchical query.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(OracleKeyword.CONNECT, OracleKeyword.START);
    }
}
