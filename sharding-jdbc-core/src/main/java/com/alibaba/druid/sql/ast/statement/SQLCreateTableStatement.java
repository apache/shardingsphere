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

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SQLCreateTableStatement extends SQLStatementImpl implements SQLDDLStatement {
    
    private boolean ifNotExists;
    
    private Type type;
    
    private SQLExprTableSource tableSource;
    
    // for postgresql
    private SQLExprTableSource inherits;
    
    private final List<SQLTableElement> tableElementList = new ArrayList<>();
    
    public SQLCreateTableStatement(final String dbType){
        super(dbType);
    }
    
    public SQLName getName() {
        return null == tableSource ? null : (SQLName) tableSource.getExpr();
    }
    
    public void setName(SQLName name) {
        setTableSource(new SQLExprTableSource(name));
    }
    
    public void setTableSource(final SQLExprTableSource tableSource) {
        if (null != tableSource) {
            tableSource.setParent(this);
        }
        this.tableSource = tableSource;
    }
    
    public void setInherits(final SQLExprTableSource inherits) {
        if (null != inherits) {
            inherits.setParent(this);
        }
        this.inherits = inherits;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, tableSource);
            acceptChild(visitor, tableElementList);
            acceptChild(visitor, inherits);
        }
        visitor.endVisit(this);
    }
    
    public enum Type {
        GLOBAL_TEMPORARY, LOCAL_TEMPORARY
    }
}
