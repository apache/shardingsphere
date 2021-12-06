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
import io.netty.util.AttributeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.parameter.TypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementSchemaHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.StatementMemoryStrictlyFetchSizeSetter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPI;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Backend connection.
 */
@Getter
@Setter
public final class BackendConnection implements ExecutorJDBCManager {
    
    static {
        ShardingSphereServiceLoader.register(StatementMemoryStrictlyFetchSizeSetter.class);
    }
    
    @Setter(AccessLevel.NONE)
    private volatile String schemaName;
    
    private volatile int connectionId;
    
    private volatile Grantee grantee;
    
    private volatile FederationExecutor federationExecutor;
    
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    private final Collection<DatabaseCommunicationEngine> databaseCommunicationEngines = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<DatabaseCommunicationEngine> inUseDatabaseCommunicationEngines = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    
    private final Collection<ConnectionPostProcessor> connectionPostProcessors = new LinkedList<>();
    
    private final ResourceLock resourceLock = new ResourceLock();
    
    private final ConnectionStatus connectionStatus = new ConnectionStatus();
    
    private final TransactionStatus transactionStatus;
    
    private final Map<String, StatementMemoryStrictlyFetchSizeSetter> fetchSizeSetters;
    
    private final AttributeMap attributeMap;

    private boolean autoCommit = true;
    
    public BackendConnection(final TransactionType initialTransactionType, final AttributeMap attributeMap) {
        transactionStatus = new TransactionStatus(initialTransactionType);
        fetchSizeSetters = ShardingSphereServiceLoader.getSingletonServiceInstances(StatementMemoryStrictlyFetchSizeSetter.class).stream()
                .collect(Collectors.toMap(TypedSPI::getType, Function.identity()));
        this.attributeMap = attributeMap;
    }
    
    /**
     * Change schema of current channel.
     *
     * @param schemaName schema name
     */
    public void setCurrentSchema(final String schemaName) {
        if (null != schemaName && schemaName.equals(this.schemaName)) {
            return;
        }
        if (transactionStatus.isInTransaction()) {
            throw new ShardingSphereException("Failed to switch schema, please terminate current transaction.");
        }
        this.schemaName = schemaName;
    }
    
    /**
     * Get schema name.
     * 
     * @return schema name
     */
    public String getSchemaName() {
        return null == SQLStatementSchemaHolder.get() ? schemaName : SQLStatementSchemaHolder.get();
    }
    
    /**
     * Get default schema name.
     *
     * @return default schema name
     */
    public String getDefaultSchemaName() {
        return schemaName;
    }
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        return transactionStatus.isInTransaction()
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
        Preconditions.checkNotNull(getSchemaName(), "Current schema is null.");
        List<Connection> result = ProxyContext.getInstance().getBackendDataSource().getConnections(getSchemaName(), dataSourceName, connectionSize, connectionMode);
        for (Connection each : result) {
            replayMethodsInvocation(each);
        }
        return result;
    }
    
    private List<Connection> getConnectionsWithoutTransaction(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        Preconditions.checkNotNull(getSchemaName(), "Current schema is null.");
        List<Connection> result = ProxyContext.getInstance().getBackendDataSource().getConnections(getSchemaName(), dataSourceName, connectionSize, connectionMode);
        synchronized (cachedConnections) {
            cachedConnections.putAll(dataSourceName, result);
        }
        return result;
    }
    
    private void replayMethodsInvocation(final Connection target) {
        for (ConnectionPostProcessor each : connectionPostProcessors) {
            each.process(target);
        }
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
        DatabaseType databaseType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(getSchemaName()).getResource().getDatabaseType();
        if (fetchSizeSetters.containsKey(databaseType.getName())) {
            fetchSizeSetters.get(databaseType.getName()).setFetchSize(statement);
        }
    }
    
    /**
     * Whether execute SQL serial or not.
     *
     * @return true or false
     */
    public boolean isSerialExecute() {
        return transactionStatus.isInTransaction() && (TransactionType.LOCAL == transactionStatus.getTransactionType() || TransactionType.XA == transactionStatus.getTransactionType());
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
     * Add database communication engine.
     *
     * @param databaseCommunicationEngine database communication engine to be added
     */
    public void add(final DatabaseCommunicationEngine databaseCommunicationEngine) {
        databaseCommunicationEngines.add(databaseCommunicationEngine);
    }
    
    /**
     * Mark a database communication engine as in use.
     *
     * @param databaseCommunicationEngine database communication engine to be added
     */
    public void markResourceInUse(final DatabaseCommunicationEngine databaseCommunicationEngine) {
        inUseDatabaseCommunicationEngines.add(databaseCommunicationEngine);
    }
    
    /**
     * Unmark an in use database communication engine.
     *
     * @param databaseCommunicationEngine database communication engine to be added
     */
    public void unmarkResourceInUse(final DatabaseCommunicationEngine databaseCommunicationEngine) {
        inUseDatabaseCommunicationEngines.remove(databaseCommunicationEngine);
    }
    
    /**
     * Close database communication engines.
     *
     * @param includeInUse include engines in use
     * @return SQL exception when engine close
     */
    public synchronized Collection<SQLException> closeDatabaseCommunicationEngines(final boolean includeInUse) {
        Collection<SQLException> result = new LinkedList<>();
        for (DatabaseCommunicationEngine each : databaseCommunicationEngines) {
            if (!includeInUse && inUseDatabaseCommunicationEngines.contains(each)) {
                continue;
            }
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        if (includeInUse) {
            inUseDatabaseCommunicationEngines.clear();
        }
        databaseCommunicationEngines.retainAll(inUseDatabaseCommunicationEngines);
        return result;
    }
    
    /**
     * Close connections.
     * 
     * @param forceRollback is force rollback
     * @return SQL exception when connections close
     */
    public synchronized Collection<SQLException> closeConnections(final boolean forceRollback) {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                if (forceRollback && transactionStatus.isInTransaction()) {
                    each.rollback();
                }
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        cachedConnections.clear();
        connectionPostProcessors.clear();
        return result;
    }
    
    /**
     * Close federation executor.
     * 
     * @return SQL exception when federation executor close
     */
    public synchronized Collection<SQLException> closeFederationExecutor() {
        Collection<SQLException> result = new LinkedList<>();
        if (null != federationExecutor) {
            try {
                federationExecutor.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
}
