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
import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.exception.QueryDidNotReturnResultSetException;
import org.apache.shardingsphere.mcp.protocol.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;

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
     * @return execution response
     * @throws MCPTransactionStateException when the current transaction state blocks execution
     * @throws MCPTimeoutException when the JDBC execution times out
     * @throws MCPUnsupportedException when the JDBC driver or statement class is unsupported
     * @throws MCPInvalidRequestException when the SQL is invalid for the target database
     * @throws MCPQueryFailedException when query execution fails
     * @throws MCPUnavailableException when the runtime database configuration is unavailable
     */
    public ExecuteQueryResponse execute(final ExecutionRequest executionRequest, final ClassificationResult classificationResult) {
        Connection connection = null;
        boolean needCloseConnection = false;
        try {
            try {
                Optional<Connection> transactionConnection = transactionResourceManager.findTransactionConnection(executionRequest.getSessionId(), executionRequest.getDatabase());
                if (transactionConnection.isPresent()) {
                    connection = transactionConnection.get();
                } else {
                    connection = openConnection(executionRequest.getDatabase());
                    needCloseConnection = true;
                }
            } catch (final IllegalStateException ex) {
                throw new MCPTransactionStateException(ex.getMessage(), ex);
            }
            applySchema(connection, executionRequest.getSchema());
            try (Statement statement = connection.createStatement()) {
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
                            throw new QueryDidNotReturnResultSetException();
                        }
                        return createResultSetResponse(statement.getResultSet(), executionRequest.getMaxRows());
                    case DML:
                        return ExecuteQueryResponse.updateCount(classificationResult.getStatementType(), statement.getUpdateCount());
                    case DDL:
                    case DCL:
                        return ExecuteQueryResponse.statementAck(classificationResult.getStatementType(), "Statement executed.");
                    default:
                        throw new StatementClassNotSupportedException();
                }
            }
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
    
    private ExecuteQueryResponse createResultSetResponse(final ResultSet resultSet, final int maxRows) throws SQLException {
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
        return ExecuteQueryResponse.resultSet(columns, rows, truncated);
    }
    
    private Connection openConnection(final String databaseName) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = Optional.ofNullable(runtimeDatabases.get(databaseName))
                .orElseThrow(() -> new MCPUnavailableException(String.format("Database `%s` is not configured.", databaseName)));
        return runtimeDatabaseConfig.openConnection(databaseName);
    }
    
    private void applySchema(final Connection connection, final String schemaName) throws SQLException {
        String actualSchema = Objects.toString(schemaName, "").trim();
        if (actualSchema.isEmpty()) {
            return;
        }
        try {
            connection.setSchema(actualSchema);
        } catch (final SQLFeatureNotSupportedException ignored) {
        }
    }
}
