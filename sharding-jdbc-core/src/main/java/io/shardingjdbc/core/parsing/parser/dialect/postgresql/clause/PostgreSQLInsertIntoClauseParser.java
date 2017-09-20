package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.InsertIntoClauseParser;

/**
 * Insert into clause parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLInsertIntoClauseParser extends InsertIntoClauseParser {
    
    public PostgreSQLInsertIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine));
    }
}
