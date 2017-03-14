package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 语言标记.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class Token {
    
    private final TokenType type;
    
    private final String literals;
    
    private final int beginPosition;
    
    public Token(final Tokenizer tokenizer) {
        this(tokenizer.getTokenType(), tokenizer.getLiterals(), tokenizer.getCurrentPosition());
    }
}
