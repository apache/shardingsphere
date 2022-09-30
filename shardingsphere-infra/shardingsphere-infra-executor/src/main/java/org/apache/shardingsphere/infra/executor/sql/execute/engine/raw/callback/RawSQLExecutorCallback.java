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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Raw SQL executor callback.
 */
public final class RawSQLExecutorCallback implements ExecutorCallback<RawSQLExecutionUnit, ExecuteResult> {
    
    @SuppressWarnings("rawtypes")
    private final Collection<RawExecutorCallback> callbacks;
    
    private final EventBusContext eventBusContext;
    
    public RawSQLExecutorCallback(final EventBusContext eventBusContext) {
        this.eventBusContext = eventBusContext;
        callbacks = RawExecutorCallbackFactory.getAllInstances();
        Preconditions.checkState(!callbacks.isEmpty(), "No raw executor callback implementation found");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<ExecuteResult> execute(final Collection<RawSQLExecutionUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        Collection<ExecuteResult> result = callbacks.iterator().next().execute(inputs, isTrunkThread, dataMap);
        if (dataMap.containsKey(ExecuteProcessConstants.EXECUTE_ID.name())) {
            String executionID = dataMap.get(ExecuteProcessConstants.EXECUTE_ID.name()).toString();
            for (RawSQLExecutionUnit each : inputs) {
                ExecuteProcessEngine.finishExecution(executionID, each, eventBusContext);
            }
        }
        return result;
    }
}
