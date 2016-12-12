package com.alibaba.druid.sql.ast;

import com.alibaba.druid.sql.visitor.SQLASTVisitor;
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
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, partitionBy);
            acceptChild(visitor, orderBy);
        }
        visitor.endVisit(this);
    }
}
