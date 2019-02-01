/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.spi.executor;

import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.spi.NewInstanceServiceLoader;

import java.util.Collection;
import java.util.Map;

/**
 * SQL Execution hook for SPI.
 *
 * @author zhangliang
 */
public final class SPISQLExecutionHook implements SQLExecutionHook {
    
    private final Collection<SQLExecutionHook> sqlExecutionHooks = NewInstanceServiceLoader.newServiceInstances(SQLExecutionHook.class);
    
    @Override
    public void start(final RouteUnit routeUnit, final DataSourceMetaData dataSourceMetaData, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
        for (SQLExecutionHook each : sqlExecutionHooks) {
            each.start(routeUnit, dataSourceMetaData, isTrunkThread, shardingExecuteDataMap);
        }
    }
    
    @Override
    public void finishSuccess() {
        for (SQLExecutionHook each : sqlExecutionHooks) {
            each.finishSuccess();
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        for (SQLExecutionHook each : sqlExecutionHooks) {
            each.finishFailure(cause);
        }
    }
}
