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

package io.shardingsphere.transaction.saga.servicecomb.transport;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.transaction.core.constant.ExecutionResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.servicecomb.saga.core.SagaResponse;
import org.apache.servicecomb.saga.core.TransportFailedException;
import org.apache.servicecomb.saga.format.JsonSuccessfulSagaResponse;
import org.apache.servicecomb.saga.transports.SQLTransport;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Cache result of execution SQL.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public final class ResultsSQLTransport implements SQLTransport {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final Map<String, ExecutionResult> resultMap = new ConcurrentHashMap<>();
    
    @Getter
    private volatile boolean hasExecutionFailure;
    
    @Override
    public SagaResponse with(final String datasource, final String sql, final List<List<String>> params) {
        String id = generateId(datasource, sql, params);
        if (resultMap.containsKey(id)) {
            switch (resultMap.get(id)) {
                case FAILURE:
                    throw new TransportFailedException(String.format("execute SQL %s fail", sql));
                case SUCCESS:
                    return new JsonSuccessfulSagaResponse("{}");
                default:
            }
        }
        try (Connection connection = getConnectionFromDataSourceMap(datasource);
            PreparedStatement statement = connection.prepareStatement(sql)) {
            if (params.isEmpty()) {
                statement.executeUpdate();
            } else {
                for (List<String> each : params) {
                    for (int parameterIndex = 0; parameterIndex < each.size(); parameterIndex++) {
                        statement.setObject(parameterIndex + 1, each.get(parameterIndex));
                    }
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            throw new TransportFailedException("execute SQL " + sql + " occur exception: ", e);
        }
        return new JsonSuccessfulSagaResponse("{}");
    }
    
    /**
     * Cache execution result for each SQL.
     *
     * @param executeUnit statement execute Unit
     * @param executionResult execution result
     */
    public void cacheResult(final StatementExecuteUnit executeUnit, final ExecutionResult executionResult) {
        if (ExecutionResult.FAILURE == executionResult) {
            hasExecutionFailure = true;
        }
        resultMap.put(generateId(executeUnit), executionResult);
    }
    
    private Connection getConnectionFromDataSourceMap(final String datasource) {
        try {
            Connection result = dataSourceMap.get(datasource).getConnection();
            if (!result.getAutoCommit()) {
                result.setAutoCommit(true);
            }
            return result;
        } catch (SQLException ex) {
            throw new TransportFailedException("get connection of [" + datasource + "] occur exception ", ex);
        }
    }
    
    private String generateId(final StatementExecuteUnit executeUnit) {
        String paramsString = Joiner.on(',').join(Lists
            .transform(executeUnit.getRouteUnit().getSqlUnit().getParameterSets(), new Function<List<Object>, String>() {
                
                @Override
                public String apply(final List<Object> input) {
                    return Joiner.on(',').join(input);
                }
            }));
        return String.valueOf((executeUnit.getRouteUnit().getDataSourceName() + executeUnit.getRouteUnit().getSqlUnit().getSql() + paramsString).hashCode());
    }
    
    private String generateId(final String datasource, final String sql, final List<List<String>> params) {
        String paramsString = Joiner.on(',').join(Lists.transform(params, new Function<List<String>, String>() {
            
            @Override
            public String apply(final List<String> input) {
                return Joiner.on(',').join(input);
            }
        }));
        return String.valueOf((datasource + sql + paramsString).hashCode());
    }
}
