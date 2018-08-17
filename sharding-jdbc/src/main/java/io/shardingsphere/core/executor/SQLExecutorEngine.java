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

package io.shardingsphere.core.executor;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.executor.event.overall.OverallExecutionEvent;
import io.shardingsphere.core.executor.threadlocal.ExecutorExceptionHandler;
import lombok.Getter;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL execute engine.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class SQLExecutorEngine implements AutoCloseable {
    
    @Getter
    private final ShardingExecuteEngine shardingExecuteEngine;
    
    private final ConnectionMode connectionMode;
    
    public SQLExecutorEngine(final int executorSize, final ConnectionMode connectionMode) {
        shardingExecuteEngine = new ShardingExecuteEngine(executorSize);
        this.connectionMode = connectionMode;
    }
    
    /**
     * Execute.
     *
     * @param baseStatementUnits statement execute units
     * @param executeCallback prepared statement execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(final Collection<? extends BaseStatementUnit> baseStatementUnits, final SQLExecuteCallback<T> executeCallback) throws SQLException {
        OverallExecutionEvent event = new OverallExecutionEvent(executeCallback.getSqlType(), baseStatementUnits.size() > 1);
        ShardingEventBusInstance.getInstance().post(event);
        try {
            List<T> result = ConnectionMode.MEMORY_STRICTLY == connectionMode ? shardingExecuteEngine.execute(new LinkedList<>(baseStatementUnits), executeCallback)
                    : shardingExecuteEngine.groupExecute(getBaseStatementUnitGroups(baseStatementUnits), executeCallback);
            event.setExecuteSuccess();
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            event.setExecuteFailure(ex);
            ExecutorExceptionHandler.handleException(ex);
            return Collections.emptyList();
        } finally {
            ShardingEventBusInstance.getInstance().post(event);
        }
    }
    
    private Map<String, Collection<BaseStatementUnit>> getBaseStatementUnitGroups(final Collection<? extends BaseStatementUnit> baseStatementUnits) {
        Map<String, Collection<BaseStatementUnit>> result = new LinkedHashMap<>(baseStatementUnits.size(), 1);
        for (BaseStatementUnit each : baseStatementUnits) {
            String dataSourceName = each.getSqlExecutionUnit().getDataSource();
            if (!result.keySet().contains(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<BaseStatementUnit>());
            }
            result.get(dataSourceName).add(each);
        }
        return result;
    }
    
    @Override
    public void close() {
        shardingExecuteEngine.close();
    }
}
