package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.expression;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLAliasExpressionParser extends AliasExpressionParser {
    
    public PostgreSQLAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForAlias() {
        return new TokenType[0];
    }
}
