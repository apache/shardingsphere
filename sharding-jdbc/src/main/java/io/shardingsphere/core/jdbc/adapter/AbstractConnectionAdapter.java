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

package io.shardingsphere.core.jdbc.adapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.connection.CloseConnectionEvent;
import io.shardingsphere.core.event.connection.CloseConnectionFinishEvent;
import io.shardingsphere.core.event.connection.CloseConnectionStartEvent;
import io.shardingsphere.core.event.connection.GetConnectionEvent;
import io.shardingsphere.core.event.connection.GetConnectionFinishEvent;
import io.shardingsphere.core.event.connection.GetConnectionStartEvent;
import io.shardingsphere.core.event.root.RootInvokeFinishEvent;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.jdbc.adapter.executor.ForceExecuteCallback;
import io.shardingsphere.core.jdbc.adapter.executor.ForceExecuteTemplate;
import io.shardingsphere.core.jdbc.unsupported.AbstractUnsupportedOperationConnection;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaDataFactory;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.core.transaction.TransactionTypeHolder;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Adapter for {@code Connection}.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public abstract class AbstractConnectionAdapter extends AbstractUnsupportedOperationConnection {
    
    private final DatabaseType databaseType;
    
    private final Multimap<String, Connection> cachedConnections = HashMultimap.create();
    
    private boolean autoCommit = true;
    
    private boolean readOnly = true;
    
    private boolean closed;
    
    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    private final ForceExecuteTemplate<Connection> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    private final ForceExecuteTemplate<Map.Entry<String, Connection>> forceExecuteTemplateForClose = new ForceExecuteTemplate<>();
    
    /**
     * Get database connection.
     *
     * @param dataSourceName data source name
     * @return database connection
     * @throws SQLException SQL exception
     */
    public final Connection getConnection(final String dataSourceName) throws SQLException {
        return getConnections(ConnectionMode.MEMORY_STRICTLY, dataSourceName, 1).get(0);
    }
    
    /**
     * Get database connections.
     *
     * @param connectionMode connection mode
     * @param dataSourceName data source name
     * @param connectionSize size of connection list to be get
     * @return database connections
     * @throws SQLException SQL exception
     */
    public final List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        ShardingEventBusInstance.getInstance().post(new GetConnectionStartEvent(dataSourceName));
        DataSource dataSource = getDataSourceMap().get(dataSourceName);
        Preconditions.checkState(null != dataSource, "Missing the data source name: '%s'", dataSourceName);
        Collection<Connection> connections = cachedConnections.get(dataSourceName);
        List<Connection> result;
        if (connections.size() >= connectionSize) {
            result = new ArrayList<>(cachedConnections.get(dataSourceName)).subList(0, connectionSize);
        } else if (!connections.isEmpty()) {
            result = new ArrayList<>(connectionSize);
            result.addAll(connections);
            List<Connection> newConnections = createConnections(connectionMode, dataSource, connectionSize - connections.size());
            result.addAll(newConnections);
            cachedConnections.putAll(dataSourceName, newConnections);
        } else {
            result = new ArrayList<>(createConnections(connectionMode, dataSource, connectionSize));
            cachedConnections.putAll(dataSourceName, result);
        }
        postGetConnectionEvent(result);
        return result;
    }
    
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private List<Connection> createConnections(final ConnectionMode connectionMode, final DataSource dataSource, final int connectionSize) throws SQLException {
        if (1 == connectionSize) {
            return Collections.singletonList(createConnection(dataSource));
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(dataSource, connectionSize);
        }
        synchronized (dataSource) {
            return createConnections(dataSource, connectionSize);
        }
    }
    
    private List<Connection> createConnections(final DataSource dataSource, final int connectionSize) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            result.add(createConnection(dataSource));
        }
        return result;
    }
    
    private Connection createConnection(final DataSource dataSource) throws SQLException {
        Connection result = dataSource.getConnection();
        replayMethodsInvocation(result);
        return result;
    }
    
    private void postGetConnectionEvent(final List<Connection> connections) throws SQLException {
        GetConnectionEvent finishEvent = new GetConnectionFinishEvent(connections.size(), DataSourceMetaDataFactory.newInstance(databaseType, connections.get(0).getMetaData().getURL()));
        finishEvent.setExecuteSuccess();
        ShardingEventBusInstance.getInstance().post(finishEvent);
    }
    
    protected abstract Map<String, DataSource> getDataSourceMap();
    
    protected final void removeCache(final Connection connection) {
        cachedConnections.values().remove(connection);
    }
    
    @Override
    public final boolean getAutoCommit() {
        return autoCommit;
    }
    
    @Override
    public final void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        if (TransactionType.LOCAL == TransactionTypeHolder.get()) {
            recordMethodInvocation(Connection.class, "setAutoCommit", new Class[]{boolean.class}, new Object[]{autoCommit});
            forceExecuteTemplate.execute(cachedConnections.values(), new ForceExecuteCallback<Connection>() {
                
                @Override
                public void execute(final Connection connection) throws SQLException {
                    connection.setAutoCommit(autoCommit);
                }
            });
        } else if (TransactionType.XA == TransactionTypeHolder.get()) {
            ShardingEventBusInstance.getInstance().post(new XATransactionEvent(TransactionOperationType.BEGIN));
        }
    }
    
    @Override
    public final void commit() throws SQLException {
        if (TransactionType.LOCAL == TransactionTypeHolder.get()) {
            forceExecuteTemplate.execute(cachedConnections.values(), new ForceExecuteCallback<Connection>() {
                
                @Override
                public void execute(final Connection connection) throws SQLException {
                    connection.commit();
                }
            });
        } else if (TransactionType.XA == TransactionTypeHolder.get()) {
            ShardingEventBusInstance.getInstance().post(new XATransactionEvent(TransactionOperationType.COMMIT));
        }
    }
    
    @Override
    public final void rollback() throws SQLException {
        if (TransactionType.LOCAL == TransactionTypeHolder.get()) {
            forceExecuteTemplate.execute(cachedConnections.values(), new ForceExecuteCallback<Connection>() {
                
                @Override
                public void execute(final Connection connection) throws SQLException {
                    connection.rollback();
                }
            });
        } else if (TransactionType.XA == TransactionTypeHolder.get()) {
            ShardingEventBusInstance.getInstance().post(new XATransactionEvent(TransactionOperationType.ROLLBACK));
        }
    }
    
    @Override
    public final void close() throws SQLException {
        if (closed) {
            return;
        }
        closed = true;
        HintManagerHolder.clear();
        MasterVisitedManager.clear();
        TransactionTypeHolder.clear();
        forceExecuteTemplateForClose.execute(cachedConnections.entries(), new ForceExecuteCallback<Map.Entry<String, Connection>>() {
            
            @Override
            public void execute(final Map.Entry<String, Connection> cachedConnectionsEntrySet) throws SQLException {
                Connection connection = cachedConnectionsEntrySet.getValue();
                ShardingEventBusInstance.getInstance().post(
                        new CloseConnectionStartEvent(cachedConnectionsEntrySet.getKey(), DataSourceMetaDataFactory.newInstance(databaseType, connection.getMetaData().getURL())));
                CloseConnectionEvent finishEvent = new CloseConnectionFinishEvent();
                try {
                    connection.close();
                    finishEvent.setExecuteSuccess();
                    // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    // CHECKSTYLE:ON
                    finishEvent.setExecuteFailure(ex);
                    throw ex;
                } finally {
                    ShardingEventBusInstance.getInstance().post(finishEvent);
                }
            }
        });
        ShardingEventBusInstance.getInstance().post(new RootInvokeFinishEvent());
    }
                    
    @Override
    public final boolean isClosed() {
        return closed;
    }
    
    @Override
    public final boolean isReadOnly() {
        return readOnly;
    }
    
    @Override
    public final void setReadOnly(final boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        recordMethodInvocation(Connection.class, "setReadOnly", new Class[]{boolean.class}, new Object[]{readOnly});
        forceExecuteTemplate.execute(cachedConnections.values(), new ForceExecuteCallback<Connection>() {
            
            @Override
            public void execute(final Connection connection) throws SQLException {
                connection.setReadOnly(readOnly);
            }
        });
    }
    
    @Override
    public final int getTransactionIsolation() throws SQLException {
        if (cachedConnections.values().isEmpty()) {
            return transactionIsolation;
        }
        return cachedConnections.values().iterator().next().getTransactionIsolation();
    }
    
    @Override
    public final void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        recordMethodInvocation(Connection.class, "setTransactionIsolation", new Class[]{int.class}, new Object[]{level});
        forceExecuteTemplate.execute(cachedConnections.values(), new ForceExecuteCallback<Connection>() {
            
            @Override
            public void execute(final Connection connection) throws SQLException {
                connection.setTransactionIsolation(level);
            }
        });
    }
    
    // ------- Consist with MySQL driver implementation -------
    
    @Override
    public final SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public void clearWarnings() {
    }
    
    @Override
    public final int getHoldability() {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public final void setHoldability(final int holdability) {
    }
}
