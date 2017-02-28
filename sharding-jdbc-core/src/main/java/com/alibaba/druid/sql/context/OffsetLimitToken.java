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
public final class OffsetLimitToken implements SQLToken {
    
    public static final String OFFSET_NAME = "limit_offset";
    
    private final int beginPosition;
    
    private final int offset;
}
