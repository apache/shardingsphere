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
import com.alibaba.druid.sql.ast.SQLHint;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleErrorLoggingClause;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OracleMergeStatement extends OracleStatementImpl {
    
    private SQLName into;
    
    private String alias;
    
    private SQLTableSource using;
    
    private SQLExpr on;
    
    private MergeUpdateClause updateClause;
    
    private MergeInsertClause insertClause;
    
    private OracleErrorLoggingClause errorLoggingClause;
    
    private final List<SQLHint> hints = new ArrayList<>();
    
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, into);
            acceptChild(visitor, using);
            acceptChild(visitor, on);
            acceptChild(visitor, updateClause);
            acceptChild(visitor, insertClause);
            acceptChild(visitor, errorLoggingClause);
        }
        visitor.endVisit(this);
    }
    
    @Getter
    @Setter
    public static class MergeUpdateClause extends OracleSQLObjectImpl {
        
        private List<SQLUpdateSetItem> items = new ArrayList<>();
        
        private SQLExpr where;
        
        private SQLExpr deleteWhere;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, items);
                acceptChild(visitor, where);
                acceptChild(visitor, deleteWhere);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class MergeInsertClause extends OracleSQLObjectImpl {
        
        private SQLExpr where;
        
        private final List<SQLExpr> columns = new ArrayList<>();
        
        private final List<SQLExpr> values = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, columns);
                acceptChild(visitor, columns);
                acceptChild(visitor, columns);
            }
            visitor.endVisit(this);
        }
    }
}
