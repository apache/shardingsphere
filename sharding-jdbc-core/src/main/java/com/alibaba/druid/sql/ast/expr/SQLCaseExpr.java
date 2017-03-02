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
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class SQLCaseExpr extends SQLExprImpl implements Serializable {
    
    private static final long serialVersionUID = 8764874646914339226L;
    
    private SQLExpr valueExpr;
    
    private SQLExpr elseExpr;
    
    private final List<Item> items = new ArrayList<>();
    
    public void setValueExpr(final SQLExpr valueExpr) {
        if (null != valueExpr) {
            valueExpr.setParent(this);
        }
        this.valueExpr = valueExpr;
    }
    
    public void setElseExpr(final SQLExpr elseExpr) {
        if (null != elseExpr) {
            elseExpr.setParent(this);
        }
        this.elseExpr = elseExpr;
    }
    
    public void addItem(final Item item) {
        if (null != item) {
            item.setParent(this);
            items.add(item);
        }
    }
    
    @Getter
    @EqualsAndHashCode
    public static class Item extends SQLObjectImpl implements Serializable {
        
        private static final long serialVersionUID = 6037220586116903111L;
        
        private final SQLExpr conditionExpr;
        
        private final SQLExpr valueExpr;
        
        public Item(final SQLExpr conditionExpr, final SQLExpr valueExpr) {
            if (null != conditionExpr) {
                conditionExpr.setParent(this);
            }
            this.conditionExpr = conditionExpr;
            if (null != valueExpr) {
                valueExpr.setParent(this);
            }
            this.valueExpr = valueExpr;
        }
    }
}
