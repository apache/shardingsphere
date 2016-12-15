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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class OracleCreateSequenceStatement extends OracleStatementImpl {
    
    private final SQLName name;
    
    private SQLExpr startWith;
    
    private SQLExpr incrementBy;
    
    private SQLExpr minValue;
    
    private SQLExpr maxValue;
    
    private boolean noMaxValue;
    
    private boolean noMinValue;
    
    private Boolean cycle;
    
    private Boolean cache;
    
    private Boolean order;
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
            acceptChild(visitor, startWith);
            acceptChild(visitor, incrementBy);
            acceptChild(visitor, minValue);
            acceptChild(visitor, maxValue);
        }
        visitor.endVisit(this);
    }
}
