package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.expression;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
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
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[] {
            DefaultKeyword.WHILE, DefaultKeyword.FULLTEXT, DefaultKeyword.MODIFY, DefaultKeyword.IDENTIFIED, DefaultKeyword.USE, DefaultKeyword.LEAVE, DefaultKeyword.ITERATE, 
            DefaultKeyword.REPEAT, DefaultKeyword.OPEN, DefaultKeyword.LOOP, DefaultKeyword.VARCHAR2, DefaultKeyword.DATE, DefaultKeyword.BLOB, DefaultKeyword.XOR, DefaultKeyword.CONVERT,
            PostgreSQLKeyword.PLAIN, PostgreSQLKeyword.EXTENDED, PostgreSQLKeyword.MAIN,
        };
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[0];
    }
}
