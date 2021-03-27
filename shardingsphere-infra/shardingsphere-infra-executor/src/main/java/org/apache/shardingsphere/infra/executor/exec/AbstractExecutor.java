/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.executor.exec;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Iterator;

/**
 * Base class for {@link Executor} of rational operator.
 */
public abstract class AbstractExecutor implements Executor {
    
    /**
     * context for current {@link Executor}.
     */
    @Getter(AccessLevel.PROTECTED)
    private final ExecContext execContext;
    
    private volatile boolean inited;
    
    private Row current;
    
    public AbstractExecutor(final ExecContext execContext) {
        this.execContext = execContext;
    }
    
    /**
     * move to next row.
     * @return true, if the next row exist, or false
     */
    @Override
    public final boolean moveNext() {
        init();
        return executeMove();
    }
    
    /**
     * default implementation for {@link Executor#reset()}.
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * default implementation for {@link Executor#close()}.
     */
    @Override
    public void close() {
        
    }
    
    /**
     * Get current Row.
     * @return current Row.
     */
    @Override
    public Row current() {
        return current;
    }
    
    /**
     * replace the current row reference, so sub-class do not need to override {@link #current()} method.
     * @param row the row to replace current.
     */
    protected void replaceCurrent(final Row row) {
        current = row;
    }
    
    /**
     * execute initialization for this executor.
     */
    protected abstract void executeInit();
    
    /**
     * move to the next row of this executor.
     * @return true if the next row exist, else false
     */
    protected abstract boolean executeMove();
    
    /**
     * Decorate this Executor to Iterator.
     * @return Iterator.
     */
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return AbstractExecutor.this.moveNext();
            }
    
            @Override
            public Row next() {
                return AbstractExecutor.this.current();
            }
        };
    }
    
    @Override
    public final boolean isInited() {
        return inited;
    }
    
    @Override
    public final void init() {
        if (inited) {
            return;
        }
        synchronized (this) {
            if (inited) {
                return;
            }
            executeInit();
            inited = true;
        }
       
    }
}
