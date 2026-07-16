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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.exception.QueryDidNotReturnResultSetException;
import org.apache.shardingsphere.mcp.support.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPDatabaseQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPDatabaseSQLSyntaxException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCExceptionClassifier;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MCP JDBC statement executor.
 */
@RequiredArgsConstructor
public final class MCPJdbcStatementExecutor {
    
    private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    /**
     * Execute one classified request.
     *
     * @param executionRequest execution request
     * @param classificationResult classification result
     * @param databaseCapability database capability
     * @return execution result
     * @throws MCPTransactionStateException when the current transaction state blocks execution
     * @throws MCPTimeoutException when the JDBC execution times out
     * @throws MCPUnsupportedException when the JDBC driver or statement class is unsupported
     * @throws MCPInvalidRequestException when the SQL is invalid for the target database
     * @throws RuleDistSQLExecutionException when rule DistSQL needs workflow-aware recovery
     * @throws MCPQueryFailedException when query execution fails
     * @throws MCPUnavailableException when the runtime database configuration is unavailable
     */
    public SQLExecutionResult execute(final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult, final MCPDatabaseCapability databaseCapability) {
        try {
            Optional<Connection> transactionConnection = findTransactionConnection(executionRequest);
            if (transactionConnection.isPresent()) {
                return executeWithBorrowedConnection(transactionConnection.get(), executionRequest, classificationResult, databaseCapability);
            }
            return executeWithOwnedConnection(openOwnedConnection(executionRequest.getDatabase()), executionRequest, classificationResult, databaseCapability);
        } catch (final SQLException ex) {
            throw createExecutionException(executionRequest, classificationResult, databaseCapability, ex);
        }
    }
    
    private RuntimeException createExecutionException(final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult,
                                                      final MCPDatabaseCapability databaseCapability, final SQLException cause) {
        MCPJDBCErrorCategory category = MCPJDBCExceptionClassifier.classify(databaseCapability.getDatabaseType(), cause);
        switch (category) {
            case TIMEOUT:
                return new MCPTimeoutException(cause.getMessage(), cause);
            case FEATURE_NOT_SUPPORTED:
                return new MCPUnsupportedException(cause.getMessage(), cause);
            case SYNTAX:
                return classificationResult.isRuleDistSQL()
                        ? new RuleDistSQLExecutionException(executionRequest.getDatabase(), classificationResult, cause)
                        : new MCPDatabaseSQLSyntaxException(cause);
            default:
                return new MCPDatabaseQueryFailedException(category, cause);
        }
    }
    
    private Optional<Connection> findTransactionConnection(final SQLExecutionRequest executionRequest) {
        try {
            return transactionResourceManager.findTransactionConnection(executionRequest.getSessionId(), executionRequest.getDatabase());
        } catch (final IllegalStateException ex) {
            throw new MCPTransactionStateException(ex.getMessage(), ex);
        }
    }
    
    private Connection openOwnedConnection(final String databaseName) throws SQLException {
        try {
            return openConnection(databaseName);
        } catch (final IllegalStateException ex) {
            throw new MCPTransactionStateException(ex.getMessage(), ex);
        }
    }
    
    private SQLExecutionResult executeWithBorrowedConnection(final Connection connection, final SQLExecutionRequest executionRequest,
                                                             final ClassificationResult classificationResult, final MCPDatabaseCapability databaseCapability) throws SQLException {
        applySchema(connection, executionRequest.getSchema(), databaseCapability.getSchemaExecutionSemantics());
        return executeWithStatement(connection, executionRequest, classificationResult);
    }
    
    private SQLExecutionResult executeWithOwnedConnection(final Connection connection, final SQLExecutionRequest executionRequest,
                                                          final ClassificationResult classificationResult, final MCPDatabaseCapability databaseCapability) throws SQLException {
        try {
            applySchema(connection, executionRequest.getSchema(), databaseCapability.getSchemaExecutionSemantics());
            return executionRequest.isReadOnlyExecution()
                    ? executeWithReadOnlyConnection(connection, executionRequest, classificationResult)
                    : executeWithStatement(connection, executionRequest, classificationResult);
        } finally {
            closeOwnedConnection(connection);
        }
    }
    
    private SQLExecutionResult executeWithStatement(final Connection connection, final SQLExecutionRequest executionRequest,
                                                    final ClassificationResult classificationResult) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            configureStatement(statement, executionRequest);
            return executeStatement(statement, executionRequest, classificationResult);
        }
    }
    
    private void closeOwnedConnection(final Connection connection) {
        try {
            connection.close();
        } catch (final SQLException ignored) {
        }
    }
    
    private SQLExecutionResult executeWithReadOnlyConnection(final Connection connection, final SQLExecutionRequest executionRequest,
                                                             final ClassificationResult classificationResult) throws SQLException {
        boolean originalReadOnly = connection.isReadOnly();
        boolean originalAutoCommit = connection.getAutoCommit();
        boolean autoCommitDisabled = false;
        SQLException failure = null;
        try {
            connection.setReadOnly(true);
            if (originalAutoCommit) {
                connection.setAutoCommit(false);
                autoCommitDisabled = true;
            }
            try (Statement statement = connection.createStatement()) {
                configureStatement(statement, executionRequest);
                return executeStatement(statement, executionRequest, classificationResult);
            }
        } catch (final SQLException ex) {
            failure = ex;
            throw ex;
        } finally {
            if (autoCommitDisabled || !originalAutoCommit) {
                failure = rollbackReadOnlyConnection(connection, failure);
            }
            if (autoCommitDisabled) {
                failure = restoreAutoCommit(connection, originalAutoCommit, failure);
            }
            failure = restoreReadOnly(connection, originalReadOnly, failure);
            if (null != failure) {
                throw failure;
            }
        }
    }
    
    private SQLException rollbackReadOnlyConnection(final Connection connection, final SQLException previousFailure) {
        try {
            connection.rollback();
            return previousFailure;
        } catch (final SQLException ex) {
            return appendFailure(previousFailure, ex);
        }
    }
    
    private SQLException restoreAutoCommit(final Connection connection, final boolean originalAutoCommit, final SQLException previousFailure) {
        try {
            connection.setAutoCommit(originalAutoCommit);
            return previousFailure;
        } catch (final SQLException ex) {
            return appendFailure(previousFailure, ex);
        }
    }
    
    private SQLException restoreReadOnly(final Connection connection, final boolean originalReadOnly, final SQLException previousFailure) {
        try {
            connection.setReadOnly(originalReadOnly);
            return previousFailure;
        } catch (final SQLException ex) {
            return appendFailure(previousFailure, ex);
        }
    }
    
    private SQLException appendFailure(final SQLException previousFailure, final SQLException failure) {
        if (null == previousFailure) {
            return failure;
        }
        previousFailure.addSuppressed(failure);
        return previousFailure;
    }
    
    private void configureStatement(final Statement statement, final SQLExecutionRequest executionRequest) throws SQLException {
        if (0 < executionRequest.getMaxRows()) {
            statement.setMaxRows(resolveStatementMaxRows(executionRequest.getMaxRows()));
        }
        if (0 < executionRequest.getTimeoutMs()) {
            statement.setQueryTimeout((executionRequest.getTimeoutMs() + 999) / 1000);
        }
    }
    
    private SQLExecutionResult executeStatement(final Statement statement, final SQLExecutionRequest executionRequest,
                                                final ClassificationResult classificationResult) throws SQLException {
        boolean hasResultSet = statement.execute(classificationResult.getNormalizedSql());
        switch (classificationResult.getStatementClass()) {
            case QUERY:
            case EXPLAIN:
                if (!hasResultSet) {
                    throw new QueryDidNotReturnResultSetException();
                }
                return createResultSetResult(statement.getResultSet(), executionRequest, classificationResult);
            case DML:
                return hasResultSet
                        ? createResultSetResult(statement.getResultSet(), executionRequest, classificationResult)
                        : SQLExecutionResult.updateCount(classificationResult.getStatementClass(), classificationResult.getStatementType(), statement.getUpdateCount(),
                                executionRequest.getMaxRows(), executionRequest.getTimeoutMs(), classificationResult.getNormalizedSql());
            case DDL:
            case DCL:
                return SQLExecutionResult.statementAck(classificationResult.getStatementClass(), classificationResult.getStatementType(),
                        executionRequest.getMaxRows(), executionRequest.getTimeoutMs(), classificationResult.getNormalizedSql());
            default:
                throw new StatementClassNotSupportedException();
        }
    }
    
    private SQLExecutionResult createResultSetResult(final ResultSet resultSet, final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        LinkedList<SQLExecutionColumnDefinition> columns = new LinkedList<>();
        for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
            columns.add(new SQLExecutionColumnDefinition(resultSetMetaData.getColumnLabel(index), resultSetMetaData.getColumnTypeName(index),
                    resultSetMetaData.getColumnTypeName(index), ResultSetMetaData.columnNoNulls != resultSetMetaData.isNullable(index)));
        }
        LinkedList<List<Object>> rows = new LinkedList<>();
        boolean truncated = false;
        int effectiveMaxRows = 0 >= executionRequest.getMaxRows() ? Integer.MAX_VALUE : executionRequest.getMaxRows();
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
        return SQLExecutionResult.resultSet(classificationResult.getStatementClass(), classificationResult.getStatementType(), columns, rows, truncated,
                executionRequest.getMaxRows(), executionRequest.getTimeoutMs(), classificationResult.getNormalizedSql());
    }
    
    private int resolveStatementMaxRows(final int maxRows) {
        return Integer.MAX_VALUE == maxRows ? Integer.MAX_VALUE : maxRows + 1;
    }
    
    private Connection openConnection(final String databaseName) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = Optional.ofNullable(runtimeDatabases.get(databaseName))
                .orElseThrow(() -> new MCPUnavailableException(String.format("Database `%s` is not configured.", databaseName)));
        return runtimeDatabaseConfig.openConnection(databaseName);
    }
    
    private void applySchema(final Connection connection, final String schemaName, final SchemaExecutionSemantics schemaExecutionSemantics) throws SQLException {
        String actualSchema = Objects.toString(schemaName, "").trim();
        if (actualSchema.isEmpty() || SchemaExecutionSemantics.FIXED_TO_DATABASE == schemaExecutionSemantics) {
            return;
        }
        connection.setSchema(actualSchema);
    }
}
