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
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class OracleOrderByItem extends SQLSelectOrderByItem {
    
    public OracleOrderByItem(final SQLExpr expr){
        super(expr);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor instanceof OracleASTVisitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, getExpr());
            }
            visitor.endVisit(this);
        } else {
            super.acceptInternal(visitor);
        }
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        getExpr().output(buffer);
        if (SQLOrderingSpecification.ASC.equals(getType())) {
            buffer.append(" ASC");
        } else if (SQLOrderingSpecification.DESC.equals(getType())) {
            buffer.append(" DESC");
        }
        if (NullsOrderType.NullsFirst.equals(getNullsOrderType())) {
            buffer.append(" NULLS FIRST");
        } else if (NullsOrderType.NullsLast.equals(getNullsOrderType())) {
            buffer.append(" NULLS LAST");
        }
    }
}
