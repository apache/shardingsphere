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

package io.shardingsphere.core.executor.prepared;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Prepared statement executor.
 * 
 * @author zhangliang
 * @author caohao
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public abstract class PreparedStatementExecutor {
    
    private final SQLType sqlType;
    
    /**
     * Execute query.
     * 
     * @return result set list
     * @throws SQLException SQL exception
     */
    public List<ResultSet> executeQuery() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<ResultSet> executeCallback = new SQLExecuteCallback<ResultSet>(sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected ResultSet executeSQL(final SQLExecuteUnit sqlExecuteUnit) throws SQLException {
                return ((PreparedStatement) sqlExecuteUnit.getStatement()).executeQuery();
            }
        };
        return executeCallback(executeCallback);
    }
    
    /**
     * Execute update.
     * 
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Integer> executeCallback = new SQLExecuteCallback<Integer>(sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Integer executeSQL(final SQLExecuteUnit sqlExecuteUnit) throws SQLException {
                return ((PreparedStatement) sqlExecuteUnit.getStatement()).executeUpdate();
            }
        };
        List<Integer> results = executeCallback(executeCallback);
        return accumulate(results);
    }
    
    private int accumulate(final List<Integer> results) {
        int result = 0;
        for (Integer each : results) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    /**
     * Execute SQL.
     *
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute() throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Boolean> executeCallback = new SQLExecuteCallback<Boolean>(sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Boolean executeSQL(final SQLExecuteUnit sqlExecuteUnit) throws SQLException {
                return ((PreparedStatement) sqlExecuteUnit.getStatement()).execute();
            }
        };
        List<Boolean> result = executeCallback(executeCallback);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
    
    protected abstract <T> List<T> executeCallback(SQLExecuteCallback<T> executeCallback) throws SQLException;
}
