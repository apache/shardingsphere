package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause;

import io.shardingjdbc.core.constant.OrderType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;

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
