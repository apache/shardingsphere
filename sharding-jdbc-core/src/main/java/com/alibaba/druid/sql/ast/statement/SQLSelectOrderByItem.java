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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = {"expr", "collate", "type"})
public class SQLSelectOrderByItem extends SQLObjectImpl {
    
    private final SQLExpr expr;
    
    private String collate;
    
    private SQLOrderingSpecification type;
    
    private NullsOrderType nullsOrderType;

    public SQLSelectOrderByItem(final SQLExpr expr) {
        if (null != expr) {
            expr.setParent(this);
        }
        this.expr = expr;
    }
    
    @Getter
    @RequiredArgsConstructor
    public enum NullsOrderType {
        
        NullsFirst("NULLS FIRST"), 
        NullsLast("NULLS LAST");
        
        private final String text;
    }
}
