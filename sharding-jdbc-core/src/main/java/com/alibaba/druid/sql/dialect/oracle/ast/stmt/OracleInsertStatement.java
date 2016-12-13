/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.dialect.oracle.ast.stmt;

import com.alibaba.druid.sql.ast.SQLHint;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleErrorLoggingClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleReturningClause;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class OracleInsertStatement extends SQLInsertStatement implements OracleStatement {

    private OracleReturningClause    returning;
    private OracleErrorLoggingClause errorLogging;
    private List<SQLHint>            hints = new ArrayList<>();

    public List<SQLHint> getHints() {
        return hints;
    }

    public void setHints(List<SQLHint> hints) {
        this.hints = hints;
    }

    public OracleReturningClause getReturning() {
        return returning;
    }

    public void setReturning(OracleReturningClause returning) {
        this.returning = returning;
    }

    public OracleErrorLoggingClause getErrorLogging() {
        return errorLogging;
    }

    public void setErrorLogging(OracleErrorLoggingClause errorLogging) {
        this.errorLogging = errorLogging;
    }

    @Override
    protected void acceptInternal(SQLASTVisitor visitor) {
        this.accept0((OracleASTVisitor) visitor);
    }
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getTableSource());
            acceptChild(visitor, getColumns());
            acceptChild(visitor, getValues());
            acceptChild(visitor, getQuery());
            acceptChild(visitor, returning);
            acceptChild(visitor, errorLogging);
        }
        visitor.endVisit(this);
    }
}
