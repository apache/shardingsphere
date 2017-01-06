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
package com.alibaba.druid.sql.dialect.sqlserver.ast.expr;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerObjectImpl;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SQLServerObjectReferenceExpr extends SQLServerObjectImpl implements SQLServerExpr, SQLName {
    
    private String server;
    
    private String database;
    
    private String schema;
    
    public SQLServerObjectReferenceExpr(final SQLExpr owner) {
        if (owner instanceof SQLIdentifierExpr) {
            database = ((SQLIdentifierExpr) owner).getSimpleName();
        } else if (owner instanceof SQLPropertyExpr) {
            SQLPropertyExpr propExpr = (SQLPropertyExpr) owner;
            server = ((SQLIdentifierExpr) propExpr.getOwner()).getSimpleName();
            database = propExpr.getSimpleName();
        } else {
            throw new IllegalArgumentException(owner.toString());
        }
    }
    
    @Override
    public String getSimpleName() {
        if (null != schema) {
            return schema;
        }
        if (null != database) {
            return database;
        }
        return server;
    }
    
    @Override
    public void accept0(final SQLServerASTVisitor visitor) {
        visitor.visit(this);
        visitor.endVisit(this);
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        boolean flag = false;
        if (null != server) {
            buffer.append(server);
            flag = true;
        }
        if (flag) {
            buffer.append('.');
        }
        if (null != database) {
            buffer.append(database);
            flag = true;
        }
        if (flag) {
            buffer.append('.');
        }
        if (null != schema) {
            buffer.append(schema);
        }
    }
}
