package io.shardingjdbc.core.rewrite.placeholder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Table placeholder for rewrite.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class TablePlaceholder implements ShardingPlaceholder {
    
    private final String logicTableName;
    
    @Override
    public String toString() {
        return logicTableName;
    }
}
