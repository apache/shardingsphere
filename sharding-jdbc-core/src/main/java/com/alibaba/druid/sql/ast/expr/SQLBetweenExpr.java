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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class SQLBetweenExpr extends SQLExprImpl implements Serializable {
    
    private static final long serialVersionUID = -6190097972762592083L;
    
    private SQLExpr testExpr;
    
    private boolean not;
    
    private SQLExpr beginExpr;
    
    private SQLExpr endExpr;
    
    public SQLBetweenExpr(final SQLExpr testExpr, final SQLExpr beginExpr, final SQLExpr endExpr) {
        setTestExpr(testExpr);
        setBeginExpr(beginExpr);
        setEndExpr(endExpr);
    }
    
    public SQLBetweenExpr(final SQLExpr testExpr, final boolean not, final SQLExpr beginExpr, final SQLExpr endExpr) {
        this(testExpr, beginExpr, endExpr);
        this.not = not;
    }
    
    public void setTestExpr(final SQLExpr testExpr) {
        if (null != testExpr) {
            testExpr.setParent(this);
        }
        this.testExpr = testExpr;
    }
    
    public void setBeginExpr(final SQLExpr beginExpr) {
        if (null != beginExpr) {
            beginExpr.setParent(this);
        }
        this.beginExpr = beginExpr;
    }
    
    public void setEndExpr(final SQLExpr endExpr) {
        if (null != endExpr) {
            endExpr.setParent(this);
        }
        this.endExpr = endExpr;
    }
}
