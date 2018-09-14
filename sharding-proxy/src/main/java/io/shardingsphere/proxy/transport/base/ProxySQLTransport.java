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

package io.shardingsphere.proxy.transport.base;

import io.shardingsphere.proxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.transaction.manager.base.servicecomb.AbstractSQLTransport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.servicecomb.saga.core.TransportFailedException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL Transport implement for proxy.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public final class ProxySQLTransport extends AbstractSQLTransport {
    
    private final JDBCBackendDataSource backendDataSource;
    
    @Override
    protected Connection getConnection(final String datasource) throws TransportFailedException {
        try {
            return backendDataSource.getConnection(datasource);
        } catch (SQLException ex) {
            throw new TransportFailedException("get connection of [" + datasource + "] occur exception ", ex);
        }
    }
}
