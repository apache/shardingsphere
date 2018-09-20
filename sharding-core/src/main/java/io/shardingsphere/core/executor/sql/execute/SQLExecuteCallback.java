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

import com.google.common.eventbus.EventBus;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.executor.SQLExecutionEvent;
import io.shardingsphere.core.event.executor.SQLExecutionEventFactory;
import io.shardingsphere.core.executor.ShardingExecuteCallback;
import io.shardingsphere.core.executor.ShardingGroupExecuteCallback;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaDataFactory;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Statement execute callback interface.
 *
 * @author gaohongtao
 * @author zhangliang
 *
 * @param <T> class type of return value
 */
@RequiredArgsConstructor
public abstract class SQLExecuteCallback<T> implements ShardingExecuteCallback<StatementExecuteUnit, T>, ShardingGroupExecuteCallback<StatementExecuteUnit, T> {
    
    private final DatabaseType databaseType;
    
    private final SQLType sqlType;
    
    private final boolean isExceptionThrown;
    
    private final Map<String, Object> dataMap;
    
    private final EventBus shardingEventBus = ShardingEventBusInstance.getInstance();
    
    @Override
    public final T execute(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
        return execute0(statementExecuteUnit);
    }
    
    @Override
    public final Collection<T> execute(final Collection<StatementExecuteUnit> statementExecuteUnits) throws SQLException {
        Collection<T> result = new LinkedList<>();
        for (StatementExecuteUnit each : statementExecuteUnits) {
            result.add(execute0(each));
        }
        return result;
    }
    
    private T execute0(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        List<List<Object>> parameterSets = statementExecuteUnit.getRouteUnit().getSqlUnit().getParameterSets();
        DataSourceMetaData dataSourceMetaData = DataSourceMetaDataFactory.newInstance(databaseType, statementExecuteUnit.getStatement().getConnection().getMetaData().getURL());
        for (List<Object> each : parameterSets) {
            shardingEventBus.post(SQLExecutionEventFactory.createEvent(sqlType, statementExecuteUnit, each, dataSourceMetaData));
        }
        try {
            T result = executeSQL(statementExecuteUnit);
            for (List<Object> each : parameterSets) {
                SQLExecutionEvent finishEvent = SQLExecutionEventFactory.createEvent(sqlType, statementExecuteUnit, each, dataSourceMetaData);
                finishEvent.setExecuteSuccess();
                shardingEventBus.post(finishEvent);
            }
            return result;
        } catch (final SQLException ex) {
            for (List<Object> each : parameterSets) {
                SQLExecutionEvent finishEvent = SQLExecutionEventFactory.createEvent(sqlType, statementExecuteUnit, each, dataSourceMetaData);
                finishEvent.setExecuteFailure(ex);
                shardingEventBus.post(finishEvent);
            }
            ExecutorExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    protected abstract T executeSQL(StatementExecuteUnit statementExecuteUnit) throws SQLException;
}
