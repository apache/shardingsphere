package com.alibaba.druid.sql.context;

/**
 * Update SQL上下文.
 *
 * @author zhangliang
 */
public final class UpdateSQLContext extends AbstractSQLContext {
    
    public UpdateSQLContext(final String originalSQL) {
        super(originalSQL);
    }
}
