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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * SQL executor.
 */
@RequiredArgsConstructor
public final class SQLExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final boolean serial;
    
    /**
     * Execute.
     *
     * @param executionGroups execution groups
     * @param callback SQL execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, final SQLExecutorCallback<T> callback) throws SQLException {
        return execute(executionGroups, null, callback);
    }
    
    /**
     * Execute.
     *
     * @param executionGroups execution groups
     * @param firstCallback first SQL execute callback
     * @param callback SQL execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, 
                               final SQLExecutorCallback<T> firstCallback, final SQLExecutorCallback<T> callback) throws SQLException {
        try {
            return executorEngine.execute(executionGroups, firstCallback, callback, serial);
        } catch (final SQLException ex) {
            ExecutorExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
}
