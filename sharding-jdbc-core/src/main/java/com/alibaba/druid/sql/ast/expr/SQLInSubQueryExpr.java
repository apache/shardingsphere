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
package com.alibaba.druid.sql.ast.expr;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class SQLInSubQueryExpr extends SQLExprImpl implements Serializable {
    
    private static final long serialVersionUID = 1307332712437199415L;
    
    private boolean not;
    
    private SQLExpr expr;
    
    private SQLSelect subQuery;
    
    public void setSubQuery(final SQLSelect subQuery) {
        if (null != subQuery) {
            subQuery.setParent(this);
        }
        this.subQuery = subQuery;
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        this.subQuery.output(buffer);
    }
}
