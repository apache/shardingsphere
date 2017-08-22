package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.AbstractOrderBySQLParser;

/**
 * MySQL Order By解析器.
 *
 * @author zhangliang
 */
public final class MySQLOrderBySQLParser extends AbstractOrderBySQLParser {
    
    public MySQLOrderBySQLParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        return OrderType.ASC;
    }
}
