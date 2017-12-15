package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.HavingClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SelectListClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLDistinctClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLGroupByClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLSelectRestClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLWhereClauseParser;

/**
 * Select clause parser facade for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public MySQLSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLDistinctClauseParser(lexerEngine), new SelectListClauseParser(shardingRule, lexerEngine),
                new MySQLTableReferencesClauseParser(shardingRule, lexerEngine), new MySQLWhereClauseParser(lexerEngine), new MySQLGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new OrderByClauseParser(lexerEngine), new MySQLSelectRestClauseParser(lexerEngine));
    }
}
