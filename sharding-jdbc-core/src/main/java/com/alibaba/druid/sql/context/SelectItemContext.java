package com.alibaba.druid.sql.context;

/**
 * Select Item上下文接口.
 *
 * @author zhangliang
 */
public interface SelectItemContext {
    
    String getExpression();
    
    String getAlias();
    
    int getIndex();
}
