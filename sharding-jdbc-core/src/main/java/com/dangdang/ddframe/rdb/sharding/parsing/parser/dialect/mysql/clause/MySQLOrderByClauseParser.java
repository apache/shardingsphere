package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderByClauseParser;

/**
 * MySQL排序从句解析器.
 *
 * @author zhangliang
 */
public final class MySQLOrderByClauseParser extends OrderByClauseParser {
    
    public MySQLOrderByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
}
