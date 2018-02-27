package io.shardingjdbc.core.rewrite.placeholder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Schema placeholder for rewrite.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SchemaPlaceholder implements ShardingPlaceholder {
    
    private final String logicSchemaName;
    
    private final String logicTableName;
    
    @Override
    public String toString() {
        return logicSchemaName;
    }
}
