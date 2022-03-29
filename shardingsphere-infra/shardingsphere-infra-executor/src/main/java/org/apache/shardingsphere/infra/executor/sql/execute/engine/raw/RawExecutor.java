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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.raw;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Raw executor.
 */
@RequiredArgsConstructor
public final class RawExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final boolean serial;
    
    private final ConfigurationProperties props;
    
    /**
     * Execute.
     *
     * @param executionGroupContext execution group context
     * @param logicSQL logic SQL
     * @param callback raw SQL executor callback
     * @return execute results
     * @throws SQLException SQL exception
     */
    public List<ExecuteResult> execute(final ExecutionGroupContext<RawSQLExecutionUnit> executionGroupContext, final LogicSQL logicSQL, final RawSQLExecutorCallback callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(logicSQL, executionGroupContext, props);
            // TODO Load query header for first query
            List<ExecuteResult> results = execute(executionGroupContext, (RawSQLExecutorCallback) null, callback);
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return results.isEmpty() || Objects.isNull(results.get(0)) ? Collections
                .singletonList(new UpdateResult(0, 0L)) : results;
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> execute(final ExecutionGroupContext<RawSQLExecutionUnit> executionGroupContext,
                                final RawSQLExecutorCallback firstCallback, final RawSQLExecutorCallback callback) throws SQLException {
        try {
            return (List<T>) executorEngine.execute(executionGroupContext, firstCallback, callback, serial);
        } catch (final SQLException ex) {
            SQLExecutorExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
}
