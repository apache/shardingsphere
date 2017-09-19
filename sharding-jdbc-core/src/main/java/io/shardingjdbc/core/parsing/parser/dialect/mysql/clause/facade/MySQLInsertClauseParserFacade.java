package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.InsertColumnsClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLInsertIntoClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLInsertSetClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLInsertValuesClauseParser;

/**
 * Insert clause parser facade for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public MySQLInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new MySQLInsertValuesClauseParser(shardingRule, lexerEngine), new MySQLInsertSetClauseParser(shardingRule, lexerEngine));
    }
}
