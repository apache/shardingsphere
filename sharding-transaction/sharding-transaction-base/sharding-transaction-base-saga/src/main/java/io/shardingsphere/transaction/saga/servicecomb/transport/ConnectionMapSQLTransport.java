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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

import io.shardingsphere.core.executor.StatementExecuteUnit;

/**
 * Cache statement SQL transport.
 *
 * @author yangyi
 */
@Slf4j
@RequiredArgsConstructor
public final class ConnectionMapSQLTransport implements SQLTransport {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    
    @Override
    public SagaResponse with(final String datasource, final String sql, final List<List<String>> params) {
        String id = generateId(datasource, sql, params);
        boolean isCachedConnection = connectionMap.containsKey(id);
        Connection connection = isCachedConnection ? connectionMap.get(id) : getConnectionFromDataSourceMap(datasource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
        } finally {
            if (!isCachedConnection) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return new JsonSuccessfulSagaResponse("{}");
    }
    
    public void cacheStatement(final StatementExecuteUnit executeUnit) throws SQLException {
        connectionMap.put(generateId(executeUnit), executeUnit.getStatement().getConnection());
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
        String paramsString = Joiner.on(',').join(Lists.transform(executeUnit.getRouteUnit().getSqlUnit().getParameterSets(), new Function<List<Object>, String>() {
            
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
