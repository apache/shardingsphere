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

package org.apache.shardingsphere.proxy.backend.communication.vertx;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.vertx.ExecutorVertxConnectionManager;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.ConnectionPostProcessor;
import org.apache.shardingsphere.proxy.backend.communication.vertx.transaction.VertxLocalTransactionManager;
import org.apache.shardingsphere.proxy.backend.reactive.context.ReactiveProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Vert.x backend connection.
 */
@Getter
public final class VertxBackendConnection implements BackendConnection<Future<Void>>, ExecutorVertxConnectionManager {
    
    private final ConnectionSession connectionSession;
    
    private final List<ConnectionPostProcessor<Future<SqlConnection>>> connectionPostProcessors = new LinkedList<>();
    
    private final Multimap<String, Future<SqlConnection>> cachedConnections = LinkedHashMultimap.create();
    
    private final AtomicBoolean closed;
    
    public VertxBackendConnection(final ConnectionSession connectionSession) {
        ShardingSpherePreconditions.checkState(TransactionType.LOCAL == connectionSession.getTransactionStatus().getTransactionType(),
                () -> new UnsupportedSQLOperationException("Vert.x backend supports LOCAL transaction only for now"));
        closed = new AtomicBoolean(false);
        this.connectionSession = connectionSession;
    }
    
    @Override
    public List<Future<? extends SqlClient>> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) {
        return connectionSession.getTransactionStatus().isInTransaction() ? getConnectionsWithTransaction(dataSourceName, connectionSize) : getConnectionsWithoutTransaction(dataSourceName);
    }
    
    private List<Future<? extends SqlClient>> getConnectionsWithTransaction(final String dataSourceName, final int connectionSize) {
        Collection<Future<SqlConnection>> connections;
        synchronized (cachedConnections) {
            connections = cachedConnections.get(connectionSession.getDatabaseName() + "." + dataSourceName);
        }
        List<Future<SqlConnection>> result;
        if (connections.size() >= connectionSize) {
            result = new ArrayList<>(connections).subList(0, connectionSize);
        } else if (!connections.isEmpty()) {
            result = new ArrayList<>(connectionSize);
            result.addAll(connections);
            List<Future<SqlConnection>> newConnections = createNewConnections(dataSourceName, connectionSize - connections.size());
            result.addAll(newConnections);
            synchronized (cachedConnections) {
                cachedConnections.putAll(connectionSession.getDatabaseName() + "." + dataSourceName, newConnections);
            }
        } else {
            result = createNewConnections(dataSourceName, connectionSize);
            synchronized (cachedConnections) {
                cachedConnections.putAll(connectionSession.getDatabaseName() + "." + dataSourceName, result);
            }
        }
        return new ArrayList<>(result);
    }
    
    private List<Future<SqlConnection>> createNewConnections(final String dataSourceName, final int connectionSize) {
        Preconditions.checkNotNull(connectionSession.getDatabaseName(), "Current database is null");
        List<Future<SqlConnection>> result = ReactiveProxyContext.getInstance().getVertxBackendDataSource().getConnections(connectionSession.getDatabaseName(), dataSourceName, connectionSize);
        for (Future<SqlConnection> each : result) {
            replayMethodsInvocation(each);
        }
        return result;
    }
    
    private void replayMethodsInvocation(final Future<SqlConnection> target) {
        for (ConnectionPostProcessor<Future<SqlConnection>> each : connectionPostProcessors) {
            each.process(target);
        }
    }
    
    private List<Future<? extends SqlClient>> getConnectionsWithoutTransaction(final String dataSourceName) {
        Preconditions.checkNotNull(connectionSession.getDatabaseName(), "Current database is null");
        // TODO At present, amount of connections without transaction is controlled by Vert.x pool.
        Future<SqlClient> poolFuture = Future.succeededFuture(ReactiveProxyContext.getInstance().getVertxBackendDataSource().getPool(connectionSession.getDatabaseName(), dataSourceName));
        return Collections.singletonList(poolFuture);
    }
    
    @Override
    public Future<Void> prepareForTaskExecution() {
        return Future.succeededFuture();
    }
    
    @Override
    public Future<Void> handleAutoCommit() {
        if (!connectionSession.isAutoCommit() && !connectionSession.getTransactionStatus().isInTransaction()) {
            VertxLocalTransactionManager transactionManager = new VertxLocalTransactionManager(this);
            return transactionManager.begin();
        }
        return Future.succeededFuture();
    }
    
    @Override
    public Future<Void> closeExecutionResources() {
        if (!connectionSession.getTransactionStatus().isInTransaction()) {
            return closeAllConnections(false);
        }
        if (closed.get()) {
            return closeAllConnections(true);
        }
        return Future.succeededFuture();
    }
    
    @Override
    public Future<Void> closeAllResources() {
        closed.set(true);
        return closeAllConnections(true);
    }
    
    @SuppressWarnings("rawtypes")
    private Future<Void> closeAllConnections(final boolean rollbackBeforeClosing) {
        Collection<Future<SqlConnection>> connections = cachedConnections.values();
        if (connections.isEmpty()) {
            return Future.succeededFuture();
        }
        List<Future> closeFutures = new ArrayList<>(connections.size());
        for (Future<SqlConnection> each : connections) {
            closeFutures.add(rollbackBeforeClosing ? each.compose(connection -> connection.query("rollback").execute().compose(unused -> connection.close())) : each.compose(SqlClient::close));
        }
        return CompositeFuture.join(closeFutures).onComplete(unused -> cachedConnections.clear()).compose(unused -> Future.succeededFuture());
    }
    
    /**
     * Execute in all cached connections.
     *
     * @param sql SQL to be executed
     * @return join future
     */
    @SuppressWarnings("rawtypes")
    public Future<Void> executeInAllCachedConnections(final String sql) {
        List<Future> futures = new ArrayList<>(cachedConnections.size());
        for (Future<SqlConnection> each : cachedConnections.values()) {
            futures.add(each.compose(connection -> connection.query(sql).execute()));
        }
        return CompositeFuture.join(futures).compose(result -> Future.succeededFuture());
    }
}
