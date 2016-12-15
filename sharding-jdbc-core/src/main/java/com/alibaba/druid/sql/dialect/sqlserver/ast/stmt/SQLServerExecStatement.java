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
package com.alibaba.druid.sql.dialect.sqlserver.ast.stmt;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerObjectImpl;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerStatement;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SQLServerExecStatement extends SQLServerObjectImpl implements SQLServerStatement {
    
    private SQLName returnStatus;
    
    private SQLName moduleName;
    
    private final List<SQLServerParameter> parameters = new ArrayList<>();
    
    private String dbType;
    
    @Override
    public void accept0(final SQLServerASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.returnStatus);
            acceptChild(visitor, this.moduleName);
            acceptChild(visitor, this.parameters);
        }
        visitor.endVisit(this);
    }
    
    @Getter
    @Setter
    public static class SQLServerParameter extends SQLServerObjectImpl {
        
        private SQLExpr expr;
        
        private boolean type;//sql server 支持参数只有input 和 output 两种
        
        @Override
        public void accept0(final SQLServerASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, expr);
            }
            visitor.endVisit(this);
        }
    }
}
