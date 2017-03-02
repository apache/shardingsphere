package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Select SQL上下文.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class SelectSQLContext extends AbstractSQLContext {
    
    private boolean distinct;
    
    private boolean containStar;
    
    private int selectListLastPosition;
    
    private final List<SelectItemContext> itemContexts = new LinkedList<>();
    
    private final List<GroupByContext>  groupByContexts = new LinkedList<>();
    
    private final List<OrderByContext>  orderByContexts = new LinkedList<>();
    
    private LimitContext limitContext;
    
    private SelectSQLContext parent;
    
    private List<SelectSQLContext> children = new LinkedList<>();
    
    public SelectSQLContext(final String originalSQL) {
        super(originalSQL);
    }
    
    @Override
    public SQLStatementType getType() {
        return SQLStatementType.SELECT;
    }
}
