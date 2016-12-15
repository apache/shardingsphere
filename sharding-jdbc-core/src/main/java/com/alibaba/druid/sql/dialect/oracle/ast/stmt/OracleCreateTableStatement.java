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
import com.alibaba.druid.sql.ast.SQLPartitioningClause;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleLobStorageClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleStorageClause;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.util.JdbcConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleCreateTableStatement extends SQLCreateTableStatement implements OracleDDLStatement {
    
    private SQLName tablespace;
    
    private SQLSelect select;
    
    private boolean inMemoryMetadata;
    
    private boolean cursorSpecificSegment;
    
    // NOPARALLEL
    private Boolean parallel;
    
    private OracleStorageClause storage;
    
    private OracleLobStorageClause lobStorage;
    
    private boolean organizationIndex;
    
    private SQLExpr ptcfree;
    
    private SQLExpr pctused;
    
    private SQLExpr initrans;
    
    private SQLExpr maxtrans;
    
    private Boolean logging;
    
    private Boolean compress;
    
    private boolean onCommit;
    
    private boolean preserveRows;
    
    private Boolean cache;
    
    private SQLPartitioningClause partitioning;
    
    private DeferredSegmentCreation deferredSegmentCreation;
    
    public OracleCreateTableStatement() {
        super (JdbcConstants.ORACLE);
    }
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            this.acceptChild(visitor, getTableSource());
            this.acceptChild(visitor, getTableElementList());
            this.acceptChild(visitor, tablespace);
            this.acceptChild(visitor, select);
            this.acceptChild(visitor, storage);
            this.acceptChild(visitor, partitioning);
        }
        visitor.endVisit(this);
    }
    
    public enum DeferredSegmentCreation {
        
        IMMEDIATE, DEFERRED
    }
}
