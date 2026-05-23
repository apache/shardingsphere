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
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;
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
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
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
     * @return execution response
     * @throws MCPTransactionStateException when the current transaction state blocks execution
     * @throws MCPTimeoutException when the JDBC execution times out
     * @throws MCPUnsupportedException when the JDBC driver or statement class is unsupported
     * @throws MCPInvalidRequestException when the SQL is invalid for the target database
     * @throws MCPQueryFailedException when query execution fails
     * @throws MCPUnavailableException when the runtime database configuration is unavailable
     */
    public SQLExecutionResponse execute(final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult, final MCPDatabaseCapability databaseCapability) {
        Connection connection = null;
        boolean needCloseConnection = false;
        boolean transactionConnectionInUse = false;
        try {
            try {
                Optional<Connection> transactionConnection = transactionResourceManager.findTransactionConnection(executionRequest.getSessionId(), executionRequest.getDatabase());
                if (transactionConnection.isPresent()) {
                    connection = transactionConnection.get();
                    transactionConnectionInUse = true;
                } else {
                    connection = openConnection(executionRequest.getDatabase());
                    needCloseConnection = true;
                }
            } catch (final IllegalStateException ex) {
                throw new MCPTransactionStateException(ex.getMessage(), ex);
            }
            return executeWithConnection(connection, executionRequest, classificationResult, databaseCapability, transactionConnectionInUse);
        } catch (final SQLTimeoutException ex) {
            throw new MCPTimeoutException(ex.getMessage(), ex);
        } catch (final SQLFeatureNotSupportedException ex) {
            throw new MCPUnsupportedException(ex.getMessage(), ex);
        } catch (final SQLSyntaxErrorException ex) {
            throw new MCPInvalidRequestException(ex.getMessage(), ex);
        } catch (final SQLException ex) {
            throw new MCPQueryFailedException(ex.getMessage(), ex);
        } finally {
            if (needCloseConnection && null != connection) {
                try {
                    connection.close();
                } catch (final SQLException ignored) {
                }
            }
        }
    }

    private SQLExecutionResponse executeWithConnection(final Connection connection, final SQLExecutionRequest executionRequest,
                                                       final ClassificationResult classificationResult, final MCPDatabaseCapability databaseCapability,
                                                       final boolean transactionConnectionInUse) throws SQLException {
        applySchema(connection, executionRequest.getSchema(), databaseCapability.getSchemaExecutionSemantics());
        if (executionRequest.isReadOnlyExecution() && !transactionConnectionInUse) {
            return executeWithReadOnlyConnection(connection, executionRequest, classificationResult);
        }
        try (Statement statement = connection.createStatement()) {
            configureStatement(statement, executionRequest);
            return executeStatement(statement, executionRequest, classificationResult);
        }
    }

    private SQLExecutionResponse executeWithReadOnlyConnection(final Connection connection, final SQLExecutionRequest executionRequest,
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

    private SQLExecutionResponse executeStatement(final Statement statement, final SQLExecutionRequest executionRequest,
                                                  final ClassificationResult classificationResult) throws SQLException {
        boolean hasResultSet = statement.execute(classificationResult.getNormalizedSql());
        switch (classificationResult.getStatementClass()) {
            case QUERY:
            case EXPLAIN_ANALYZE:
                if (!hasResultSet) {
                    throw new QueryDidNotReturnResultSetException();
                }
                return withExecutionHints(createResultSetResponse(statement.getResultSet(), executionRequest.getMaxRows(), classificationResult), executionRequest, classificationResult);
            case DML:
                return hasResultSet
                        ? withExecutionHints(createResultSetResponse(statement.getResultSet(), executionRequest.getMaxRows(), classificationResult), executionRequest, classificationResult)
                        : withExecutionHints(SQLExecutionResponse.updateCount(
                                classificationResult.getStatementClass(), classificationResult.getStatementType(), statement.getUpdateCount()), executionRequest, classificationResult);
            case DDL:
            case DCL:
                return withExecutionHints(SQLExecutionResponse.statementAck(
                        classificationResult.getStatementClass(), classificationResult.getStatementType(), "Statement executed."), executionRequest, classificationResult);
            default:
                throw new StatementClassNotSupportedException();
        }
    }

    private SQLExecutionResponse withExecutionHints(final SQLExecutionResponse response, final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        return response.withExecutionHints(executionRequest.getMaxRows(), executionRequest.getTimeoutMs()).withNormalizedSql(classificationResult.getNormalizedSql());
    }

    private SQLExecutionResponse createResultSetResponse(final ResultSet resultSet, final int maxRows, final ClassificationResult classificationResult) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        LinkedList<ExecuteQueryColumnDefinition> columns = new LinkedList<>();
        for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
            columns.add(new ExecuteQueryColumnDefinition(resultSetMetaData.getColumnLabel(index), resultSetMetaData.getColumnTypeName(index),
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
        return SQLExecutionResponse.resultSet(classificationResult.getStatementClass(), classificationResult.getStatementType(), columns, rows, truncated);
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
