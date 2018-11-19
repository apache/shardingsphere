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

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import io.shardingsphere.spi.transaction.ShardingTransactionHandler;
import io.shardingsphere.spi.transaction.ShardingTransactionHandlerRegistry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

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
 */
@NoArgsConstructor
public final class BackendConnection implements AutoCloseable {
    
    @Getter
    @Setter
    private LogicSchema logicSchema;
    
    private final Multimap<String, Connection> cachedConnections = HashMultimap.create();
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
    private final Collection<MethodInvocation> methodInvocations = new ArrayList<>();
    
    private ConnectionStatus status = ConnectionStatus.INIT;
    
    @Getter
    @Setter
    private TransactionType transactionType = GlobalRegistry.getInstance().getTransactionType();
    
    /**
     * Get connection size.
     * 
     * @return connection size
     */
    public int getConnectionSize() {
        return cachedConnections.values().size();
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
        if (ConnectionStatus.INIT == status || ConnectionStatus.TERMINATED == status) {
            status = ConnectionStatus.RUNNING;
        }
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
    
    private List<Connection> createNewConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
        List<Connection> result = logicSchema.getBackendDataSource().getConnections(connectionMode, dataSourceName, connectionSize);
        for (Connection each : result) {
            replayMethodsInvocation(each);
        }
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
        MasterVisitedManager.clear();
        exceptions.addAll(closeStatements());
        exceptions.addAll(closeResultSets());
        if (ConnectionStatus.TERMINATED == status) {
            exceptions.addAll(closeConnections());
            cachedConnections.clear();
            methodInvocations.clear();
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    /**
     * set auto commit.
     *
     * @param autoCommit auto commit
     */
    private void setAutoCommit(final boolean autoCommit) {
        if (!autoCommit) {
            status = ConnectionStatus.TRANSACTION;
        }
        cachedConnections.clear();
        recordMethodInvocation(Connection.class, "setAutoCommit", new Class[]{boolean.class}, new Object[]{autoCommit});
    }
    
    /**
     * Execute transaction by operation type.
     *
     * @param operationType operation type
     * @throws SQLException SQL exception
     */
    public void doInTransactional(final TransactionOperationType operationType) throws SQLException {
        ShardingTransactionHandler<ShardingTransactionEvent> shardingTransactionHandler = ShardingTransactionHandlerRegistry.getInstance().getHandler(transactionType);
        if (null != transactionType && transactionType != TransactionType.LOCAL) {
            Preconditions.checkNotNull(shardingTransactionHandler, String.format("Cannot find transaction manager of [%s]", transactionType));
        }
        if (TransactionType.LOCAL == transactionType) {
            switch (operationType) {
                case BEGIN:
                    setAutoCommit(false);
                    break;
                case COMMIT:
                    commit();
                    break;
                case ROLLBACK:
                    rollback();
                    break;
                default:
            }
        } else if (TransactionType.XA == transactionType) {
            shardingTransactionHandler.doInTransaction(new XATransactionEvent(operationType));
        }
    }
    
    /**
     * Do commit.
     *
     * @throws SQLException SQL exception
     */
    private void commit() throws SQLException {
        if (ConnectionStatus.TRANSACTION == status) {
            Collection<SQLException> exceptions = new LinkedList<>();
            exceptions.addAll(commitConnections());
            throwSQLExceptionIfNecessary(exceptions);
            status = ConnectionStatus.TERMINATED;
        }
    }
    
    /**
     * Do rollback.
     *
     * @throws SQLException SQL exception
     */
    private void rollback() throws SQLException {
        if (ConnectionStatus.TRANSACTION == status) {
            Collection<SQLException> exceptions = new LinkedList<>();
            exceptions.addAll(rollbackConnections());
            throwSQLExceptionIfNecessary(exceptions);
            status = ConnectionStatus.TERMINATED;
        }
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
    
    private Collection<SQLException> closeConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                each.close();
            } catch (SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
    
    private Collection<SQLException> commitConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                each.commit();
            } catch (SQLException ex) {
                result.add(ex);
            }
        }
        return result;
    }
    
    private Collection<SQLException> rollbackConnections() {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                each.rollback();
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
    
    @SneakyThrows
    private void recordMethodInvocation(final Class<?> targetClass, final String methodName, final Class<?>[] argumentTypes, final Object[] arguments) {
        methodInvocations.add(new MethodInvocation(targetClass.getMethod(methodName, argumentTypes), arguments));
    }
    
    private void replayMethodsInvocation(final Object target) {
        for (MethodInvocation each : methodInvocations) {
            each.invoke(target);
        }
    }
}
