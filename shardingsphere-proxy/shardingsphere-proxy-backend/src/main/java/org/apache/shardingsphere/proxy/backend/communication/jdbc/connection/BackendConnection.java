/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.parameter.TypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.connection.JDBCExecutionConnection;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementOption;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.masterslave.route.engine.impl.MasterVisitedManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.status.ConnectionStatusManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.StatementMemoryStrictlyFetchSizeSetter;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Backend connection.
 */
@Getter
@Slf4j
public final class BackendConnection implements JDBCExecutionConnection, AutoCloseable {
    
    static {
        ShardingSphereServiceLoader.register(StatementMemoryStrictlyFetchSizeSetter.class);
    }
    
    private static final int MAXIMUM_RETRY_COUNT = 5;
    
    private volatile String schemaName;
    
    private TransactionType transactionType;
    
    @Setter
    private int connectionId;
    
    @Setter
    private String username;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
    private final Collection<MethodInvocation> methodInvocations = new LinkedList<>();
    
    private final ResourceLock resourceLock = new ResourceLock();
    
    private final ConnectionStatusManager statusManager = new ConnectionStatusManager(resourceLock);
    
    public BackendConnection(final TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    /**
     * Change transaction type of current channel.
     *
     * @param transactionType transaction type
     */
    public void setTransactionType(final TransactionType transactionType) {
        if (null == schemaName) {
            throw new ShardingSphereException("Please select database, then switch transaction type.");
        }
        if (isSwitchFailed()) {
            throw new ShardingSphereException("Failed to switch transaction type, please terminate current transaction.");
        }
        this.transactionType = transactionType;
    }
    
    /**
     * Change schema of current channel.
     *
     * @param schemaName schema name
     */
    public void setCurrentSchema(final String schemaName) {
        if (isSwitchFailed()) {
            throw new ShardingSphereException("Failed to switch schema, please terminate current transaction.");
        }
        this.schemaName = schemaName;
    }
    
    private boolean isSwitchFailed() {
        int retryCount = 0;
        while (statusManager.isInTransaction() && retryCount < MAXIMUM_RETRY_COUNT) {
            resourceLock.doAwait();
            ++retryCount;
            log.info("Current transaction have not terminated, retry count:[{}].", retryCount);
        }
        return retryCount >= MAXIMUM_RETRY_COUNT;
    }
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        return statusManager.isInTransaction()
                ? getConnectionsWithTransaction(dataSourceName, connectionSize, connectionMode) : getConnectionsWithoutTransaction(dataSourceName, connectionSize, connectionMode);
    }
    
    private List<Connection> getConnectionsWithTransaction(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        Collection<Connection> connections;
        synchronized (cachedConnections) {
            connections = cachedConnections.get(dataSourceName);
        }
        List<Connection> result;
        if (connections.size() >= connectionSize) {
            result = new ArrayList<>(connections).subList(0, connectionSize);
        } else if (!connections.isEmpty()) {
            result = new ArrayList<>(connectionSize);
            result.addAll(connections);
            List<Connection> newConnections = createNewConnections(dataSourceName, connectionSize - connections.size(), connectionMode);
            result.addAll(newConnections);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, newConnections);
            }
        } else {
            result = createNewConnections(dataSourceName, connectionSize, connectionMode);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, result);
            }
        }
        return result;
    }
    
    private List<Connection> createNewConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        Preconditions.checkNotNull(schemaName, "Current schema is null.");
        List<Connection> result = ProxyContext.getInstance().getBackendDataSource().getConnections(schemaName, dataSourceName, connectionSize, connectionMode);
        for (Connection each : result) {
            replayMethodsInvocation(each);
        }
        return result;
    }
    
    private List<Connection> getConnectionsWithoutTransaction(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        Preconditions.checkNotNull(schemaName, "Current schema is null.");
        List<Connection> result = ProxyContext.getInstance().getBackendDataSource().getConnections(schemaName, dataSourceName, connectionSize, connectionMode);
        synchronized (cachedConnections) {
            cachedConnections.putAll(dataSourceName, result);
        }
        return result;
    }
    
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        Statement result = connection.createStatement();
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result);
        }
        return result;
    }
    
    @Override
    public PreparedStatement createStorageResource(final String sql, final List<Object> parameters, 
                                                   final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        PreparedStatement result = option.isReturnGeneratedKeys()
                ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            if (parameter instanceof TypeUnspecifiedSQLParameter) {
                result.setObject(i + 1, parameter, Types.OTHER);
            } else {
                result.setObject(i + 1, parameter);
            }
        }
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result);
        }
        return result;
    }
    
    private void setFetchSize(final Statement statement) throws SQLException {
        DatabaseType databaseType = ProxyContext.getInstance().getSchemaContexts().getDatabaseType();
        TypedSPIRegistry.getRegisteredService(StatementMemoryStrictlyFetchSizeSetter.class, databaseType.getName(), new Properties()).setFetchSize(statement);
    }
    
    /**
     * Whether execute SQL serial or not.
     *
     * @return true or false
     */
    public boolean isSerialExecute() {
        return statusManager.isInTransaction() && (TransactionType.LOCAL == transactionType || TransactionType.XA == transactionType);
    }
    
    /**
     * Get connection size.
     *
     * @return connection size
     */
    public int getConnectionSize() {
        return cachedConnections.values().size();
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
    
    @Override
    public void close() throws SQLException {
        close(false);
    }
    
    /**
     * Close cached connection.
     *
     * @param forceClose force close flag
     * @throws SQLException SQL exception
     */
    public synchronized void close(final boolean forceClose) throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        MasterVisitedManager.clear();
        exceptions.addAll(closeResultSets());
        exceptions.addAll(closeStatements());
        if (!statusManager.isInTransaction() || forceClose || TransactionType.BASE == transactionType) {
            exceptions.addAll(releaseConnections(forceClose));
        }
        statusManager.switchToReleased();
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
        cachedResultSets.clear();
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
        cachedStatements.clear();
        return result;
    }
    
    /**
     * Release connections.
     * 
     * @param forceRollback is force rollback
     * @return SQL exception when connections release
     */
    public Collection<SQLException> releaseConnections(final boolean forceRollback) {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                if (forceRollback && statusManager.isInTransaction()) {
                    each.rollback();
                }
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        cachedConnections.clear();
        methodInvocations.clear();
        return result;
    }
    
    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException("");
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }
    
    private void replayMethodsInvocation(final Object target) {
        for (MethodInvocation each : methodInvocations) {
            each.invoke(target);
        }
    }
}
