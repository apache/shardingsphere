package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 分组上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
// TODO remove @ToString
@ToString
public final class GroupByContext {
    
    private final Optional<String> owner;
    
    private final String name;
    
    private final OrderByColumn.OrderByType orderByType;
}
