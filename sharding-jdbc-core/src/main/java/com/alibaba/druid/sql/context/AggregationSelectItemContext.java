package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 聚合Select Item上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
// TODO remove @ToString
@ToString
public final class AggregationSelectItemContext implements SelectItemContext {
    
    private final String expression;
    
    private final String alias;
    
    private final int index;
    
    private final AggregationColumn.AggregationType aggregationType;
}
