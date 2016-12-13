package com.alibaba.druid.sql.ast.statement;

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SQLDropTriggerStatement extends SQLStatementImpl implements SQLDDLStatement {
    
    private SQLName name;
    
    public SQLDropTriggerStatement(final String dbType) {
        super (dbType);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
        }
        visitor.endVisit(this);
    }
}
