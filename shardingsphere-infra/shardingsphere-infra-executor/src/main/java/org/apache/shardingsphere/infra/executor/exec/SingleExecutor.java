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

/**
 * Abstract base class for <code>Executor</code> with a single input .
 *
 * <p>It is not required that single-input <code>Executor</code> use this
 * class as a base class. However, default implementations of methods make life
 * easier.
 */
public abstract class SingleExecutor extends AbstractExecutor {
    
    @Getter(AccessLevel.PROTECTED)
    private final Executor executor;
    
    public SingleExecutor(final Executor executor, final ExecContext execContext) {
        super(execContext);
        this.executor = executor;
    }
    
    @Override
    protected final void executeInit() {
        executor.init();
        doInit();
    }
    
    /**
     * do initialization for current <code>Executor</code> instance.
     */
    protected abstract void doInit();
    
    @Override
    public final void close() {
        if (executor != null) {
            executor.close();
        }
    }
}
