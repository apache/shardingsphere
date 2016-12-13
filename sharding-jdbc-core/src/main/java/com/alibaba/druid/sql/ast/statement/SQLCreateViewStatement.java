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
package com.alibaba.druid.sql.ast.statement;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLLiteralExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SQLCreateViewStatement extends SQLStatementImpl implements SQLDDLStatement {
    
    private boolean orReplace;
    
    private SQLName name;
    
    private SQLSelect subQuery;
    
    private boolean ifNotExists;
    
    private Level with;
    
    private SQLLiteralExpr comment;
    
    private final List<Column> columns = new ArrayList<>();

    public SQLCreateViewStatement(final String dbType){
        super(dbType);
    }
    
    public void setComment(final SQLLiteralExpr comment) {
        if (null != comment) {
            comment.setParent(this);
        }
        this.comment = comment;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
            acceptChild(visitor, columns);
            acceptChild(visitor, comment);
            acceptChild(visitor, subQuery);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        buffer.append("CREATE VIEW ");
        name.output(buffer);
        if (!columns.isEmpty()) {
            buffer.append(" (");
            for (int i = 0, size = columns.size(); i < size; ++i) {
                if (i != 0) {
                    buffer.append(", ");
                }
                columns.get(i).output(buffer);
            }
            buffer.append(")");
        }
        buffer.append(" AS ");
        subQuery.output(buffer);
        if (null != with) {
            buffer.append(" WITH ");
            buffer.append(with.name());
        }
    }
    
    public enum Level {
        CASCADED, LOCAL
    }
    
    @Getter
    public static class Column extends SQLObjectImpl {
        
        private SQLExpr expr;
        
        private SQLCharExpr comment;
        
        public void setExpr(final SQLExpr expr) {
            if (null != expr) {
                expr.setParent(this);
            }
            this.expr = expr;
        }
        
        public void setComment(final SQLCharExpr comment) {
            if (null != comment) {
                comment.setParent(this);
            }
            this.comment = comment;
        }
        
        @Override
        protected void acceptInternal(final SQLASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, expr);
                acceptChild(visitor, comment);
            }
        }
    }
}
