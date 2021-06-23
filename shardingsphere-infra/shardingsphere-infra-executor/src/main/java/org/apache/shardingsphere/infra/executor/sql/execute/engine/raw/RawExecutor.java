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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.List;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;

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
     * @param sqlStatementContext SQL statement context
     * @param callback raw SQL executor callback
     * @return execute results
     * @throws SQLException SQL exception
     */
    public Collection<ExecuteResult> execute(final ExecutionGroupContext<RawSQLExecutionUnit> executionGroupContext,
                                             final SQLStatementContext<?> sqlStatementContext,
                                             final RawSQLExecutorCallback callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(sqlStatementContext, executionGroupContext, props);
            // TODO Load query header for first query
            List<ExecuteResult> results = execute(executionGroupContext, (RawSQLExecutorCallback) null, callback);
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return CollectionUtils.isEmpty(results) || Objects.isNull(results.get(0)) ? Collections
                .singleton(new UpdateResult(0, 0L)) : results;
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
