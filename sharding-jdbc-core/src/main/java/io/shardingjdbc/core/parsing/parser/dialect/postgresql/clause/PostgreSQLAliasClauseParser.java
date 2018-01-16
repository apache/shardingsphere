package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.AliasClauseParser;

/**
 * Alias clause parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLAliasClauseParser extends AliasClauseParser {
    
    public PostgreSQLAliasClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForAlias() {
        return new TokenType[0];
    }
}
