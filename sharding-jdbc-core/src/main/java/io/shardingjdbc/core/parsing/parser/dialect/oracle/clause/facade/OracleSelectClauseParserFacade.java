package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.facade;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.HavingClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SelectRestClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleDistinctClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleGroupByClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleOrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleSelectListClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;
import io.shardingjdbc.core.rule.ShardingRule;

/**
 * Select clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public OracleSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleDistinctClauseParser(lexerEngine), new OracleSelectListClauseParser(shardingRule, lexerEngine),
                new OracleTableReferencesClauseParser(shardingRule, lexerEngine), new OracleWhereClauseParser(lexerEngine), new OracleGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new OracleOrderByClauseParser(lexerEngine), new SelectRestClauseParser(lexerEngine));
    }
}
