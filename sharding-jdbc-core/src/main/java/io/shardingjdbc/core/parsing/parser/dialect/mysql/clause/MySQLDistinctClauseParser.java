package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.DistinctClauseParser;

/**
 * Distinct clause parser for MySQL.
 *
 * @author zhangliang
 */
public class MySQLDistinctClauseParser extends DistinctClauseParser {
    
    public MySQLDistinctClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[] {MySQLKeyword.DISTINCTROW};
    }
}
