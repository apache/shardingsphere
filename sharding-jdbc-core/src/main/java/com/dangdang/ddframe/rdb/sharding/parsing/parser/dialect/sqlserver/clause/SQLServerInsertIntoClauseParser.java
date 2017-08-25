package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertIntoClauseParser;

/**
 * Insert into clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerInsertIntoClauseParser extends InsertIntoClauseParser {
    
    public SQLServerInsertIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new SQLServerTableReferencesClauseParser(shardingRule, lexerEngine));
    }
}
