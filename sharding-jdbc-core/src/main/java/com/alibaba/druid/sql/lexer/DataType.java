package com.alibaba.druid.sql.lexer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 数据类型标记.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum DataType implements Token {
    
    IDENTIFIER,
    LITERAL_INT,
    LITERAL_FLOAT,
    LITERAL_HEX,
    LITERAL_CHARS,
    LITERAL_NCHARS,
    LITERAL_ALIAS,
    BINARY_FLOAT,
    BINARY_DOUBLE,
    VARIANT,
    HINT,
    COMMENT,
    LINE_COMMENT,
    MULTI_LINE_COMMENT,
    ERROR,
    EOF,
}
