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

import com.google.common.collect.Lists;
import io.shardingsphere.transaction.saga.constant.ExecutionResult;
import io.shardingsphere.transaction.saga.SagaSubTransaction;
import io.shardingsphere.transaction.saga.SagaTransaction;
import lombok.RequiredArgsConstructor;
import org.apache.servicecomb.saga.core.SagaResponse;
import org.apache.servicecomb.saga.core.TransportFailedException;
import org.apache.servicecomb.saga.format.JsonSuccessfulSagaResponse;
import org.apache.servicecomb.saga.transports.SQLTransport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Sharding SQL transport.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public final class ShardingSQLTransport implements SQLTransport {
    
    private final SagaTransaction sagaTransaction;
    
    @Override
    public SagaResponse with(final String datasource, final String sql, final List<List<String>> params) {
        SagaSubTransaction subTransaction = new SagaSubTransaction(datasource, sql, copyList(params));
        return isExecutionSuccess(subTransaction) ? new JsonSuccessfulSagaResponse("{}") : executeFromDataSource(subTransaction);
    }
    
    private List<List<Object>> copyList(final List<List<String>> origin) {
        List<List<Object>> result = Lists.newArrayList();
        for (List<String> each : origin) {
            result.add(Lists.<Object>newArrayList(each));
        }
        return result;
    }
    
    private boolean isExecutionSuccess(final SagaSubTransaction subTransaction) {
        return sagaTransaction.getExecutionResultMap().containsKey(subTransaction) && ExecutionResult.SUCCESS == sagaTransaction.getExecutionResultMap().get(subTransaction);
    }
    
    private SagaResponse executeFromDataSource(final SagaSubTransaction subTransaction) {
        try (Connection connection = getConnectionFromDataSourceMap(subTransaction.getDataSourceName());
            PreparedStatement statement = connection.prepareStatement(subTransaction.getSql())) {
            if (subTransaction.getParameterSets().isEmpty()) {
                statement.executeUpdate();
            } else {
                for (List<Object> each : subTransaction.getParameterSets()) {
                    for (int parameterIndex = 0; parameterIndex < each.size(); parameterIndex++) {
                        statement.setObject(parameterIndex + 1, each.get(parameterIndex));
                    }
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException ex) {
            throw new TransportFailedException(String.format("execute SQL %s occur exception: ", subTransaction.toString()), ex);
        }
        return new JsonSuccessfulSagaResponse("{}");
    }
    
    private Connection getConnectionFromDataSourceMap(final String datasource) {
        try {
            Connection result = sagaTransaction.getDataSourceMap().get(datasource).getConnection();
            if (!result.getAutoCommit()) {
                result.setAutoCommit(true);
            }
            return result;
        } catch (SQLException ex) {
            throw new TransportFailedException("get connection of [" + datasource + "] occur exception ", ex);
        }
    }
}
