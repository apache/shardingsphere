package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.AliasClauseParser;

/**
 * Alias clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerAliasClauseParser extends AliasClauseParser {
    
    public SQLServerAliasClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForAlias() {
        return new TokenType[0];
    }
}
