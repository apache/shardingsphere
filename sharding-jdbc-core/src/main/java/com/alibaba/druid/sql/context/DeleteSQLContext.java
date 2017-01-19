package com.alibaba.druid.sql.context;

import lombok.Getter;

/**
 * Delete SQL上下文.
 *
 * @author zhangliang
 */
@Getter
public final class DeleteSQLContext extends AbstractSQLContext {
    
    public DeleteSQLContext(final String originalSQL) {
        super(originalSQL);
    }
}
