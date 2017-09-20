package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.InsertColumnsClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.InsertSetClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.InsertValuesClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleInsertIntoClauseParser;

/**
 * Insert clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public OracleInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new InsertValuesClauseParser(shardingRule, lexerEngine), new InsertSetClauseParser(shardingRule, lexerEngine));
    }
}
