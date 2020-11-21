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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.engine.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.update.ExecuteUpdateResult;
import org.apache.shardingsphere.infra.executor.sql.execute.resourced.jdbc.executor.ExecutorExceptionHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Raw Proxy executor.
 */
@RequiredArgsConstructor
public final class RawProxyExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final boolean serial;
    
    /**
     * Execute.
     *
     * @param executionGroups execution groups
     * @param callback raw SQL execute callback
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public Collection<ExecuteResult> execute(final Collection<ExecutionGroup<RawSQLExecuteUnit>> executionGroups, final RawSQLExecutorCallback callback) throws SQLException {
        // TODO Load query header for first query
        List<ExecuteResult> results = doExecute(executionGroups, null, callback);
        // TODO refresh metadata
        if (null == results || results.isEmpty() || null == results.get(0)) {
            return Collections.singleton(new ExecuteUpdateResult(0, 0L));
        }
        // CHECKSTYLE:OFF
        if (results.get(0) instanceof ExecuteUpdateResult) {
            // TODO refresh metadata
        }
        // CHECKSTYLE:ON
        return results;
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> doExecute(final Collection<ExecutionGroup<RawSQLExecuteUnit>> executionGroups, 
                                  final RawSQLExecutorCallback firstCallback, final RawSQLExecutorCallback callback) throws SQLException {
        try {
            return executorEngine.execute((Collection) executionGroups, firstCallback, callback, serial);
        } catch (final SQLException ex) {
            ExecutorExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
}
