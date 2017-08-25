package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertIntoClauseParser;

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
