/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.executor.type.batch;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.executor.BaseStatementUnit;
import io.shardingjdbc.core.executor.ExecuteCallback;
import io.shardingjdbc.core.executor.ExecutorEngine;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * PreparedStatement Executor for  multiple threads to process add batch.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class BatchPreparedStatementExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final DatabaseType dbType;
    
    private final SQLType sqlType;
    
    private final Collection<BatchPreparedStatementUnit> batchPreparedStatementUnits;
    
    private final List<List<Object>> parameterSets;
    
    /**
     * Execute batch.
     * 
     * @return execute results
     */
    public int[] executeBatch() {
        return accumulate(executorEngine.executeBatch(sqlType, batchPreparedStatementUnits, parameterSets, new ExecuteCallback<int[]>() {
            
            @Override
            public int[] execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                return baseStatementUnit.getStatement().executeBatch();
            }
        }));
    }
    
    private int[] accumulate(final List<int[]> results) {
        int[] result = new int[parameterSets.size()];
        int count = 0;
        for (BatchPreparedStatementUnit each : batchPreparedStatementUnits) {
            for (Map.Entry<Integer, Integer> entry : each.getJdbcAndActualAddBatchCallTimesMap().entrySet()) {
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
}
