package com.alibaba.druid.sql.dialect.oracle.ast.clause;

import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause.Entry;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObject;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class OracleWithSubqueryEntry extends Entry implements OracleSQLObject {

    private SearchClause searchClause;
    private CycleClause  cycleClause;

    public CycleClause getCycleClause() {
        return cycleClause;
    }

    public void setCycleClause(CycleClause cycleClause) {
        this.cycleClause = cycleClause;
    }

    public SearchClause getSearchClause() {
        return searchClause;
    }

    public void setSearchClause(SearchClause searchClause) {
        this.searchClause = searchClause;
    }

    @Override
    public void accept0(OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getName());
            acceptChild(visitor, getColumns());
            acceptChild(visitor, getSubQuery());
            acceptChild(visitor, searchClause);
            acceptChild(visitor, cycleClause);
        }
        visitor.endVisit(this);
    }

    @Override
    protected void acceptInternal(SQLASTVisitor visitor) {
        this.accept0((OracleASTVisitor) visitor);
    }

}
