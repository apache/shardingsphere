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

package io.shardingsphere.core.executor.batch;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prepared statement executor to process add batch.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public abstract class BatchPreparedStatementExecutor {
    
    private final DatabaseType dbType;
    
    private final SQLType sqlType;
    
    private final int batchCount;
    
    /**
     * Execute batch.
     * 
     * @return execute results
     * @throws SQLException SQL exception
     */
    public int[] executeBatch() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<int[]> callback = new SQLExecuteCallback<int[]>(sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected int[] executeSQL(final SQLExecuteUnit sqlExecuteUnit) throws SQLException {
                return sqlExecuteUnit.getStatement().executeBatch();
            }
        };
        return accumulate(executeCallback(callback));
    }
    
    private int[] accumulate(final List<int[]> results) {
        int[] result = new int[batchCount];
        int count = 0;
        for (BatchPreparedStatementExecuteUnit each : getBatchPreparedStatementUnitGroups()) {
            for (Entry<Integer, Integer> entry : each.getJdbcAndActualAddBatchCallTimesMap().entrySet()) {
                int value = null == results.get(count) ? 0 : results.get(count)[entry.getValue()];
                if (DatabaseType.Oracle == dbType) {
                    result[entry.getKey()] = value;
                } else {
                    result[entry.getKey()] += value;
                }
            }
            count++;
        }
        return result;
    }
    
    protected abstract <T> List<T> executeCallback(SQLExecuteCallback<T> executeCallback) throws SQLException;
    
    protected abstract Collection<BatchPreparedStatementExecuteUnit> getBatchPreparedStatementUnitGroups();
}
