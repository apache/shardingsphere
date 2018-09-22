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

package io.shardingsphere.proxy.backend.jdbc.connection;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.connection.CloseConnectionEvent;
import io.shardingsphere.core.event.connection.CloseConnectionFinishEvent;
import io.shardingsphere.core.event.connection.CloseConnectionStartEvent;
import io.shardingsphere.core.event.connection.GetConnectionEvent;
import io.shardingsphere.core.event.connection.GetConnectionFinishEvent;
import io.shardingsphere.core.event.connection.GetConnectionStartEvent;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaDataFactory;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public final class BackendConnection implements AutoCloseable {
    
    @Getter
    private final RuleRegistry ruleRegistry;
    
    private final Collection<Connection> cachedConnections = new CopyOnWriteArrayList<>();
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
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
        try {
            ShardingEventBusInstance.getInstance().post(new GetConnectionStartEvent(dataSourceName));
            List<Connection> result = ruleRegistry.getBackendDataSource().getConnections(connectionMode, dataSourceName, connectionSize);
            cachedConnections.addAll(result);
            GetConnectionEvent finishEvent = new GetConnectionFinishEvent(result.size(), DataSourceMetaDataFactory.newInstance(DatabaseType.MySQL, result.get(0).getMetaData().getURL()));
            finishEvent.setExecuteSuccess();
            ShardingEventBusInstance.getInstance().post(finishEvent);
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            GetConnectionEvent finishEvent = new GetConnectionFinishEvent(0, null);
            finishEvent.setExecuteFailure(ex);
            ShardingEventBusInstance.getInstance().post(finishEvent);
            throw ex;
        }
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
            } catch (final SQLException ignore) {
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
        CloseConnectionEvent finishEvent = new CloseConnectionFinishEvent();
        for (Connection each : cachedConnections) {
            try {
                ShardingEventBusInstance.getInstance().post(new CloseConnectionStartEvent(each.getCatalog(), DataSourceMetaDataFactory.newInstance(DatabaseType.MySQL, each.getMetaData().getURL())));
                each.close();
                finishEvent.setExecuteSuccess();
                ShardingEventBusInstance.getInstance().post(finishEvent);
            } catch (SQLException ex) {
                finishEvent.setExecuteFailure(ex);
                result.add(ex);
                ShardingEventBusInstance.getInstance().post(finishEvent);
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
