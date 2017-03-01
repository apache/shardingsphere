package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;

/**
 * Update SQL上下文.
 *
 * @author zhangliang
 */
public final class UpdateSQLContext extends AbstractSQLContext {
    
    public UpdateSQLContext(final String originalSQL) {
        super(originalSQL);
    }
    
    @Override
    public SQLStatementType getType() {
        return SQLStatementType.UPDATE;
    }
}
