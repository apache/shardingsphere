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
import com.alibaba.druid.sql.ast.statement.SQLInsertInto;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObject;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleErrorLoggingClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleReturningClause;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OracleMultiInsertStatement extends OracleStatementImpl {
    
    private SQLSelect subQuery;
    
    private Option option;
    
    private final List<Entry> entries = new ArrayList<>();
    
    private final List<SQLHint> hints = new ArrayList<>(1);
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, entries);
            acceptChild(visitor, subQuery);
        }
        visitor.endVisit(this);
    }
    
    public enum Option {
        ALL, FIRST
    }
    
    public interface Entry extends OracleSQLObject {
    }
    
    @Getter
    @Setter
    public static class ConditionalInsertClause extends OracleSQLObjectImpl implements Entry {
        
        private List<ConditionalInsertClauseItem> items = new ArrayList<>();
        
        private InsertIntoClause elseItem;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, items);
                acceptChild(visitor, elseItem);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class ConditionalInsertClauseItem extends OracleSQLObjectImpl {
        
        private SQLExpr when;
        
        private InsertIntoClause then;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, when);
                acceptChild(visitor, then);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class InsertIntoClause extends SQLInsertInto implements OracleSQLObject, Entry {
        
        private OracleReturningClause returning;
        
        private OracleErrorLoggingClause errorLogging;
        
        @Override
        protected void acceptInternal(final SQLASTVisitor visitor) {
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
}
