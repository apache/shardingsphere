package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerWhereClauseParser;

/**
 * Delete clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public SQLServerDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerTableReferencesClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine));
    }
}
