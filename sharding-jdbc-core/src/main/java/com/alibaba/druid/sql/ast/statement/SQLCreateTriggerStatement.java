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

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SQLCreateTriggerStatement extends SQLStatementImpl {
    
    private SQLName name;
    
    private boolean orReplace;
    
    private TriggerType triggerType;
    
    private SQLName on;
    
    private boolean forEachRow;
    
    private SQLStatement body;
    
    private final List<TriggerEvent> triggerEvents = new ArrayList<>();
    
    public SQLCreateTriggerStatement(final String dbType) {
        super (dbType);
    }
    
    public void setName(final SQLName name) {
        if (null != name) {
            name.setParent(this);
        }
        this.name = name;
    }
    
    public void setBody(final SQLStatement body) {
        if (null != body) {
            body.setParent(this);
        }
        this.body = body;
    }
    
    @Override
    protected void acceptInternal(final SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
            acceptChild(visitor, on);
            acceptChild(visitor, body);
        }
        visitor.endVisit(this);
    }
    
    public enum TriggerType {
        BEFORE, AFTER, INSTEAD_OF
    }
    
    public enum TriggerEvent {
        INSERT, UPDATE, DELETE
    }
}
