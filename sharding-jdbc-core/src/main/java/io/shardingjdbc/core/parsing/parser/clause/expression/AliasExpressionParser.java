package io.shardingjdbc.core.parsing.parser.clause.expression;

import com.google.common.base.Optional;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Literals;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.util.SQLUtil;
import lombok.RequiredArgsConstructor;

/**
 * Alias expression parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class AliasExpressionParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse alias.
     *
     * @return alias
     */
    public Optional<String> parse() {
        if (lexerEngine.skipIfEqual(DefaultKeyword.AS)) {
            if (lexerEngine.equalAny(Symbol.values())) {
                return Optional.absent();
            }
            String result = SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            return Optional.of(result);
        }
        if (lexerEngine.equalAny(getDefaultAvailableKeywordsForAlias()) || lexerEngine.equalAny(getCustomizedAvailableKeywordsForAlias())) {
            String result = SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
    
    private TokenType[] getDefaultAvailableKeywordsForAlias() {
        return new TokenType[] {
            Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.TABLESPACE, DefaultKeyword.FUNCTION, DefaultKeyword.SEQUENCE, DefaultKeyword.OF, DefaultKeyword.DO, 
            DefaultKeyword.NO, DefaultKeyword.TEMPORARY, DefaultKeyword.TEMP, DefaultKeyword.COMMENT, DefaultKeyword.AFTER, DefaultKeyword.INSTEAD, DefaultKeyword.ROW, 
            DefaultKeyword.STATEMENT, DefaultKeyword.EXECUTE, DefaultKeyword.BITMAP, DefaultKeyword.NOSORT, DefaultKeyword.REVERSE, DefaultKeyword.COMPILE, 
            DefaultKeyword.PASSWORD, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT, };
    }
    
    protected TokenType[] getCustomizedAvailableKeywordsForAlias() {
        return new TokenType[0];
    }
}
