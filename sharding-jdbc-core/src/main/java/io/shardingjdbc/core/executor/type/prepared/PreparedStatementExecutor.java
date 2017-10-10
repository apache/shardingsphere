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

package io.shardingjdbc.core.executor.type.prepared;

import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.executor.BaseStatementUnit;
import io.shardingjdbc.core.executor.ExecuteCallback;
import io.shardingjdbc.core.executor.ExecutorEngine;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

/**
 * PreparedStatement Executor for multiple threads.
 * 
 * @author zhangliang
 * @author caohao
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final SQLType sqlType;
    
    private final Collection<PreparedStatementUnit> preparedStatementUnits;
    
    private final List<Object> parameters;
    
    /**
     * Execute query.
     * 
     * @return result set list
     */
    public List<ResultSet> executeQuery() {
        return executorEngine.executePreparedStatement(sqlType, preparedStatementUnits, parameters, new ExecuteCallback<ResultSet>() {
            
            @Override
            public ResultSet execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                return ((PreparedStatement) baseStatementUnit.getStatement()).executeQuery();
            }
        });
    }
    
    /**
     * Execute update.
     * 
     * @return effected records count
     */
    public int executeUpdate() {
        List<Integer> results = executorEngine.executePreparedStatement(sqlType, preparedStatementUnits, parameters, new ExecuteCallback<Integer>() {
            
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                return ((PreparedStatement) baseStatementUnit.getStatement()).executeUpdate();
            }
        });
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
     */
    public boolean execute() {
        List<Boolean> result = executorEngine.executePreparedStatement(sqlType, preparedStatementUnits, parameters, new ExecuteCallback<Boolean>() {
            
            @Override
            public Boolean execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                return ((PreparedStatement) baseStatementUnit.getStatement()).execute();
            }
        });
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
}
