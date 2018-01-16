package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.expression;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleAliasExpressionParser extends AliasExpressionParser {
    
    public OracleAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForAlias() {
        return new TokenType[0];
    }
}
