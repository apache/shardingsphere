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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.masterslave.route.engine.impl.MasterVisitedManager;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Backend connection.
 *
 * @author zhaojun
 * @author zhangliang
 * @author liya
 */
@Getter
@Slf4j
public final class BackendConnection implements AutoCloseable {
    
    private static final int MAXIMUM_RETRY_COUNT = 5;
    
    private volatile String schemaName;
    
    private LogicSchema logicSchema;
    
    private TransactionType transactionType;
    
    private boolean supportHint;
    
    @Setter
    private int connectionId;
    
    @Setter
    private String userName;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
    private final Collection<MethodInvocation> methodInvocations = new ArrayList<>();
    
    @Getter
    private final ResourceSynchronizer resourceSynchronizer = new ResourceSynchronizer();
    
    private final ConnectionStateHandler stateHandler = new ConnectionStateHandler(resourceSynchronizer);
    
    public BackendConnection(final TransactionType transactionType) {
        this.transactionType = transactionType;
        this.supportHint = false;
    }
    
    public BackendConnection(final TransactionType transactionType, final boolean supportHint) {
        this.transactionType = transactionType;
        this.supportHint = supportHint;
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
     * Change logic schema of current channel.
     *
     * @param schemaName schema name
     */
    public void setCurrentSchema(final String schemaName) {
        if (isSwitchFailed()) {
            throw new ShardingSphereException("Failed to switch schema, please terminate current transaction.");
        }
        this.schemaName = schemaName;
        this.logicSchema = LogicSchemas.getInstance().getLogicSchema(schemaName);
    }
    
    @SneakyThrows
    private boolean isSwitchFailed() {
        int retryCount = 0;
        while (stateHandler.isInTransaction() && retryCount < MAXIMUM_RETRY_COUNT) {
            resourceSynchronizer.doAwaitUntil();
            ++retryCount;
            log.warn("Current transaction have not terminated, retry count:[{}].", retryCount);
        }
        if (retryCount >= MAXIMUM_RETRY_COUNT) {
            log.error("Cannot do switch, exceed maximum retry count:[{}].", MAXIMUM_RETRY_COUNT);
            return true;
        }
        return false;
    }
    
    /**
     * Get connections of current thread datasource.
     *
     * @param connectionMode connection mode
     * @param dataSourceName data source name
     * @param connectionSize size of connections to be get
     * @return connections
     * @throws SQLException SQL exception
     */
    public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        if (stateHandler.isInTransaction()) {
            return getConnectionsWithTransaction(connectionMode, dataSourceName, connectionSize);
        } else {
            return getConnectionsWithoutTransaction(connectionMode, dataSourceName, connectionSize);
        }
    }
    
    private List<Connection> getConnectionsWithTransaction(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
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
            List<Connection> newConnections = createNewConnections(connectionMode, dataSourceName, connectionSize - connections.size());
            result.addAll(newConnections);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, newConnections);
            }
        } else {
            result = createNewConnections(connectionMode, dataSourceName, connectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(dataSourceName, result);
            }
        }
        return result;
    }
    
    private List<Connection> getConnectionsWithoutTransaction(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        Preconditions.checkNotNull(logicSchema, "current logic schema is null");
        List<Connection> result = getConnectionFromUnderlying(connectionMode, dataSourceName, connectionSize);
        synchronized (cachedConnections) {
            cachedConnections.putAll(dataSourceName, result);
        }
        return result;
    }
    
    private List<Connection> createNewConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        Preconditions.checkNotNull(logicSchema, "current logic schema is null");
        List<Connection> result = getConnectionFromUnderlying(connectionMode, dataSourceName, connectionSize);
        for (Connection each : result) {
            replayMethodsInvocation(each);
        }
        return result;
    }
    
    private List<Connection> getConnectionFromUnderlying(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        return logicSchema.getBackendDataSource().getConnections(connectionMode, dataSourceName, connectionSize, transactionType);
    }
    
    /**
     * Whether execute SQL serial or not.
     *
     * @return true or false
     */
    public boolean isSerialExecute() {
        return stateHandler.isInTransaction() && (TransactionType.LOCAL == transactionType || TransactionType.XA == transactionType);
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
        if (!stateHandler.isInTransaction() || forceClose) {
            exceptions.addAll(releaseConnections(forceClose));
        }
        stateHandler.doNotifyIfNecessary();
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
    
    Collection<SQLException> releaseConnections(final boolean forceRollback) {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                if (forceRollback && stateHandler.isInTransaction()) {
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
        SQLException ex = new SQLException();
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
