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
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SQLExprTableSource extends SQLTableSourceImpl {
    
    private SQLExpr expr;
    
    public SQLExprTableSource() {
    }

    public SQLExprTableSource(final SQLExpr expr){
        this(expr, null);
    }
    
    public SQLExprTableSource(final SQLExpr expr, final String alias) {
        setExpr(expr);
        setAlias(alias);
    }
    
    public void setExpr(final SQLExpr expr) {
        if (null != expr) {
            expr.setParent(this);
        }
        this.expr = expr;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, expr);
        }
        visitor.endVisit(this);
    }

    @Override
    public void output(final StringBuffer buffer) {
        expr.output(buffer);
    }
}
