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
public class SQLTruncateStatement extends SQLStatementImpl {
    
    private final List<SQLExprTableSource> tableSources = new ArrayList<>(2);
    
    private boolean purgeSnapshotLog;
    
    private boolean only;
    
    private Boolean restartIdentity;
    
    private Boolean cascade;
    
    public SQLTruncateStatement(final String dbType) {
        super (dbType);
    }
    
    public void addTableSource(final SQLName name) {
        SQLExprTableSource tableSource = new SQLExprTableSource(name);
        tableSource.setParent(this);
        tableSources.add(tableSource);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, tableSources);
        }
        visitor.endVisit(this);
    }
}
