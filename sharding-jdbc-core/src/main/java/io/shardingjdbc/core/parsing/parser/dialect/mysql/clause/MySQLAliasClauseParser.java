package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.AliasClauseParser;

/**
 * Alias clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLAliasClauseParser extends AliasClauseParser {
    
    public MySQLAliasClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForAlias() {
        return new TokenType[0];
    }
}
