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

package io.shardingsphere.core.executor.sql.execute;

import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.executor.overall.OverallExecutionEvent;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    
    /**
     * Execute.
     *
     * @param sqlExecuteUnits SQL execute units
     * @param callback SQL execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(final Collection<? extends SQLExecuteUnit> sqlExecuteUnits, final SQLExecuteCallback<T> callback) throws SQLException {
        return execute(sqlExecuteUnits, null, callback);
    }
    
    /**
     * Execute.
     *
     * @param sqlExecuteUnits SQL execute units
     * @param firstExecuteCallback first SQL execute callback
     * @param callback SQL execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> execute(final Collection<? extends SQLExecuteUnit> sqlExecuteUnits,
                               final SQLExecuteCallback<T> firstExecuteCallback, final SQLExecuteCallback<T> callback) throws SQLException {
        OverallExecutionEvent event = new OverallExecutionEvent(sqlExecuteUnits.size() > 1);
        ShardingEventBusInstance.getInstance().post(event);
        try {
            List<T> result = executeEngine.execute((Collection) sqlExecuteUnits, firstExecuteCallback, callback);
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
    
    /**
     * Execute group.
     *
     * @param sqlExecuteGroups SQL execute groups
     * @param callback SQL execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> executeGroup(final Collection<ShardingExecuteGroup<? extends SQLExecuteUnit>> sqlExecuteGroups, final SQLExecuteCallback<T> callback) throws SQLException {
        return executeGroup(sqlExecuteGroups, null, callback);
    }
    
    /**
     * Execute group.
     *
     * @param sqlExecuteGroups SQL execute groups
     * @param firstCallback first SQL execute callback
     * @param callback SQL execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> executeGroup(final Collection<ShardingExecuteGroup<? extends SQLExecuteUnit>> sqlExecuteGroups, 
                               final SQLExecuteCallback<T> firstCallback, final SQLExecuteCallback<T> callback) throws SQLException {
        OverallExecutionEvent event = new OverallExecutionEvent(sqlExecuteGroups.size() > 1);
        ShardingEventBusInstance.getInstance().post(event);
        try {
            List<T> result = executeEngine.groupExecute((Collection) sqlExecuteGroups, firstCallback, callback);
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
}
