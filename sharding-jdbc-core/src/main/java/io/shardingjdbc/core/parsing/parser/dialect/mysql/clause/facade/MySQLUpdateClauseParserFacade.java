package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.UpdateSetItemsClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLWhereClauseParser;

/**
 * Update clause parser facade for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public MySQLUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLTableReferencesClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new MySQLWhereClauseParser(lexerEngine));
    }
}
