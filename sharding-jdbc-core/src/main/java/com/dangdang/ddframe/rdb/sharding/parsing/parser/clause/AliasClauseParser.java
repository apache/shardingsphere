package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Alias clause parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class AliasClauseParser implements SQLClauseParser {
    
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
        if (lexerEngine.equalAny(
                Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT)) {
            String result = SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
}
