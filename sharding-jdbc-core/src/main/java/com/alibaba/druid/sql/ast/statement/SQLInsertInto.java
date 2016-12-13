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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class SQLInsertInto extends SQLObjectImpl {
    
    private SQLExprTableSource  tableSource;
    
    private ValuesClause values;
    
    private SQLSelect query;
    
    private final List<SQLExpr> columns = new ArrayList<>();
    
    public void setTableSource(final SQLExprTableSource tableSource) {
        if (null != tableSource) {
            tableSource.setParent(this);
        }
        this.tableSource = tableSource;
    }
    
    public String getAlias() {
        return tableSource.getAlias();
    }
    
    public void setAlias(final String alias) {
        tableSource.setAlias(alias);
    }
    
    public SQLName getTableName() {
        return (SQLName) tableSource.getExpr();
    }
    
    public void setTableName(final SQLName tableName) {
        setTableSource(new SQLExprTableSource(tableName));
    }
}
