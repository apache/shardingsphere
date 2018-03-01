package io.shardingjdbc.core.rewrite.placeholder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Index placeholder for rewrite.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class IndexPlaceholder implements ShardingPlaceholder {
    
    private final String logicIndexName;
    
    private final String logicTableName;
    
    @Override
    public String toString() {
        return logicIndexName;
    }
}
