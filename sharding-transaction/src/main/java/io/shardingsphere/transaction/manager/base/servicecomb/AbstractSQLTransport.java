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

package io.shardingsphere.transaction.manager.base.servicecomb;

import lombok.extern.slf4j.Slf4j;
import org.apache.servicecomb.saga.core.SagaResponse;
import org.apache.servicecomb.saga.core.TransportFailedException;
import org.apache.servicecomb.saga.format.JsonSuccessfulSagaResponse;
import org.apache.servicecomb.saga.transports.SQLTransport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Abstract implement for servicecomb {@code SQLTransport} interface.
 *
 * @author yangyi
 */
@Slf4j
public abstract class AbstractSQLTransport implements SQLTransport {
    
    /**
     * servicecomb saga would call this function for each SQL in transaction.
     *
     * @param datasource data source name for each SQL
     * @param sql        SQL in transaction
     * @param params     parameters for SQL
     * @return saga execute response
     */
    @Override
    public SagaResponse with(final String datasource, final String sql, final List<List<String>> params) {
        Connection connection = getConnection(datasource);
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
        }
        return new JsonSuccessfulSagaResponse("{}");
    }
    
    protected abstract Connection getConnection(String datasource) throws TransportFailedException;
}
