package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderBySQLParser;

/**
 * MySQL Order By解析器.
 *
 * @author zhangliang
 */
public final class MySQLOrderBySQLParser extends OrderBySQLParser {
    
    public MySQLOrderBySQLParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
}
