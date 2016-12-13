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

import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @Description: MySql cursor close statement
 * @author zz email:455910092@qq.com
 * @date 2015-9-14
 * @version V1.0
 */
@Getter
@Setter
public class SQLCloseStatement extends SQLStatementImpl {
    
    private String cursorName; 
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        visitor.visit(this);
        visitor.endVisit(this);
    }
}
