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
package com.alibaba.druid.sql.dialect.postgresql.ast.stmt;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGSQLObject;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGSQLObjectImpl;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithClause;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PGSelectQueryBlock extends SQLSelectQueryBlock {
    
    private PGWithClause with;
    
    private PGLimit limit;
    
    private WindowClause window;
    
    private SQLOrderBy orderBy;
    
    private FetchClause fetch;
    
    private ForClause forClause;
    
    private IntoOption intoOption;
    
    private final List<SQLExpr> distinctOn = new ArrayList<>(2);

    public enum IntoOption {
        TEMPORARY, TEMP, UNLOGGED
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, with);
            acceptChild(visitor, distinctOn);
            acceptChild(visitor, getSelectList());
            acceptChild(visitor, getInto());
            acceptChild(visitor, getFrom());
            acceptChild(visitor, getWhere());
            acceptChild(visitor, getGroupBy());
            acceptChild(visitor, window);
            acceptChild(visitor, orderBy);
            acceptChild(visitor, limit);
            acceptChild(visitor, fetch);
            acceptChild(visitor, forClause);
        }
        visitor.endVisit(this);
    }
    
    public SQLExpr getOffset() {
        return null == limit ? null : limit.offset;
    }
    
    public void setOffset(final SQLExpr offset) {
        if (null == limit) {
            limit = new PGLimit();
            limit.setParent(this);
        }
        limit.setOffset(offset);
    }
    
    @Getter
    @Setter
    public static class WindowClause extends PGSQLObjectImpl {
        
        private SQLExpr name;
        
        private List<SQLExpr> definition = new ArrayList<>(2);
        
        @Override
        public void accept0(final PGASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, name);
                acceptChild(visitor, definition);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class FetchClause extends PGSQLObjectImpl {
        
        private Option  option;
        
        private SQLExpr count;

        @Override
        public void accept0(final PGASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, count);
            }
            visitor.endVisit(this);
        }
        
        public enum Option {
            FIRST, NEXT
        }
    }
    
    @Getter
    @Setter
    public static class ForClause extends PGSQLObjectImpl {
        
        private List<SQLExpr> of = new ArrayList<>(2);
        
        private boolean noWait;
        
        private Option option;
        
        @Override
        public void accept0(final PGASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, of);
            }
            visitor.endVisit(this);
        }
        
        public enum Option {
            UPDATE, SHARE
        }
    }
    
    @Getter
    public static class PGLimit extends SQLObjectImpl implements SQLExpr, PGSQLObject {
        
        private SQLExpr rowCount;
        
        private SQLExpr offset;
        
        public void setRowCount(final SQLExpr rowCount) {
            if (null != rowCount) {
                rowCount.setParent(this);
            }
            this.rowCount = rowCount;
        }
        
        public void setOffset(final SQLExpr offset) {
            if (null != offset) {
                offset.setParent(this);
            }
            this.offset = offset;
        }
        
        @Override
        protected void acceptInternal(final SQLASTVisitor visitor) {
            accept0((PGASTVisitor) visitor);
        }
        
        @Override
        public void accept0(final PGASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, offset);
                acceptChild(visitor, rowCount);
            }
            visitor.endVisit(this);
        }
    }
}
