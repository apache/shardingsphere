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
     * Parse alias for select item.
     * 
     * @return alias for select item
     */
    public Optional<String> parseSelectItemAlias() {
        if (lexerEngine.skipIfEqual(DefaultKeyword.AS)) {
            return parseWithAs();
        }
        if (lexerEngine.equalAny(getDefaultAvailableKeywordsForSelectItemAlias()) || lexerEngine.equalAny(getCustomizedAvailableKeywordsForSelectItemAlias())) {
            return parseAlias();
        }
        return Optional.absent();
    }
    
    private Optional<String> parseWithAs() {
        if (lexerEngine.equalAny(Symbol.values())) {
            return Optional.absent();
        }
        return parseAlias();
    }
    
    private Optional<String> parseAlias() {
        String result = SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals());
        lexerEngine.nextToken();
        return Optional.of(result);
    }
    
    private TokenType[] getDefaultAvailableKeywordsForSelectItemAlias() {
        return new TokenType[] {
            Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.TABLESPACE, DefaultKeyword.FUNCTION, DefaultKeyword.SEQUENCE, DefaultKeyword.OF, DefaultKeyword.DO, 
            DefaultKeyword.NO, DefaultKeyword.TEMPORARY, DefaultKeyword.TEMP, DefaultKeyword.COMMENT, DefaultKeyword.AFTER, DefaultKeyword.INSTEAD, DefaultKeyword.ROW, 
            DefaultKeyword.STATEMENT, DefaultKeyword.EXECUTE, DefaultKeyword.BITMAP, DefaultKeyword.NOSORT, DefaultKeyword.REVERSE, DefaultKeyword.COMPILE, 
            DefaultKeyword.PASSWORD, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT, };
    }
    
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[0];
    }
    
    /**
     * Parse alias for table.
     *
     * @return alias for table
     */
    public Optional<String> parseTableAlias() {
        if (lexerEngine.skipIfEqual(DefaultKeyword.AS)) {
            return parseWithAs();
        }
        if (lexerEngine.equalAny(getDefaultAvailableKeywordsForTableAlias()) || lexerEngine.equalAny(getCustomizedAvailableKeywordsForTableAlias())) {
            return parseAlias();
        }
        return Optional.absent();
    }
    
    private TokenType[] getDefaultAvailableKeywordsForTableAlias() {
        return new TokenType[] {
            Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.TABLESPACE, DefaultKeyword.FUNCTION, DefaultKeyword.SEQUENCE, DefaultKeyword.OF, DefaultKeyword.DO,
            DefaultKeyword.NO, DefaultKeyword.TEMPORARY, DefaultKeyword.TEMP, DefaultKeyword.COMMENT, DefaultKeyword.AFTER, DefaultKeyword.INSTEAD, DefaultKeyword.ROW,
            DefaultKeyword.STATEMENT, DefaultKeyword.EXECUTE, DefaultKeyword.BITMAP, DefaultKeyword.NOSORT, DefaultKeyword.REVERSE, DefaultKeyword.COMPILE,
            DefaultKeyword.PASSWORD, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT, };
    }
    
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[0];
    }
}
