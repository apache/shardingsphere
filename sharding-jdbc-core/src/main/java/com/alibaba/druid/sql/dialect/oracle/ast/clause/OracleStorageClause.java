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
package com.alibaba.druid.sql.dialect.oracle.ast.clause;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OracleStorageClause extends OracleSQLObjectImpl {
    
    private SQLExpr initial;
    
    private SQLExpr next;
    
    private SQLExpr minExtents;
    
    private SQLExpr maxExtents;
    
    private SQLExpr maxSize;
    
    private SQLExpr pctIncrease;
    
    private SQLExpr freeLists;
    
    private SQLExpr freeListGroups;
    
    private SQLExpr bufferPool;
    
    private SQLExpr objno;
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, initial);
            acceptChild(visitor, next);
            acceptChild(visitor, minExtents);
            acceptChild(visitor, maxExtents);
            acceptChild(visitor, maxSize);
            acceptChild(visitor, pctIncrease);
            acceptChild(visitor, freeLists);
            acceptChild(visitor, freeListGroups);
            acceptChild(visitor, bufferPool);
            acceptChild(visitor, objno);
        }
        visitor.endVisit(this);
    }
}
