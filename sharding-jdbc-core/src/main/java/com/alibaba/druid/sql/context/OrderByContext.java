package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.ToString;

/**
 * 排序上下文.
 *
 * @author zhangliang
 */
@Getter
// TODO remove @ToString
@ToString
public final class OrderByContext {
    
    private final Optional<String> owner;
    
    private final Optional<String> name;
    
    private final Optional<Integer> index;
    
    private final OrderByColumn.OrderByType orderByType;
    
    public OrderByContext(final String name, final OrderByColumn.OrderByType orderByType) {
        this.owner = Optional.absent();
        this.name = Optional.of(name);
        index = Optional.absent();
        this.orderByType = orderByType;
    }
    
    public OrderByContext(final String owner, final String name, final OrderByColumn.OrderByType orderByType) {
        this.owner = Optional.of(owner);
        this.name = Optional.of(name);
        index = Optional.absent();
        this.orderByType = orderByType;
    }
    
    public OrderByContext(final int index, final OrderByColumn.OrderByType orderByType) {
        owner = Optional.absent();
        name = Optional.absent();
        this.index = Optional.of(index);
        this.orderByType = orderByType;
    }
}
