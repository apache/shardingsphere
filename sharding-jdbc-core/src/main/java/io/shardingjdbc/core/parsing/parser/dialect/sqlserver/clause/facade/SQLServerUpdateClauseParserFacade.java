package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.clause.UpdateSetItemsClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerWhereClauseParser;

/**
 * Update clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public SQLServerUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerTableReferencesClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new SQLServerWhereClauseParser(lexerEngine));
    }
}
