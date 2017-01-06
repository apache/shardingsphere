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
package com.alibaba.druid.sql.dialect.oracle.ast.stmt;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.FlashbackQueryClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.PartitionExtensionClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.SampleClause;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleSelectTableReference extends SQLExprTableSource implements OracleSelectTableSource {
    
    private boolean only;
    
    private OracleSelectPivotBase pivot;
    
    private PartitionExtensionClause partition;
    
    private SampleClause sampleClause;
    
    private FlashbackQueryClause flashback;
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getExpr());
            acceptChild(visitor, this.partition);
            acceptChild(visitor, this.sampleClause);
            acceptChild(visitor, this.pivot);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        if (only) {
            buffer.append("ONLY (");
            getExpr().output(buffer);
            buffer.append(")");
        } else {
            getExpr().output(buffer);
        }
        if (null != pivot) {
            buffer.append(" ");
            pivot.output(buffer);
        }
        if (!Strings.isNullOrEmpty(getAlias())) {
            buffer.append(getAlias());
        }
    }
    
    @Override
    public String toString () {
        return SQLUtils.toOracleString(this);
    }
}
