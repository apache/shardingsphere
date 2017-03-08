package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 聚合Select Item上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class AggregationSelectItemContext implements SelectItemContext {
    
    private final String innerExpression;
    
    private final Optional<String> alias;
    
    private final int index;
    
    private final AggregationColumn.AggregationType aggregationType;
    
    @Override
    public String getExpression() {
        return aggregationType.name() + innerExpression;
    }
}
