package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.InsertIntoClauseParser;

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
