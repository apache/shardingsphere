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
public class SQLCreateIndexStatement extends SQLStatementImpl implements SQLDDLStatement {
    
    private SQLName name;
    
    private SQLTableSource table;
    
    private String type;
    
    private final List<SQLSelectOrderByItem> items = new ArrayList<>();
    
    public SQLCreateIndexStatement(final String dbType){
        super (dbType);
    }
    
    public void setTable(final SQLName table) {
        this.table = new SQLExprTableSource(table);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
            acceptChild(visitor, table);
            acceptChild(visitor, items);
        }
        visitor.endVisit(this);
    }
}
