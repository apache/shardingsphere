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

import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Iterator;

/**
 * Executor that wrap an Iterator to Executor interface.
 */
abstract class IteratorExecutor extends SingleExecutor {
    
    @Setter
    private Iterator<Row> inputRowIterator;
    
    protected IteratorExecutor(final Executor executor, final ExecContext execContext) {
        super(executor, execContext);
    }
    
    @Override
    protected void doInit() {
        inputRowIterator = initInputRowIterator();
    }
    
    @Override
    public boolean executeMove() {
        if (inputRowIterator == null) {
            throw new ShardingSphereException("Uninitizlized Iterator");
        }
        if (inputRowIterator.hasNext()) {
            replaceCurrent(inputRowIterator.next());
            return true;
        }
        return false;
    }
    
    /**
     * Wrap current <code>Executor</code> to an Iterator.
     * @return Iterator instance.
     */
    protected abstract Iterator<Row> initInputRowIterator();
}
