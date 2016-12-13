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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SQLJoinTableSource extends SQLTableSourceImpl {
    
    private SQLTableSource left;
    
    private JoinType joinType;
    
    private SQLTableSource right;
    
    private SQLExpr condition;
    
    private final List<SQLExpr> using = new ArrayList<>();
    
    public void setLeft(final SQLTableSource left) {
        if (null != left) {
            left.setParent(this);
        }
        this.left = left;
    }
    
    public void setRight(final SQLTableSource right) {
        if (null != right) {
            right.setParent(this);
        }
        this.right = right;
    }
    
    public void setCondition(final SQLExpr condition) {
        if (null != condition) {
            condition.setParent(this);
        }
        this.condition = condition;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, left);
            acceptChild(visitor, right);
            acceptChild(visitor, condition);
            acceptChild(visitor, using);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        left.output(buffer);
        buffer.append(' ');
        buffer.append(joinType.getName());
        buffer.append(' ');
        right.output(buffer);
        if (null != condition) {
            buffer.append(" ON ");
            condition.output(buffer);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public enum JoinType {
        
        COMMA(","), 
        JOIN("JOIN"), 
        INNER_JOIN("INNER JOIN"), 
        CROSS_JOIN("CROSS JOIN"), 
        NATURAL_JOIN("NATURAL JOIN"), 
        NATURAL_INNER_JOIN("NATURAL INNER JOIN"), 
        LEFT_OUTER_JOIN("LEFT JOIN"), 
        RIGHT_OUTER_JOIN("RIGHT JOIN"), 
        FULL_OUTER_JOIN("FULL JOIN"), 
        STRAIGHT_JOIN("STRAIGHT_JOIN"), 
        OUTER_APPLY("OUTER APPLY"), 
        CROSS_APPLY("CROSS APPLY");
        
        private final String name;
    }
}
