package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.AbstractOrderBySQLParser;

/**
 * SQLServer Order By解析器.
 *
 * @author zhangliang
 */
public final class SQLServerOrderBySQLParser extends AbstractOrderBySQLParser {
    
    public SQLServerOrderBySQLParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        return OrderType.DESC;
    }
}
