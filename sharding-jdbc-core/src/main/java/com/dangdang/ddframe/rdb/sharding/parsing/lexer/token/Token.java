package com.dangdang.ddframe.rdb.sharding.parsing.lexer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 词法标记.
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
