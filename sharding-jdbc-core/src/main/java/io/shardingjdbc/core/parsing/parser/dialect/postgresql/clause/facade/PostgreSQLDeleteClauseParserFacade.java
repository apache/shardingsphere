package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.facade;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.PostgreSQLTableReferencesClauseParser;

/**
 * Delete clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public PostgreSQLDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine), new WhereClauseParser(DatabaseType.PostgreSQL, lexerEngine));
    }
}
