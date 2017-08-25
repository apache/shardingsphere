package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderByClauseParser;

/**
 * Order by clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerOrderByClauseParser extends OrderByClauseParser {
    
    public SQLServerOrderByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        return OrderType.DESC;
    }
}
