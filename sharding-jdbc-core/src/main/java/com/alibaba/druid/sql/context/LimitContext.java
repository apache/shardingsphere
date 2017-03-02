package com.alibaba.druid.sql.context;

import com.google.common.base.Optional;
import lombok.Getter;

/**
 * 分页上下文.
 *
 * @author zhangliang
 */
@Getter
public final class LimitContext {
    
    private final int rowCount;
    
    private final Optional<Integer> offset;
    
    private final int rowCountParameterIndex;
    
    private final int offsetParameterIndex;
    
    public LimitContext(final int rowCount, final int rowCountParameterIndex) {
        this.rowCount = rowCount;
        offset = Optional.absent();
        this.offsetParameterIndex = -1;
        this.rowCountParameterIndex = rowCountParameterIndex;
    }
    
    public LimitContext(final int offset, final int rowCount, final int offsetParameterIndex, final int rowCountParameterIndex) {
        this.offset = Optional.of(offset);
        this.rowCount = rowCount;
        this.offsetParameterIndex = offsetParameterIndex;
        this.rowCountParameterIndex = rowCountParameterIndex;
    }
}
