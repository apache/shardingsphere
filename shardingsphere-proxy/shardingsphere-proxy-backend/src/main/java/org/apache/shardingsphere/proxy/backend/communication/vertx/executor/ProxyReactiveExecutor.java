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

package org.apache.shardingsphere.proxy.backend.communication.vertx.executor;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx.VertxExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx.VertxExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx.VertxExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reactive executor for ShardingSphere Proxy.
 */
@RequiredArgsConstructor
public final class ProxyReactiveExecutor {
    
    private final VertxExecutor vertxExecutor;
    
    /**
     * Execute.
     *
     * @param queryContext query context
     * @param executionGroupContext execution group context
     * @return execute results
     * @throws SQLException SQL exception
     */
    public Future<List<ExecuteResult>> execute(final QueryContext queryContext, final ExecutionGroupContext<VertxExecutionUnit> executionGroupContext) throws SQLException {
        EventBusContext eventBusContext = ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext();
        ExecuteProcessEngine.initialize(queryContext, executionGroupContext, eventBusContext);
        List<Future<ExecuteResult>> futures = vertxExecutor.execute(executionGroupContext, new VertxExecutorCallback());
        return CompositeFuture.all(new ArrayList<>(futures)).compose(compositeFuture -> {
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID(), eventBusContext);
            return Future.succeededFuture(compositeFuture.<ExecuteResult>list());
        }).eventually(unused -> {
            ExecuteProcessEngine.clean();
            return Future.succeededFuture();
        });
    }
}
