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

package org.apache.shardingsphere.core.executor.sql.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.executor.ShardingGroupExecuteCallback;
import org.apache.shardingsphere.core.executor.StatementExecuteUnit;
import org.apache.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetaDataFactory;
import org.apache.shardingsphere.core.spi.hook.SPISQLExecutionHook;
import org.apache.shardingsphere.spi.hook.SQLExecutionHook;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
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
public abstract class SQLExecuteCallback<T> implements ShardingGroupExecuteCallback<StatementExecuteUnit, T> {
    
    private final DatabaseType databaseType;
    
    private final boolean isExceptionThrown;
    
    @Override
    public final Collection<T> execute(final Collection<StatementExecuteUnit> statementExecuteUnits, final boolean isTrunkThread,
                                       final Map<String, Object> shardingExecuteDataMap) throws SQLException {
        Collection<T> result = new LinkedList<>();
        for (StatementExecuteUnit each : statementExecuteUnits) {
            result.add(execute0(each, isTrunkThread, shardingExecuteDataMap));
        }
        return result;
    }
    
    private T execute0(final StatementExecuteUnit statementExecuteUnit, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        DataSourceMetaData dataSourceMetaData = DataSourceMetaDataFactory.newInstance(databaseType, statementExecuteUnit.getDatabaseMetaData().getURL());
        SQLExecutionHook sqlExecutionHook = new SPISQLExecutionHook();
        try {
            sqlExecutionHook.start(statementExecuteUnit.getRouteUnit(), dataSourceMetaData, isTrunkThread, shardingExecuteDataMap);
            T result = executeSQL(statementExecuteUnit);
            sqlExecutionHook.finishSuccess();
            return result;
        } catch (final SQLException ex) {
            sqlExecutionHook.finishFailure(ex);
            ExecutorExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    protected abstract T executeSQL(StatementExecuteUnit statementExecuteUnit) throws SQLException;
}
