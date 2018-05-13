package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.InsertColumnsClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.InsertSetClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.InsertValuesClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerInsertIntoClauseParser;

/**
 * Insert clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public SQLServerInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new InsertValuesClauseParser(shardingRule, lexerEngine), new InsertSetClauseParser(shardingRule, lexerEngine));
    }
}
