package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SQLClauseParser;
import lombok.RequiredArgsConstructor;

/**
 * Oracle HierarchicalQuery解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OracleHierarchicalQueryClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * 解析HierarchicalQuery语句.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(OracleKeyword.CONNECT, OracleKeyword.START);
    }
}
