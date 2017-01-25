package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.GeneratedKeyContext;
import lombok.Getter;

/**
 * Insert SQL上下文.
 *
 * @author zhangliang
 */
@Getter
public final class InsertSQLContext extends AbstractSQLContext {
    
    private final GeneratedKeyContext generatedKeyContext;
    
    public InsertSQLContext(final String originalSQL) {
        super(originalSQL);
        generatedKeyContext = new GeneratedKeyContext();
    }
}
