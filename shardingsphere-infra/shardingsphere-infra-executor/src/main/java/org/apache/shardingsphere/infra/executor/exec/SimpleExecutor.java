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

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

/**
 * An simple Executor, if you need a mocked Executor, then this is.
 */
public final class SimpleExecutor extends AbstractExecutor {
    
    private final QueryResultMetaData metaData;
    
    private final Executor executor;
    
    private SimpleExecutor(final ExecContext execContext, final QueryResultMetaData metaData) {
        this(execContext, metaData, null);
    }
    
    private SimpleExecutor(final ExecContext execContext, final QueryResultMetaData metaData, final Executor executor) {
        super(execContext);
        this.metaData = metaData;
        this.executor = executor;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return metaData;
    }
    
    @Override
    public boolean executeMove() {
        if (executor != null) {
            return executor.moveNext();
        }
        return false;
    }
    
    @Override
    protected void executeInit() {
        if (executor != null) {
            executor.init();
        }
    }
    
    @Override
    public Row current() {
        if (executor != null) {
            return executor.current();
        }
        return null;
    }
    
    /**
     * Build an mocked Executor.
     * @param execContext execution context
     * @param metaData metadata
     * @return <code>SimpleExecutor</code>
     */
    public static SimpleExecutor empty(final ExecContext execContext, final QueryResultMetaData metaData) {
        return new SimpleExecutor(execContext, metaData);
    }
}
