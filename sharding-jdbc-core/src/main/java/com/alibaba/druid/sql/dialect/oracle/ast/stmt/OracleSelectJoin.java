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
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.FlashbackQueryClause;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleSelectJoin extends SQLJoinTableSource implements OracleSelectTableSource {
    
    private OracleSelectPivotBase pivot;
    
    private FlashbackQueryClause  flashback;
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getLeft());
            acceptChild(visitor, getRight());
            acceptChild(visitor, getCondition());
            acceptChild(visitor, getUsing());
            acceptChild(visitor, flashback);
        }
        visitor.endVisit(this);
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        getLeft().output(buffer);
        buffer.append(getJoinType().getName());
        getRight().output(buffer);
        if (null != getCondition()) {
            buffer.append(" ON ");
            getCondition().output(buffer);
        }
        if (!getUsing().isEmpty()) {
            buffer.append(" USING (");
            int i = 0;
            for (int size = getUsing().size(); i < size; i++) {
                if (i != 0) {
                    buffer.append(", ");
                }
                getUsing().get(i).output(buffer);
            }
            buffer.append(")");
        }
    }
    
    @Override
    public String toString () {
        return SQLUtils.toOracleString(this);
    }
}
