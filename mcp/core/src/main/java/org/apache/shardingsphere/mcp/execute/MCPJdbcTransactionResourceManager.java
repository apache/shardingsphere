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

package org.apache.shardingsphere.mcp.execute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP JDBC transaction resource manager.
 */
@RequiredArgsConstructor
public final class MCPJdbcTransactionResourceManager {
    
    private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private final Map<String, TransactionResourceContext> transactionResources = new ConcurrentHashMap<>();
    
    /**
     * Find the transaction-bound connection for the session.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @return transaction-bound connection when present
     */
    public Optional<Connection> findTransactionConnection(final String sessionId, final String databaseName) {
        Optional<TransactionResourceContext> transactionResource = findTransactionResourceContext(sessionId);
        if (transactionResource.isEmpty()) {
            return Optional.empty();
        }
        ShardingSpherePreconditions.checkState(transactionResource.get().getDatabaseName().equals(databaseName),
                () -> new IllegalStateException("Cross-database transaction switching is not supported."));
        return Optional.of(transactionResource.get().getConnection());
    }
    
    /**
     * Begin one session transaction.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @throws IllegalStateException when the transaction is already active or the connection cannot be opened
     */
    public void beginTransaction(final String sessionId, final String databaseName) {
        Optional<TransactionResourceContext> transactionResource = findTransactionResourceContext(sessionId);
        if (transactionResource.isPresent()) {
            ShardingSpherePreconditions.checkState(transactionResource.get().getDatabaseName().equals(databaseName),
                    () -> new IllegalStateException("Cross-database transaction switching is not supported."));
            throw new IllegalStateException("Transaction already active.");
        }
        try {
            Connection connection = openConnection(databaseName);
            connection.setAutoCommit(false);
            transactionResources.put(sessionId, new TransactionResourceContext(databaseName, connection));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Commit one session transaction.
     *
     * @param sessionId session identifier
     * @throws IllegalStateException when commit fails
     */
    public void commitTransaction(final String sessionId) {
        TransactionResourceContext transactionResource = getTransactionResourceContext(sessionId);
        try {
            transactionResource.getConnection().commit();
            transactionResource.getConnection().setAutoCommit(true);
            transactionResource.getConnection().close();
            transactionResources.remove(sessionId);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Roll back one session transaction.
     *
     * @param sessionId session identifier
     */
    public void rollbackTransaction(final String sessionId) {
        rollbackAndClose(sessionId, getTransactionResourceContext(sessionId));
    }
    
    /**
     * Create one savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     * @throws IllegalStateException when the savepoint cannot be created
     */
    public void createSavepoint(final String sessionId, final String savepointName) {
        TransactionResourceContext transactionResource = getTransactionResourceContext(sessionId);
        String actualSavepointName = normalizeSavepointName(savepointName);
        try {
            transactionResource.addSavepoint(actualSavepointName, transactionResource.getConnection().setSavepoint(actualSavepointName));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Roll back to one savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     * @throws IllegalStateException when the savepoint does not exist or rollback fails
     */
    public void rollbackToSavepoint(final String sessionId, final String savepointName) {
        TransactionResourceContext transactionResource = getTransactionResourceContext(sessionId);
        Savepoint savepoint = transactionResource.findSavepoint(normalizeSavepointName(savepointName)).orElseThrow(() -> new IllegalStateException("Savepoint does not exist."));
        try {
            transactionResource.getConnection().rollback(savepoint);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Release one savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     * @throws IllegalStateException when the savepoint does not exist or release fails
     */
    public void releaseSavepoint(final String sessionId, final String savepointName) {
        TransactionResourceContext transactionResource = getTransactionResourceContext(sessionId);
        String actualSavepointName = normalizeSavepointName(savepointName);
        Savepoint savepoint = transactionResource.findSavepoint(actualSavepointName).orElseThrow(() -> new IllegalStateException("Savepoint does not exist."));
        try {
            transactionResource.getConnection().releaseSavepoint(savepoint);
            transactionResource.removeSavepoint(actualSavepointName);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Close one session and roll back pending work.
     *
     * @param sessionId session identifier
     */
    public void closeSession(final String sessionId) {
        Optional<TransactionResourceContext> transactionResource = findTransactionResourceContext(sessionId);
        transactionResource.ifPresent(optional -> rollbackAndClose(sessionId, optional));
    }
    
    private Optional<TransactionResourceContext> findTransactionResourceContext(final String sessionId) {
        return Optional.ofNullable(transactionResources.get(sessionId));
    }
    
    private TransactionResourceContext getTransactionResourceContext(final String sessionId) {
        return findTransactionResourceContext(sessionId).orElseThrow(() -> new IllegalStateException("No active transaction."));
    }
    
    private void rollbackAndClose(final String sessionId, final TransactionResourceContext transactionResource) {
        try {
            transactionResource.getConnection().rollback();
            transactionResource.getConnection().setAutoCommit(true);
            transactionResource.getConnection().close();
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        } finally {
            transactionResources.remove(sessionId);
        }
    }
    
    private Connection openConnection(final String databaseName) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = Optional.ofNullable(runtimeDatabases.get(databaseName))
                .orElseThrow(() -> new IllegalStateException(String.format("Database `%s` is not configured.", databaseName)));
        return runtimeDatabaseConfig.openConnection(databaseName);
    }
    
    private String normalizeSavepointName(final String savepointName) {
        String result = Objects.toString(savepointName, "").trim().toUpperCase(Locale.ENGLISH);
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException("savepointName cannot be empty."));
        return result;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class TransactionResourceContext {
        
        private final String databaseName;
        
        private final Connection connection;
        
        private final Map<String, Savepoint> savepoints = new ConcurrentHashMap<>();
        
        private void addSavepoint(final String savepointName, final Savepoint savepoint) {
            savepoints.put(savepointName, savepoint);
        }
        
        private Optional<Savepoint> findSavepoint(final String savepointName) {
            return Optional.ofNullable(savepoints.get(savepointName));
        }
        
        private void removeSavepoint(final String savepointName) {
            savepoints.remove(savepointName);
        }
    }
}
