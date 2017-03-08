package com.alibaba.druid.sql.context;

import com.google.common.base.Optional;
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
    
    private final Optional<String> alias;
    
    private final int index;
    
    private final boolean star;
}
