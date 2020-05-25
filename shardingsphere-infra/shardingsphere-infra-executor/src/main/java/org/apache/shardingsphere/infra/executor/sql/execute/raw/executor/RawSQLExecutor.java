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

package org.apache.shardingsphere.infra.executor.sql.execute.raw.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.unit.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.callback.RawSQLExecutorCallback;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Raw SQL executor.
 */
@RequiredArgsConstructor
public final class RawSQLExecutor {
    
    private final ExecutorKernel executorKernel;
    
    private final boolean serial;
    
    /**
     * Execute query.
     *
     * @param inputGroups input groups
     * @param callback SQL execute callback
     * @return Query results
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final Collection<InputGroup<RawSQLExecuteUnit>> inputGroups, final RawSQLExecutorCallback<QueryResult> callback) throws SQLException {
        return doExecute(inputGroups, null, callback);
    }
    
    /**
     * Execute update.
     *
     * @param inputGroups input groups
     * @param callback SQL execute callback
     * @return update count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final Collection<InputGroup<RawSQLExecuteUnit>> inputGroups, final RawSQLExecutorCallback<Integer> callback) throws SQLException {
        List<Integer> results = doExecute(inputGroups, null, callback);
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
     * @param inputGroups input groups
     * @param callback SQL execute callback
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final Collection<InputGroup<RawSQLExecuteUnit>> inputGroups, final RawSQLExecutorCallback<Boolean> callback) throws SQLException {
        List<Boolean> results = doExecute(inputGroups, null, callback);
        // TODO refresh metadata
        if (null == results || results.isEmpty() || null == results.get(0)) {
            return false;
        }
        return results.get(0);
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> doExecute(final Collection<InputGroup<RawSQLExecuteUnit>> inputGroups,
                                   final RawSQLExecutorCallback<T> firstCallback, final RawSQLExecutorCallback<T> callback) throws SQLException {
        try {
            return executorKernel.execute((Collection) inputGroups, firstCallback, callback, serial);
        } catch (final SQLException ex) {
            ExecutorExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
}
