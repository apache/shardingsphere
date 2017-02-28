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
package com.alibaba.druid.sql.ast.expr;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode(of = {"left", "right", "operator"})
public class SQLBinaryOpExpr extends SQLExprImpl implements Serializable {
    
    private static final long serialVersionUID = 4108953668433055176L;
    
    private SQLExpr left;
    
    private SQLExpr right;
    
    private SQLBinaryOperator operator;
    
    private String dbType;
    
    @Setter
    private int rightStartPosition;
    
    public SQLBinaryOpExpr(final SQLExpr left, final SQLBinaryOperator operator, final SQLExpr right, final String dbType) {
        if (null != left) {
            left.setParent(this);
        }
        this.left = left;
        if (null != right) {
            right.setParent(this);
        }
        this.right = right;
        this.operator = operator;
        this.dbType = dbType;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, left);
            acceptChild(visitor, right);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public String toString() {
        return SQLUtils.toSQLString(this, dbType);
    }
}
