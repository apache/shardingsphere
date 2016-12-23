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

package com.alibaba.druid.sql.ast;

import com.alibaba.druid.sql.visitor.SQLASTVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SQLObjectImpl implements SQLObject {
    
    private final Map<String, Object> attributes = new HashMap<>();
    
    private SQLObject parent;
    
    @Override
    public final void accept(final SQLASTVisitor visitor) {
        visitor.preVisit(this);
        acceptInternal(visitor);
        visitor.postVisit(this);
    }
    
    protected abstract void acceptInternal(final SQLASTVisitor visitor);
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    public void putAttribute(final String name, final Object value) {
        attributes.put(name, value);
    }
    
    @Override
    public SQLObject getParent() {
        return parent;
    }
    
    @Override
    public void setParent(final SQLObject parent) {
        this.parent = parent;
    }
    
    @Override
    public void output(final StringBuffer buffer) {
        buffer.append(super.toString());
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        output(buffer);
        return buffer.toString();
    }
    
    protected final void acceptChild(final SQLASTVisitor visitor, final List<? extends SQLObject> children) {
        if (null == children) {
            return;
        }
        for (SQLObject each : children) {
            acceptChild(visitor, each);
        }
    }
    
    protected final void acceptChild(final SQLASTVisitor visitor, final SQLObject child) {
        if (null == child) {
            return;
        }
        child.accept(visitor);
    }
}
