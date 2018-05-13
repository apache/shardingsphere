package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.constant.OrderType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;

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
