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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Raw SQL executor callback.
 */
@Slf4j
public final class RawSQLExecutorCallback implements ExecutorCallback<RawSQLExecutionUnit, ExecuteResult> {
    
    static {
        ShardingSphereServiceLoader.register(RawExecutorCallback.class);
    }
    
    @SuppressWarnings("rawtypes")
    private final Collection<RawExecutorCallback> callbacks;
    
    public RawSQLExecutorCallback() {
        callbacks = ShardingSphereServiceLoader.newServiceInstances(RawExecutorCallback.class);
        Preconditions.checkState(!callbacks.isEmpty(), "No raw executor callback implementation found.");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<ExecuteResult> execute(final Collection<RawSQLExecutionUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        return callbacks.iterator().next().execute(inputs, isTrunkThread, dataMap);
    }
}
