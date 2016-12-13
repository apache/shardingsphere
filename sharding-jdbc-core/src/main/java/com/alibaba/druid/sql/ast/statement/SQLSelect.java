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
import com.alibaba.druid.sql.ast.SQLHint;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = {"withSubQuery", "query", "orderBy"})
public class SQLSelect extends SQLObjectImpl {
    
    private SQLWithSubqueryClause withSubQuery;
    
    private SQLSelectQuery query;
    
    private SQLOrderBy orderBy;
    
    private final List<SQLHint> hints = new ArrayList<>(2);
    
    public void setQuery(final SQLSelectQuery query) {
        if (null != query) {
            query.setParent(this);
        }
        this.query = query;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, query);
            acceptChild(visitor, orderBy);
            acceptChild(visitor, hints);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public String toString() {
        SQLObject parent = getParent();
        if (parent instanceof SQLStatement) {
            String dbType = ((SQLStatement) parent).getDbType();
            if (null != dbType) {
                return SQLUtils.toSQLString(this, dbType);
            }
        }
        return super.toString();
    }
}
