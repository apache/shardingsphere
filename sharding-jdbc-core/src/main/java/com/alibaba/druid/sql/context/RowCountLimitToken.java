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
public final class RowCountLimitToken implements SQLToken {
    
    public static final String COUNT_NAME = "limit_count";
    
    private final int beginPosition;
    
    private final int rowCount;
}
