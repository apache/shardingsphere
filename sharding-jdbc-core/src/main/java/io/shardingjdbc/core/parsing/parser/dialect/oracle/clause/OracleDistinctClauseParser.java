package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.DistinctClauseParser;

/**
 * Distinct clause parser for Oracle.
 *
 * @author zhangliang
 */
public class OracleDistinctClauseParser extends DistinctClauseParser {
    
    public OracleDistinctClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[] {DefaultKeyword.UNIQUE};
    }
}
