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

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObject;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleForeignKey extends SQLForeignKeyImpl implements OracleConstraint, OracleSQLObject {
    
    private OracleUsingIndexClause using;
    
    private SQLName exceptionsInto;
    
    private Initially initially;
    
    private Boolean deferrable;
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor instanceof OracleASTVisitor) {
            accept0((OracleASTVisitor) visitor);
            return;
        }
        super.accept(visitor);
    }
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.getName());
            acceptChild(visitor, this.getReferencedTableName());
            acceptChild(visitor, this.getReferencingColumns());
            acceptChild(visitor, this.getReferencedColumns());
            acceptChild(visitor, using);
            acceptChild(visitor, exceptionsInto);
        }
        visitor.endVisit(this);
    }
    
    public void setUsing(final OracleUsingIndexClause using) {
        if (null != using) {
            using.setParent(this);
        }
        this.using = using;
    }
}
