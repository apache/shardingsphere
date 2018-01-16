package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.expression;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLAliasExpressionParser extends AliasExpressionParser {
    
    public MySQLAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[0];
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[] {DefaultKeyword.LENGTH};
    }
}
