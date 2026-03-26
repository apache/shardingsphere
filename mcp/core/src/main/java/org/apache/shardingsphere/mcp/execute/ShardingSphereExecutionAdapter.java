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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.protocol.ColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real JDBC-backed execution adapter for the MCP runtime.
 */
@RequiredArgsConstructor
public final class ShardingSphereExecutionAdapter {
    
    private final Map<String, ConnectionProvider> connectionProviders;
    
    private final Map<String, SessionConnectionContext> sessionConnections = new ConcurrentHashMap<>();
    
    /**
     * Execute one classified request.
     *
     * @param executionRequest execution request
     * @param classificationResult classification result
     * @return execution response
     */
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        Connection connection = null;
        boolean closeConnection = false;
        try {
            Optional<SessionConnectionContext> sessionConnectionContext = findSessionConnection(executionRequest.getSessionId());
            if (sessionConnectionContext.isPresent()) {
                if (!sessionConnectionContext.get().getDatabase().equals(executionRequest.getDatabase())) {
                    return ExecuteQueryResponse.error(ErrorCode.TRANSACTION_STATE_ERROR, "Cross-database transaction switching is not supported.");
                }
                connection = sessionConnectionContext.get().getConnection();
            } else {
                connection = openConnection(executionRequest.getDatabase());
                closeConnection = true;
            }
            applySchema(connection, executionRequest.getSchema());
            Statement statement = connection.createStatement();
            if (0 < executionRequest.getMaxRows()) {
                statement.setMaxRows(executionRequest.getMaxRows());
            }
            if (0 < executionRequest.getTimeoutMs()) {
                statement.setQueryTimeout((executionRequest.getTimeoutMs() + 999) / 1000);
            }
            boolean hasResultSet = statement.execute(classificationResult.getNormalizedSql());
            switch (classificationResult.getStatementClass()) {
                case QUERY:
                case EXPLAIN_ANALYZE:
                    if (!hasResultSet) {
                        return ExecuteQueryResponse.error(ErrorCode.QUERY_FAILED, "Query did not return a result set.");
                    }
                    return createResultSetResponse(statement.getResultSet(), executionRequest.getMaxRows());
                case DML:
                    return ExecuteQueryResponse.updateCount(classificationResult.getStatementType(), statement.getUpdateCount());
                case DDL:
                case DCL:
                    return ExecuteQueryResponse.statementAck(classificationResult.getStatementType(), "Statement executed.");
                default:
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Statement class is not supported.");
            }
        } catch (final SQLTimeoutException ex) {
            return ExecuteQueryResponse.error(ErrorCode.TIMEOUT, ex.getMessage());
        } catch (final SQLFeatureNotSupportedException ex) {
            return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, ex.getMessage());
        } catch (final SQLSyntaxErrorException ex) {
            return ExecuteQueryResponse.error(ErrorCode.INVALID_REQUEST, ex.getMessage());
        } catch (final SQLException ex) {
            return ExecuteQueryResponse.error(ErrorCode.QUERY_FAILED, ex.getMessage());
        } finally {
            if (closeConnection && null != connection) {
                try {
                    connection.close();
                } catch (final SQLException ignored) {
                }
            }
        }
    }
    
    /**
     * Begin one session transaction.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @throws IllegalStateException when the transaction is already active or the connection cannot be opened
     */
    public void beginTransaction(final String sessionId, final String database) {
        if (sessionConnections.containsKey(sessionId)) {
            throw new IllegalStateException("Transaction already active.");
        }
        try {
            Connection connection = openConnection(database);
            connection.setAutoCommit(false);
            sessionConnections.put(sessionId, new SessionConnectionContext(database, connection));
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
        SessionConnectionContext sessionConnectionContext = getRequiredSessionConnection(sessionId);
        try {
            sessionConnectionContext.getConnection().commit();
            sessionConnectionContext.getConnection().setAutoCommit(true);
            sessionConnectionContext.getConnection().close();
            sessionConnections.remove(sessionId);
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
        SessionConnectionContext sessionConnectionContext = getRequiredSessionConnection(sessionId);
        rollbackAndClose(sessionId, sessionConnectionContext);
    }
    
    /**
     * Create one savepoint.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     * @throws IllegalStateException when the savepoint cannot be created
     */
    public void createSavepoint(final String sessionId, final String savepointName) {
        SessionConnectionContext sessionConnectionContext = getRequiredSessionConnection(sessionId);
        try {
            sessionConnectionContext.addSavepoint(savepointName, sessionConnectionContext.getConnection().setSavepoint(savepointName));
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
        SessionConnectionContext sessionConnectionContext = getRequiredSessionConnection(sessionId);
        Savepoint savepoint = sessionConnectionContext.findSavepoint(savepointName)
                .orElseThrow(() -> new IllegalStateException("Savepoint does not exist."));
        try {
            sessionConnectionContext.getConnection().rollback(savepoint);
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
        SessionConnectionContext sessionConnectionContext = getRequiredSessionConnection(sessionId);
        Savepoint savepoint = sessionConnectionContext.findSavepoint(savepointName)
                .orElseThrow(() -> new IllegalStateException("Savepoint does not exist."));
        try {
            sessionConnectionContext.getConnection().releaseSavepoint(savepoint);
            sessionConnectionContext.removeSavepoint(savepointName);
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
        Optional<SessionConnectionContext> sessionConnectionContext = findSessionConnection(sessionId);
        sessionConnectionContext.ifPresent(optional -> rollbackAndClose(sessionId, optional));
    }
    
    private ExecuteQueryResponse createResultSetResponse(final ResultSet resultSet, final int maxRows) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        LinkedList<ColumnDefinition> columns = new LinkedList<>();
        for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
            columns.add(new ColumnDefinition(resultSetMetaData.getColumnLabel(index), resultSetMetaData.getColumnTypeName(index),
                    resultSetMetaData.getColumnTypeName(index), ResultSetMetaData.columnNoNulls != resultSetMetaData.isNullable(index)));
        }
        LinkedList<List<Object>> rows = new LinkedList<>();
        boolean truncated = false;
        int effectiveMaxRows = 0 >= maxRows ? Integer.MAX_VALUE : maxRows;
        while (resultSet.next()) {
            if (rows.size() >= effectiveMaxRows) {
                truncated = true;
                break;
            }
            LinkedList<Object> row = new LinkedList<>();
            for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
                row.add(resultSet.getObject(index));
            }
            rows.add(row);
        }
        return ExecuteQueryResponse.resultSet(columns, rows, truncated);
    }
    
    private Connection openConnection(final String database) throws SQLException {
        ConnectionProvider connectionProvider = Optional.ofNullable(connectionProviders.get(database))
                .orElseThrow(() -> new IllegalStateException(String.format("Database `%s` is not configured.", database)));
        return connectionProvider.getConnection();
    }
    
    private void applySchema(final Connection connection, final String schema) throws SQLException {
        String actualSchema = null == schema ? "" : schema.trim();
        if (actualSchema.isEmpty()) {
            return;
        }
        try {
            connection.setSchema(actualSchema);
        } catch (final SQLFeatureNotSupportedException ignored) {
        }
    }
    
    private Optional<SessionConnectionContext> findSessionConnection(final String sessionId) {
        return Optional.ofNullable(sessionConnections.get(Objects.requireNonNull(sessionId, "sessionId cannot be null")));
    }
    
    private SessionConnectionContext getRequiredSessionConnection(final String sessionId) {
        return findSessionConnection(sessionId).orElseThrow(() -> new IllegalStateException("No active transaction."));
    }
    
    private void rollbackAndClose(final String sessionId, final SessionConnectionContext sessionConnectionContext) {
        try {
            sessionConnectionContext.getConnection().rollback();
            sessionConnectionContext.getConnection().setAutoCommit(true);
            sessionConnectionContext.getConnection().close();
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        } finally {
            sessionConnections.remove(sessionId);
        }
    }
    
    /**
     * Open one JDBC connection.
     */
    @FunctionalInterface
    public interface ConnectionProvider {
        
        /**
         * Get one connection.
         *
         * @return opened connection
         * @throws SQLException when connection open fails
         */
        Connection getConnection() throws SQLException;
    }
    
    private static final class SessionConnectionContext {
        
        private final String database;
        
        private final Connection connection;
        
        private final Map<String, Savepoint> savepoints = new ConcurrentHashMap<>();
        
        private SessionConnectionContext(final String database, final Connection connection) {
            this.database = database;
            this.connection = connection;
        }
        
        private String getDatabase() {
            return database;
        }
        
        private Connection getConnection() {
            return connection;
        }
        
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
