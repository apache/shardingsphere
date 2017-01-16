package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Update SQL上下文.
 *
 * @author zhangliang
 */
@Getter
public final class UpdateSQLContext implements SQLContext {
    
    @Setter
    private Table table;
    
    private final Collection<ConditionContext> conditionContexts = new LinkedList<>();
    
    private final SQLBuilder sqlBuilder = new SQLBuilder();
    
    public void append(final String str) {
        try {
            sqlBuilder.append(str);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void appendToken(final String token) {
        sqlBuilder.appendToken(token);
    }
}
