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
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOver;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = {"methodName", "option", "over", "arguments"})
public class SQLAggregateExpr extends SQLExprImpl implements Serializable {
    
    private static final long serialVersionUID = 6875909644887993393L;
    
    private String methodName;
    
    private SQLAggregateOption option;
    
    private SQLOver over;
    
    private SQLOrderBy withinGroup;
    
    private final List<SQLExpr> arguments = new ArrayList<>();
    
    public SQLAggregateExpr(final String methodName){
        this.methodName = methodName;
    }
    
    public SQLAggregateExpr(final String methodName, final SQLAggregateOption option){
        this.methodName = methodName;
        this.option = option;
    }
    
    public void setWithinGroup(final SQLOrderBy withinGroup) {
        if (null != withinGroup) {
            withinGroup.setParent(this);
        }
        this.withinGroup = withinGroup;
    }
}
