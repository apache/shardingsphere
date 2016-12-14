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
package com.alibaba.druid.sql.dialect.mysql.ast.expr;

import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySqlCharExpr extends SQLCharExpr implements MySqlExpr {
    
    private String charset;
    
    private String collate;
    
    public MySqlCharExpr(final String text){
        super(text);
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        visitor.visit(this);
        visitor.endVisit(this);
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        if (null != charset) {
            buffer.append(charset);
            buffer.append(' ');
        }
        super.output(buffer);
        if (null != collate) {
            buffer.append(" COLLATE ");
            buffer.append(collate);
        }
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        output(buffer);
        return buffer.toString();
    }
}
