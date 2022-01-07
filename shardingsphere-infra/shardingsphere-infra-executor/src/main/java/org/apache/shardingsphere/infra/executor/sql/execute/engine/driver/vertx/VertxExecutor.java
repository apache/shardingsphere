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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx;

import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Vert.x executor.
 */
@RequiredArgsConstructor
public final class VertxExecutor {
    
    private final ExecutorEngine executorEngine;
    
    /**
     * Execute group context and return futures.
     *
     * @param executionGroupContext Vert.x execution group context
     * @param callback callback
     * @return futures of execute results
     */
    @SneakyThrows(SQLException.class)
    public List<Future<ExecuteResult>> execute(final ExecutionGroupContext<VertxExecutionUnit> executionGroupContext, final VertxExecutorCallback callback) {
        return executorEngine.execute(executionGroupContext, null, callback, true);
    }
}
