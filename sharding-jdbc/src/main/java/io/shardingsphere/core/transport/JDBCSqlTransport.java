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

package io.shardingsphere.core.transport;

import io.shardingsphere.core.jdbc.adapter.AbstractConnectionAdapter;
import io.shardingsphere.transaction.manager.base.servicecomb.AbstractSQLTransport;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.servicecomb.saga.core.TransportFailedException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC SQLTransport implement.
 *
 * @author yangyi
 */
@AllArgsConstructor
@NoArgsConstructor
public final class JDBCSqlTransport extends AbstractSQLTransport {
    
    @Setter
    private AbstractConnectionAdapter shardingConnection;
    
    @Override
    protected Connection getConnection(final String datasource) throws TransportFailedException {
        try {
            Connection connection = shardingConnection.getConnection(datasource);
            if (!connection.isClosed() && !connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }
            return connection;
        } catch (SQLException e) {
            throw new TransportFailedException("get connection of [" + datasource + "] occur exception ", e);
        }
    }
    
    /**
     * Renew sharding connection.
     *
     * @param shardingConnection new sharding connection.
     * @throws SQLException sql exception
     */
    public void renew(final AbstractConnectionAdapter shardingConnection) throws SQLException {
        this.shardingConnection.close();
        this.shardingConnection = shardingConnection;
    }
}
