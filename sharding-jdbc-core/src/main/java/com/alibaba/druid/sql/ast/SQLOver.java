package com.alibaba.druid.sql.ast;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class SQLOver extends SQLObjectImpl {
    
    private final List<SQLExpr> partitionBy = new ArrayList<>();
    
    private SQLOrderBy orderBy;
    
    public void setOrderBy(final SQLOrderBy orderBy) {
        if (null != orderBy) {
            orderBy.setParent(this);
        }
        this.orderBy = orderBy;
    }
}
