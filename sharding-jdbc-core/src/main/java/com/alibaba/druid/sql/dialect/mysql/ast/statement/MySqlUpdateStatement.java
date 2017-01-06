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

package com.alibaba.druid.sql.dialect.mysql.ast.statement;

import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.util.JdbcConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySqlUpdateStatement extends AbstractSQLUpdateStatement implements MySqlStatement {
    
    private Limit limit;
    
    private SQLOrderBy orderBy;
    
    public void setLimit(final Limit limit) {
        if (limit != null) {
            limit.setParent(this);
        }
        this.limit = limit;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor instanceof MySqlASTVisitor) {
            accept0((MySqlASTVisitor) visitor);
        } else {
            throw new IllegalArgumentException("not support visitor type : " + visitor.getClass().getName());
        }
    }
    
    @Override
    public void accept0(final MySqlASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getTableSource());
            acceptChild(visitor, getItems());
            acceptChild(visitor, getWhere());
            acceptChild(visitor, orderBy);
            acceptChild(visitor, limit);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public String getDbType() {
        return JdbcConstants.MYSQL;
    }
}
