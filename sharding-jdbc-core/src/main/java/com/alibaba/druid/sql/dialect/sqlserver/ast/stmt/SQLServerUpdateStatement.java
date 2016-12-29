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

import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerOutput;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.util.JdbcConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SQLServerUpdateStatement extends AbstractSQLUpdateStatement implements SQLServerStatement {
    
    private SQLServerTop top;
    
    private SQLServerOutput output;
    
    private SQLTableSource from;
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        accept0((SQLServerASTVisitor) visitor);
    }
    
    @Override
    public void accept0(final SQLServerASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, top);
            acceptChild(visitor, getTableSource());
            acceptChild(visitor, getItems());
            acceptChild(visitor, output);
            acceptChild(visitor, from);
            acceptChild(visitor, getWhere());
        }
        visitor.endVisit(this);
    }
    
    @Override
    public String getDbType() {
        return JdbcConstants.SQL_SERVER;
    }
}
