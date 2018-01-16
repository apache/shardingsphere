package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.expression;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerAliasExpressionParser extends AliasExpressionParser {
    
    public SQLServerAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[0];
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[0];
    }
}
