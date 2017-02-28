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
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleSelectQueryBlock extends SQLSelectQueryBlock {
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor instanceof OracleASTVisitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, getSelectList());
                acceptChild(visitor, getInto());
                acceptChild(visitor, getFrom());
                acceptChild(visitor, getWhere());
                acceptChild(visitor, getGroupBy());
            }
            visitor.endVisit(this);
            return;
        }
        super.acceptInternal(visitor);
    }
    
    @Override
    public String toString() {
        return SQLUtils.toOracleString(this);
    }
}
