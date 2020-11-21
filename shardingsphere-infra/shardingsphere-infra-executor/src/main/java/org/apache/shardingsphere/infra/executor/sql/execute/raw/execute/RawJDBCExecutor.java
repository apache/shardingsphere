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

package org.apache.shardingsphere.infra.executor.sql.execute.raw.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.update.ExecuteUpdateResult;
import org.apache.shardingsphere.infra.executor.sql.execute.driver.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.query.ExecuteQueryResult;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Raw JDBC executor.
 */
@RequiredArgsConstructor
public final class RawJDBCExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final boolean serial;
    
    /**
     * Execute query.
     *
     * @param executionGroups execution groups
     * @param callback raw SQL execute callback
     * @return Query results
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final Collection<ExecutionGroup<RawSQLExecuteUnit>> executionGroups, final RawSQLExecutorCallback callback) throws SQLException {
        return doExecute(executionGroups, callback).stream().map(each -> ((ExecuteQueryResult) each).getQueryResult()).collect(Collectors.toList());
    }
    
    /**
     * Execute update.
     *
     * @param executionGroups execution groups
     * @param callback raw SQL execute callback
     * @return update count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final Collection<ExecutionGroup<RawSQLExecuteUnit>> executionGroups, final RawSQLExecutorCallback callback) throws SQLException {
        List<Integer> results = doExecute(executionGroups, callback).stream().map(each -> ((ExecuteUpdateResult) each).getUpdateCount()).collect(Collectors.toList());
        // TODO check is need to accumulate
        // TODO refresh metadata
        return accumulate(results);
    }
    
    private int accumulate(final List<Integer> results) {
        int result = 0;
        for (Integer each : results) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    /**
     * Execute.
     *
     * @param executionGroups execution groups
     * @param callback raw SQL execute callback
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final Collection<ExecutionGroup<RawSQLExecuteUnit>> executionGroups, final RawSQLExecutorCallback callback) throws SQLException {
        List<ExecuteResult> results = doExecute(executionGroups, callback);
        // TODO refresh metadata
        if (null == results || results.isEmpty() || null == results.get(0)) {
            return false;
        }
        return results.get(0) instanceof ExecuteQueryResult;
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> doExecute(final Collection<ExecutionGroup<RawSQLExecuteUnit>> executionGroups, final RawSQLExecutorCallback callback) throws SQLException {
        try {
            return executorEngine.execute((Collection) executionGroups, null, callback, serial);
        } catch (final SQLException ex) {
            ExecutorExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
}
