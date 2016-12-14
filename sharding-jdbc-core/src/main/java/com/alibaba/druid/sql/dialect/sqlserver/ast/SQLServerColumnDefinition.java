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
package com.alibaba.druid.sql.dialect.sqlserver.ast;

import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SQLServerColumnDefinition extends SQLColumnDefinition implements SQLServerObject {
    
    private Identity identity;
    
    public void setIdentity(final Identity identity) {
        if (null != identity) {
            identity.setParent(this);
        }
        this.identity = identity;
    }
    
    @Override
    protected void acceptInternal(SQLASTVisitor visitor) {
        accept0((SQLServerASTVisitor) visitor);
    }
    
    @Override
    public void accept0(final SQLServerASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getName());
            acceptChild(visitor, getDataType());
            acceptChild(visitor, getDefaultExpr());
            acceptChild(visitor, getConstraints());
            acceptChild(visitor, identity);
        }
        visitor.endVisit(this);
    }
    
    @Getter
    @Setter
    public static class Identity extends SQLServerObjectImpl {
        
        private Integer seed;
        
        private Integer increment;
        
        @Override
        public void accept0(final SQLServerASTVisitor visitor) {
            visitor.visit(this);
            visitor.endVisit(this);
        }
    }
}
