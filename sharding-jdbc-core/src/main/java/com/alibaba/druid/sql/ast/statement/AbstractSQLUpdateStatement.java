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

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class AbstractSQLUpdateStatement extends SQLObjectImpl implements SQLStatement {
    
    private SQLExpr where;
    
    private SQLTableSource tableSource;
    
    private final List<SQLUpdateSetItem> items = new ArrayList<>();
    
    private final List<String> identifiersBetweenUpdateAndTable = new ArrayList<>();
    
    private final List<String> appendices = new ArrayList<>();
    
    public SQLName getTableName() {
        return tableSource instanceof SQLExprTableSource ? (SQLName) ((SQLExprTableSource) tableSource).getExpr() : null;
    }
    
    public void setTableSource(final SQLTableSource tableSource) {
        if (null != tableSource) {
            tableSource.setParent(this);
        }
        this.tableSource = tableSource;
    }
    
    public void setWhere(final SQLExpr where) {
        if (null != where) {
            where.setParent(this);
        }
        this.where = where;
    }
    
    public void addItem(final SQLUpdateSetItem item) {
        item.setParent(this);
        items.add(item);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, tableSource);
            acceptChild(visitor, items);
            acceptChild(visitor, where);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public String toString() {
        return SQLUtils.toSQLString(this, getDbType());
    }
}
