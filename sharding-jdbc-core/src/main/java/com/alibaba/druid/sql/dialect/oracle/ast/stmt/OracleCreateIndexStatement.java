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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLCreateIndexStatement;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.util.JdbcConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleCreateIndexStatement extends SQLCreateIndexStatement implements OracleDDLStatement {
    
    private boolean online;
    
    private boolean indexOnlyTopLevel ;
    
    private boolean noParallel;
    
    private SQLExpr parallel;
    
    private SQLName tablespace;
    
    private SQLExpr ptcfree;
    
    private SQLExpr pctused;
    
    private SQLExpr initrans;
    
    private SQLExpr maxtrans;
    
    private Boolean enable;
    
    private boolean computeStatistics;
    
    public OracleCreateIndexStatement() {
        super (JdbcConstants.ORACLE);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        accept0((OracleASTVisitor) visitor);
    }
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getName());
            acceptChild(visitor, getTable());
            acceptChild(visitor, getItems());
            acceptChild(visitor, getTablespace());
            acceptChild(visitor, parallel);
        }
        visitor.endVisit(this);
    }
}
