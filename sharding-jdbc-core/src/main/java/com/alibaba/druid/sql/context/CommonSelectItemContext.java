package com.alibaba.druid.sql.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Select Item上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class CommonSelectItemContext implements SelectItemContext {
    
    private final String expression;
    
    private final String alias;
    
    private final int index;
    
    private final boolean star;
}
