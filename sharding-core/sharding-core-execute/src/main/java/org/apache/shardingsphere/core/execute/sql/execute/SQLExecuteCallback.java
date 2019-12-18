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

package org.apache.shardingsphere.core.execute.sql.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.execute.engine.ShardingGroupExecuteCallback;
import org.apache.shardingsphere.core.execute.hook.SPISQLExecutionHook;
import org.apache.shardingsphere.core.execute.hook.SQLExecutionHook;
import org.apache.shardingsphere.core.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.core.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.spi.database.DataSourceMetaData;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    
    private static final Map<String, DataSourceMetaData> CACHED_DATASOURCE_METADATA = new ConcurrentHashMap<>();
    
    private final DatabaseType databaseType;
    
    private final boolean isExceptionThrown;
    
    @Override
    public final Collection<T> execute(final Collection<StatementExecuteUnit> statementExecuteUnits, 
                                       final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) throws SQLException {
        Collection<T> result = new LinkedList<>();
        for (StatementExecuteUnit each : statementExecuteUnits) {
            result.add(execute0(each, isTrunkThread, shardingExecuteDataMap));
        }
        return result;
    }
    
    /**
     * To make sure SkyWalking will be available at the next release of ShardingSphere,
     * a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
    private T execute0(final StatementExecuteUnit statementExecuteUnit, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        DataSourceMetaData dataSourceMetaData = getDataSourceMetaData(statementExecuteUnit.getStatement().getConnection().getMetaData());
        SQLExecutionHook sqlExecutionHook = new SPISQLExecutionHook();
        try {
            RouteUnit routeUnit = statementExecuteUnit.getRouteUnit();
            sqlExecutionHook.start(routeUnit.getDataSourceName(), routeUnit.getSqlUnit().getSql(), routeUnit.getSqlUnit().getParameters(), dataSourceMetaData, isTrunkThread, shardingExecuteDataMap);
            T result = executeSQL(routeUnit.getSqlUnit().getSql(), statementExecuteUnit.getStatement(), statementExecuteUnit.getConnectionMode());
            sqlExecutionHook.finishSuccess();
            return result;
        } catch (final SQLException ex) {
            sqlExecutionHook.finishFailure(ex);
            ExecutorExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    private DataSourceMetaData getDataSourceMetaData(final DatabaseMetaData metaData) throws SQLException {
        String url = metaData.getURL();
        if (CACHED_DATASOURCE_METADATA.containsKey(url)) {
            return CACHED_DATASOURCE_METADATA.get(url);
        }
        DataSourceMetaData result = databaseType.getDataSourceMetaData(url, metaData.getUserName());
        CACHED_DATASOURCE_METADATA.put(url, result);
        return result;
    }
    
    protected abstract T executeSQL(String sql, Statement statement, ConnectionMode connectionMode) throws SQLException;
}
