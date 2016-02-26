/**
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

package com.dangdang.ddframe.rdb.sharding.executor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import lombok.RequiredArgsConstructor;

/**
 * 多线程执行预编译语句对象请求的执行器.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final Collection<PreparedStatement> preparedStatements;
    
    /**
     * 执行SQL查询.
     * 
     * @return 结果集列表
     * @throws SQLException SQL异常
     */
    public List<ResultSet> executeQuery() throws SQLException {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeQuery");
        List<ResultSet> result;
        if (1 == preparedStatements.size()) {
            result =  Arrays.asList(preparedStatements.iterator().next().executeQuery());
            MetricsContext.stop(context);
            return result;
        }
        result = executorEngine.execute(preparedStatements, new ExecuteUnit<PreparedStatement, ResultSet>() {
            
            @Override
            public ResultSet execute(final PreparedStatement input) throws Exception {
                return input.executeQuery();
            }
        });
        MetricsContext.stop(context);
        return result;
    }
    
    /**
     * 执行SQL更新.
     * 
     * @return 更新数量
     * @throws SQLException SQL异常
     */
    public int executeUpdate() throws SQLException {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeUpdate");
        int result;
        if (1 == preparedStatements.size()) {
            result =  preparedStatements.iterator().next().executeUpdate();
            MetricsContext.stop(context);
            return result;
        }
        result = executorEngine.execute(preparedStatements, new ExecuteUnit<PreparedStatement, Integer>() {
            
            @Override
            public Integer execute(final PreparedStatement input) throws Exception {
                return input.executeUpdate();
            }
        }, new MergeUnit<Integer, Integer>() {
            
            @Override
            public Integer merge(final List<Integer> results) {
                int result = 0;
                for (Integer each : results) {
                    result += each;
                }
                return result;
            }
        });
        MetricsContext.stop(context);
        return result;
    }
    
    /**
     * 执行SQL请求.
     * 
     * @return true表示执行DQL, false表示执行的DML
     * @throws SQLException SQL异常
     */
    public boolean execute() throws SQLException {
        Context context = MetricsContext.start("ShardingPreparedStatement-execute");
        if (1 == preparedStatements.size()) {
            boolean result = preparedStatements.iterator().next().execute();
            MetricsContext.stop(context);
            return result;
        }
        List<Boolean> result = executorEngine.execute(preparedStatements, new ExecuteUnit<PreparedStatement, Boolean>() {
            
            @Override
            public Boolean execute(final PreparedStatement input) throws Exception {
                return input.execute();
            }
        });
        MetricsContext.stop(context);
        return result.get(0);
    }
}
