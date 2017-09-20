package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.UpdateSetItemsClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.PostgreSQLTableReferencesClauseParser;

/**
 * Update clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public PostgreSQLUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new WhereClauseParser(lexerEngine));
    }
}
