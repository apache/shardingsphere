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

package org.apache.shardingsphere.proxy.backend.communication.vertx;

import io.vertx.core.Future;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.ReactiveProxySQLExecutor;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;

import java.sql.SQLException;
import java.util.List;

/**
 * Vert.x database communication engine.
 */
public final class VertxDatabaseCommunicationEngine extends DatabaseCommunicationEngine<Future<ResponseHeader>> {
    
    private final ReactiveProxySQLExecutor reactiveProxySQLExecutor;
    
    public VertxDatabaseCommunicationEngine(final ShardingSphereMetaData metaData, final LogicSQL logicSQL, final VertxBackendConnection vertxBackendConnection) {
        super("Vert.x", metaData, logicSQL, vertxBackendConnection);
        reactiveProxySQLExecutor = new ReactiveProxySQLExecutor(vertxBackendConnection);
    }
    
    /**
     * Execute future.
     *
     * @return Future of response
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Future<ResponseHeader> execute() {
        try {
            ExecutionContext executionContext = getKernelProcessor()
                    .generateExecutionContext(getLogicSQL(), getMetaData(), ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps());
            if (executionContext.getRouteContext().isFederated()) {
                return Future.failedFuture(new UnsupportedOperationException("Executing federated query by Vert.x is not supported yet."));
            }
            if (executionContext.getExecutionUnits().isEmpty()) {
                return Future.succeededFuture(new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement()));
            }
            reactiveProxySQLExecutor.checkExecutePrerequisites(executionContext);
            checkLockedSchema(executionContext);
            return reactiveProxySQLExecutor.execute(executionContext).compose(result -> {
                try {
                    refreshMetaData(executionContext);
                    ExecuteResult executeResultSample = result.iterator().next();
                    return Future.succeededFuture(executeResultSample instanceof QueryResult
                            ? processExecuteQuery(executionContext, (List) result, (QueryResult) executeResultSample)
                            : processExecuteUpdate(executionContext, (List) result));
                } catch (final SQLException ex) {
                    return Future.failedFuture(ex);
                }
            });
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return Future.failedFuture(ex);
        }
    }
}
