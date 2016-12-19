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

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerObject;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.util.JdbcConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SQLServerInsertStatement extends SQLInsertStatement implements SQLServerObject {
    
    private boolean defaultValues;
    
    private final List<ValuesClause> valuesList = new ArrayList<>();
    
    public ValuesClause getValues() {
        return valuesList.isEmpty() ? null : valuesList.get(0);
    }
    
    public void setValues(final ValuesClause values) {
        if (valuesList.isEmpty()) {
            valuesList.add(values);
        } else {
            valuesList.set(0, values);
        }
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        this.accept0((SQLServerASTVisitor) visitor);
    }
    
    @Override
    public void accept0(final SQLServerASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getTableSource());
            acceptChild(visitor, getColumns());
            acceptChild(visitor, getValuesList());
            acceptChild(visitor, getQuery());
        }
        visitor.endVisit(this);
    }
    
    @Override
    public String toString() {
        return SQLUtils.toSQLString(this, JdbcConstants.SQL_SERVER);
    }
}
