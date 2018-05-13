package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLWhereClauseParser;

/**
 * Delete clause parser facade for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public MySQLDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLTableReferencesClauseParser(shardingRule, lexerEngine), new MySQLWhereClauseParser(lexerEngine));
    }
}
