package com.alibaba.druid.sql.context;

import com.google.common.base.Optional;

/**
 * Select Item上下文接口.
 *
 * @author zhangliang
 */
public interface SelectItemContext {
    
    String getExpression();
    
    Optional<String> getAlias();
    
    int getIndex();
}
