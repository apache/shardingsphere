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

    protected void acceptInternal(SQLASTVisitor visitor) {
        if (visitor instanceof OracleASTVisitor) {
            accept0((OracleASTVisitor) visitor);
        } else {
            super.acceptInternal(visitor);
        }
    }

    protected void accept0(OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getExpr());
        }

        visitor.endVisit(this);
    }

    public void output(StringBuffer buf) {
        getExpr().output(buf);
        if (SQLOrderingSpecification.ASC.equals(getType())) {
            buf.append(" ASC");
        } else if (SQLOrderingSpecification.DESC.equals(getType())) {
            buf.append(" DESC");
        }
        if (NullsOrderType.NullsFirst.equals(getNullsOrderType())) {
            buf.append(" NULLS FIRST");
        } else if (NullsOrderType.NullsLast.equals(getNullsOrderType())) {
            buf.append(" NULLS LAST");
        }
    }

}
