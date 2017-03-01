package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
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
    
    @Override
    public SQLStatementType getType() {
        return SQLStatementType.DELETE;
    }
}
