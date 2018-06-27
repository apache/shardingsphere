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

package io.shardingsphere.proxy.backend.resource;

import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Abstract proxy-jdbc-resource.
 *
 * @author zhaojun
 */
@Getter
@Setter
public abstract class BaseJDBCResource {
    
    private List<Connection> connections;
    
    private List<ResultSet> resultSets;
    
    public BaseJDBCResource(final List<Connection> connections, final List<ResultSet> resultSets) {
        this.connections = connections;
        this.resultSets = resultSets;
    }
    
    /**
     * Add new connection to resource manager.
     *
     * @param connection Connection
     */
    public void addConnection(final Connection connection) {
        connections.add(connection);
    }
    
    /**
     * Add new resultSet to resource manager.
     *
     * @param resultSet result set
     */
    public void addResultSet(final ResultSet resultSet) {
        resultSets.add(resultSet);
    }
    
    /**
     * Clear all usable proxy resource in current thread.
     *
     * @throws SQLException SQLException
     */
    public void clear() throws SQLException {
        if (null != connections) {
            for (Connection each : connections) {
                if (!each.isClosed()) {
                    each.close();
                }
                
            }
        }
        if (null != resultSets) {
            for (ResultSet each : resultSets) {
                if (!each.isClosed()) {
                    each.close();
                }
                MasterVisitedManager.clear();
            }
        }
    }
}
