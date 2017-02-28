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
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.context.SelectItemContext;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(of = {"expr", "alias"})
public class SQLSelectItem extends SQLObjectImpl {
    
    private final SQLExpr expr;
    
    private final String alias;
    
    @Setter
    private SelectItemContext selectItemContext;
    
    public SQLSelectItem(final SQLExpr expr, final String alias) {
        this.expr = expr;
        this.alias = alias;
        if (null != expr) {
            expr.setParent(this);
        }
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
        if (!Strings.isNullOrEmpty(alias)) {
            buffer.append(" AS ");
            buffer.append(this.alias);
        }
    }
}
