package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.AliasClauseParser;

/**
 * Alias clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleAliasClauseParser extends AliasClauseParser {
    
    public OracleAliasClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForAlias() {
        return new TokenType[0];
    }
}
