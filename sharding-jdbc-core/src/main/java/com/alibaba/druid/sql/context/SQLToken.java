package com.alibaba.druid.sql.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SQL语言标记对象.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SQLToken {
    
    private final int beginPosition;
    
    private final String originalLiterals;
}
