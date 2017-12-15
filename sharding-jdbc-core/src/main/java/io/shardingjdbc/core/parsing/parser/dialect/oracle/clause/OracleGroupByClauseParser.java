package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.GroupByClauseParser;

/**
 * Group by clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleGroupByClauseParser extends GroupByClauseParser {
    
    public OracleGroupByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getUnsupportedKeywordBeforeGroupByItem() {
        return new Keyword[] {OracleKeyword.ROLLUP, OracleKeyword.CUBE, OracleKeyword.GROUPING};
    }
}
