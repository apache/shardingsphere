package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderBySQLParser;

/**
 * PostgreSQL Order By解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLOrderBySQLParser extends OrderBySQLParser {
    
    public PostgreSQLOrderBySQLParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        return OrderType.DESC;
    }
}
