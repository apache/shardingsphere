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

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Backend connection.
 *
 * @author zhaojun
 * @author zhangliang
 */
@NoArgsConstructor
public final class BackendConnection implements AutoCloseable {
    
    @Getter
    @Setter
    private LogicSchema logicSchema;
    
    private final Collection<Connection> cachedConnections = new CopyOnWriteArrayList<>();
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
    /**
     * Get connection size.
     * 
     * @return connection size
     */
    public int getConnectionSize() {
        return cachedConnections.size();
    }
    
    /**
     * Get connections of current thread datasource.
     *
     * @param connectionMode connection mode
     * @param dataSourceName data source name
     * @param connectionSize size of connections to be get
     * @return connection
     * @throws SQLException SQL exception
     */
    public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        List<Connection> result = logicSchema.getBackendDataSource().getConnections(connectionMode, dataSourceName, connectionSize);
        cachedConnections.addAll(result);
        return result;
    }
    
    /**
     * Add statement.
     *
     * @param statement statement to be added
     */
    public void add(final Statement statement) {
        cachedStatements.add(statement);
    }
    
    /**
     * Add result set.
     *
     * @param resultSet result set to be added
     */
    public void add(final ResultSet resultSet) {
        cachedResultSets.add(resultSet);
    }
    
    /**
     * Cancel statement.
     */
    public void cancel() {
        for (Statement each : cachedStatements) {
            try {
                each.cancel();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    @Override
    public void close() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        exceptions.addAll(closeResultSets());
        exceptions.addAll(closeStatements());
        exceptions.addAll(closeConnections());
        MasterVisitedManager.clear();
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    private Collection<SQLException> closeResultSets() {
        Collection<SQLException> result = new LinkedList<>();
        for (ResultSet each : cachedResultSets) {
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
    
    private Collection<SQLException> closeStatements() {
        Collection<SQLException> result = new LinkedList<>();
        for (Statement each : cachedStatements) {
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
    
    private Collection<SQLException> closeConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections) {
            try {
                each.close();
            } catch (SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
    
    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException();
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }
}
