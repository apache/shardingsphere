package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.clause.UpdateSetItemsClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;

/**
 * Update clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public OracleUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleTableReferencesClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new OracleWhereClauseParser(lexerEngine));
    }
}
