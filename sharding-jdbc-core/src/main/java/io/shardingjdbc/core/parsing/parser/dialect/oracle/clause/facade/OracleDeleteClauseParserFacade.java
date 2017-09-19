package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;

/**
 * Delete clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public OracleDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleTableReferencesClauseParser(shardingRule, lexerEngine), new OracleWhereClauseParser(lexerEngine));
    }
}
