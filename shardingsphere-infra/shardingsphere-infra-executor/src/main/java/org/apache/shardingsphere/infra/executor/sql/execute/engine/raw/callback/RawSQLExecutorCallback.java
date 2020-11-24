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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Raw SQL executor callback.
 */
@Slf4j
public final class RawSQLExecutorCallback implements ExecutorCallback<RawSQLExecutionUnit, ExecuteResult> {
    
    static {
        ShardingSphereServiceLoader.register(RawExecutorCallback.class);
    }
    
    private final Collection<RawExecutorCallback> rawExecutorCallbacks;

    private final ShardingSphereMetaData shardingSphereMetaData;
    
    public RawSQLExecutorCallback(final ShardingSphereMetaData shardingSphereMetaData) {
        rawExecutorCallbacks = ShardingSphereServiceLoader.newServiceInstances(RawExecutorCallback.class);
        if (null == rawExecutorCallbacks || rawExecutorCallbacks.isEmpty()) {
            throw new ShardingSphereException("not found raw executor callback impl");
        }
        this.shardingSphereMetaData = shardingSphereMetaData;
    }
    
    @Override
    public Collection<ExecuteResult> execute(final Collection<RawSQLExecutionUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        Map<String, Object> currDataMap = null == dataMap ? Collections.emptyMap() : dataMap;
        currDataMap.put(ShardingSphereMetaData.class.getCanonicalName(), this.shardingSphereMetaData);
        return rawExecutorCallbacks.iterator().next().execute(inputs, isTrunkThread, dataMap);
    }
}
