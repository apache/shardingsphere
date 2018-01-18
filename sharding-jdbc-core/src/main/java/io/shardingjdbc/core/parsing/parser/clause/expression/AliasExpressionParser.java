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
            Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.BITMAP, DefaultKeyword.NOSORT, DefaultKeyword.REVERSE, DefaultKeyword.COMPILE, DefaultKeyword.NEW, DefaultKeyword.ADVISE, 
            DefaultKeyword.AVG, DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.COUNT, DefaultKeyword.ROUND, DefaultKeyword.TRUNC, DefaultKeyword.LENGTH, 
            DefaultKeyword.CHAR_LENGTH, DefaultKeyword.SUBSTR, DefaultKeyword.INSTR, DefaultKeyword.INITCAP, DefaultKeyword.UPPER, DefaultKeyword.LOWER, DefaultKeyword.LTRIM, DefaultKeyword.RTRIM, 
            DefaultKeyword.TRANSLATE, DefaultKeyword.LPAD, DefaultKeyword.RPAD, DefaultKeyword.DECODE, DefaultKeyword.NVL, 
        };
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
            Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.SEQUENCE, DefaultKeyword.NO, 
            DefaultKeyword.AFTER, DefaultKeyword.BITMAP, DefaultKeyword.NOSORT, DefaultKeyword.REVERSE,
            DefaultKeyword.COMPILE, DefaultKeyword.ENABLE, DefaultKeyword.DISABLE, DefaultKeyword.NEW, DefaultKeyword.UNTIL, DefaultKeyword.ADVISE, DefaultKeyword.PASSWORD,
            DefaultKeyword.LOCAL, DefaultKeyword.GLOBAL, DefaultKeyword.STORAGE, DefaultKeyword.DATA, DefaultKeyword.TIME, DefaultKeyword.BOOLEAN, DefaultKeyword.GREATEST,
            DefaultKeyword.LEAST, DefaultKeyword.ROUND, DefaultKeyword.TRUNC, DefaultKeyword.POSITION, DefaultKeyword.LENGTH, DefaultKeyword.CHAR_LENGTH, DefaultKeyword.SUBSTRING,
            DefaultKeyword.SUBSTR, DefaultKeyword.INSTR, DefaultKeyword.INITCAP, DefaultKeyword.UPPER, DefaultKeyword.LOWER, DefaultKeyword.TRIM, 
        };
    }
    
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[0];
    }
}
