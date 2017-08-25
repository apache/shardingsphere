package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderByClauseParser;

/**
 * Order by clause parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLOrderByClauseParser extends OrderByClauseParser {
    
    public PostgreSQLOrderByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        return OrderType.DESC;
    }
}
