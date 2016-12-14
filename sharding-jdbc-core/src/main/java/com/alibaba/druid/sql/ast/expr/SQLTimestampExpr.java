package com.alibaba.druid.sql.ast.expr;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SQLTimestampExpr extends SQLExprImpl {
    
    private String literal;
    
    private String timeZone;
    
    private boolean withTimeZone;
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        visitor.visit(this);
        visitor.endVisit(this);
    }
    
    @Override
    public String toString() {
        return SQLUtils.toSQLString(this, null);
    }
}
