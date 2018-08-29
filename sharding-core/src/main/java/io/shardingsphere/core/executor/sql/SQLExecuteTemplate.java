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

package io.shardingsphere.core.executor.sql;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.executor.sql.event.overall.OverallExecutionEvent;
import io.shardingsphere.core.executor.sql.threadlocal.ExecutorExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL execute template.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class SQLExecuteTemplate {
    
    private final ShardingExecuteEngine executeEngine;
    
    private final ConnectionMode connectionMode;
    
    private final int maxConnectionsSizePerQuery;
    
    /**
     * Execute.
     *
     * @param executeUnits execute units
     * @param executeCallback execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(final Collection<? extends StatementExecuteUnit> executeUnits, final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return execute(executeUnits, null, executeCallback);
    }
    
    /**
     * Execute.
     *
     * @param executeUnits execute units
     * @param firstExecuteCallback first execute callback
     * @param executeCallback execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(
            final Collection<? extends StatementExecuteUnit> executeUnits, final SQLExecuteCallback<T> firstExecuteCallback, final SQLExecuteCallback<T> executeCallback) throws SQLException {
        OverallExecutionEvent event = new OverallExecutionEvent(executeUnits.size() > 1);
        ShardingEventBusInstance.getInstance().post(event);
        try {
            List<T> result = ConnectionMode.MEMORY_STRICTLY == connectionMode ? executeEngine.execute(new LinkedList<>(executeUnits), firstExecuteCallback, executeCallback)
                    : executeEngine.groupExecute(getExecuteUnitGroups(executeUnits), maxConnectionsSizePerQuery, firstExecuteCallback, executeCallback);
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
    
    private Map<String, Collection<StatementExecuteUnit>> getExecuteUnitGroups(final Collection<? extends StatementExecuteUnit> executeUnits) {
        Map<String, Collection<StatementExecuteUnit>> result = new LinkedHashMap<>(executeUnits.size(), 1);
        for (StatementExecuteUnit each : executeUnits) {
            String dataSourceName = each.getSqlExecutionUnit().getDataSource();
            if (!result.keySet().contains(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<StatementExecuteUnit>());
            }
            result.get(dataSourceName).add(each);
        }
        return result;
    }
}
