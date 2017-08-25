package com.dangdang.ddframe.rdb.sharding.parsing.lexer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Token.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class Token {
    
    private final TokenType type;
    
    private final String literals;
    
    private final int endPosition;
}
