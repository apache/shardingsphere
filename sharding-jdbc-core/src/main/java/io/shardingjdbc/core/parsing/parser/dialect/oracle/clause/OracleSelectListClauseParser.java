package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.SelectListClauseParser;

/**
 * Select list clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleSelectListClauseParser extends SelectListClauseParser {
    
    public OracleSelectListClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBeforeSelectItem() {
        return new Keyword[] {OracleKeyword.CONNECT_BY_ROOT};
    }
}
