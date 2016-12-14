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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class SQLMethodInvokeExpr extends SQLExprImpl implements Serializable {
    
    private static final long serialVersionUID = 2902078604214967583L;
    
    private final String methodName;
    
    private final SQLExpr owner;
    
    private final List<SQLExpr> parameters = new ArrayList<>();
    
    public SQLMethodInvokeExpr(final String methodName) {
        this.methodName = methodName;
        owner = null;
    }
    
    public SQLMethodInvokeExpr(final String methodName, final SQLExpr owner) {
        this.methodName = methodName;
        if (null != owner) {
            owner.setParent(this);
        }
        this.owner = owner;
    }
    
    public void addParameter(final SQLExpr param) {
        if (null != param) {
            param.setParent(this);
        }
        parameters.add(param);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, owner);
            acceptChild(visitor, parameters);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        if (null != owner) {
            owner.output(buffer);
            buffer.append(".");
        }
        buffer.append(methodName);
        buffer.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i != 0) {
                buffer.append(", ");
            }
            parameters.get(i).output(buffer);
        }
        buffer.append(")");
    }
}
