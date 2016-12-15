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
package com.alibaba.druid.sql.dialect.sqlserver.ast;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class SQLServerSelect extends SQLSelect implements SQLServerObject {
    
    private boolean forBrowse;
    
    private SQLExpr rowCount;
    
    private SQLExpr offset;
    
    private final List<String> forXmlOptions = new ArrayList<>(4);
    
    public void setRowCount(final SQLExpr rowCount) {
        if (null != rowCount) {
            rowCount.setParent(this);
        }
        this.rowCount = rowCount;
    }
    
    public void setOffset(final SQLExpr offset) {
        if (null != offset) {
            offset.setParent(this);
        }
        this.offset = offset;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        accept0((SQLServerASTVisitor) visitor);
    }
    
    @Override
    public void accept0(final SQLServerASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getQuery());
            acceptChild(visitor, getOrderBy());
            acceptChild(visitor, getHints());
            acceptChild(visitor, offset);
            acceptChild(visitor, rowCount);
        }
        visitor.endVisit(this);
    }
}
