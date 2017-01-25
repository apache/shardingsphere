package com.alibaba.druid.sql.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 表语言标记对象.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class TableToken implements SQLToken {
    
    private final int beginPosition;
    
    private final String originalLiterals;
    
    private final String tableName;
}
