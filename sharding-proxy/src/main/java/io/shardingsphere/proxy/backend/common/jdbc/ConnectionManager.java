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

package io.shardingsphere.proxy.backend.common.jdbc;

import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.proxy.config.RuleRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Connection manager.
 *
 * @author zhaojun
 */
public final class ConnectionManager implements AutoCloseable {
    
    private final Collection<Connection> cachedConnections = new LinkedList<>();
    
    /**
     * Get connection of current thread datasource.
     *
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection(final String dataSourceName) throws SQLException {
        Connection result = RuleRegistry.getInstance().getDataSourceMap().get(dataSourceName).getConnection();
        cachedConnections.add(result);
        return result;
    }
    
    @Override
    public void close() {
        try {
            for (Connection each : cachedConnections) {
                each.close();
            }
        } catch (final SQLException ignored) {
        }
        MasterVisitedManager.clear();
    }
}
