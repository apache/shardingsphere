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
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SQLWithSubqueryClause extends SQLObjectImpl {
    
    private Boolean recursive;
    
    private final List<Entry> entries = new ArrayList<>();
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, entries);
        }
        visitor.endVisit(this);
    }
    
    @Getter
    @Setter
    public static class Entry extends SQLObjectImpl {
        
        private SQLIdentifierExpr name;
        
        private SQLSelect subQuery;
        
        private final List<SQLName> columns = new ArrayList<>();
        
        @Override
        protected void acceptInternal(final SQLASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, name);
                acceptChild(visitor, columns);
                acceptChild(visitor, subQuery);
            }
            visitor.endVisit(this);
        }
    }
}
