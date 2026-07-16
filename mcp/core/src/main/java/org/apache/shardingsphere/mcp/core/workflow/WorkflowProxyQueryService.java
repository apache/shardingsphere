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

package org.apache.shardingsphere.mcp.core.workflow;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPDatabaseQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCExceptionClassifier;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Direct query service for Proxy-backed workflow reads.
 */
@RequiredArgsConstructor
public final class WorkflowProxyQueryService implements MCPFeatureQueryFacade {
    
    private static final String DEFAULT_COLUMN_DEFINITION = "VARCHAR(4000)";
    
    private final MCPSessionManager sessionManager;
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    @Override
    public List<Map<String, Object>> query(final String databaseName, final String schemaName, final String sql) {
        String actualDatabaseName = WorkflowSQLUtils.normalizeIdentifier(databaseName);
        try (
                Connection connection = openConnection(actualDatabaseName);
                Statement statement = connection.createStatement();
                ResultSet resultSet = executeQuery(connection, statement, schemaName, sql)) {
            return extractRows(resultSet);
        } catch (final SQLException ex) {
            throw createQueryFailedException(actualDatabaseName, ex);
        }
    }
    
    @Override
    public List<Map<String, Object>> queryWithAnyDatabase(final String sql) {
        String databaseName = sessionManager.getTransactionResourceManager().getRuntimeDatabases().keySet().stream().findFirst()
                .orElseThrow(() -> new MCPUnavailableException("No runtime database is configured."));
        return query(databaseName, "", sql);
    }
    
    @Override
    public void checkDatabaseCapability(final String databaseName) {
        getDatabaseCapability(databaseName);
    }
    
    @Override
    public boolean isSameIdentifier(final String databaseName, final IdentifierScope identifierScope, final String identifier, final String existingIdentifier) {
        return WorkflowSQLUtils.isSameIdentifier(getDatabaseCapability(databaseName).getIdentifierCasePolicySet().getPolicy(identifierScope), identifier, existingIdentifier);
    }
    
    @Override
    public String queryColumnDefinition(final String databaseName, final String schemaName, final String tableName, final String columnName) {
        WorkflowSQLUtils.checkSupportedIdentifier("database", databaseName);
        WorkflowSQLUtils.checkSupportedIdentifier("schema", schemaName);
        String actualDatabaseName = WorkflowSQLUtils.normalizeIdentifier(databaseName);
        String actualSchemaName = WorkflowSQLUtils.normalizeIdentifier(schemaName);
        String databaseType = getDatabaseType(actualDatabaseName);
        String sql = String.format("SELECT %s FROM %s WHERE 1 = 0", WorkflowSQLUtils.formatSQLIdentifier(databaseType, columnName), WorkflowSQLUtils.formatSQLIdentifier(databaseType, tableName));
        try (
                Connection connection = openConnection(actualDatabaseName);
                Statement statement = connection.createStatement();
                ResultSet resultSet = executeQuery(connection, statement, actualSchemaName, sql)) {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            if (0 == resultSetMetaData.getColumnCount()) {
                return DEFAULT_COLUMN_DEFINITION;
            }
            return formatColumnDefinition(resultSetMetaData.getColumnType(1), resultSetMetaData.getColumnTypeName(1),
                    resultSetMetaData.getPrecision(1), resultSetMetaData.getScale(1));
        } catch (final SQLException ex) {
            throw new MCPDatabaseQueryFailedException(MCPJDBCExceptionClassifier.classify(databaseType, ex), ex);
        }
    }
    
    private MCPDatabaseQueryFailedException createQueryFailedException(final String databaseName, final SQLException cause) {
        Optional<MCPDatabaseCapability> databaseCapability = databaseCapabilityProvider.provide(databaseName);
        MCPJDBCErrorCategory category = databaseCapability.isPresent()
                ? MCPJDBCExceptionClassifier.classify(databaseCapability.get().getDatabaseType(), cause)
                : MCPJDBCExceptionClassifier.classify(cause);
        return new MCPDatabaseQueryFailedException(category, cause);
    }
    
    private Connection openConnection(final String databaseName) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = sessionManager.getTransactionResourceManager().getRuntimeDatabases().get(databaseName);
        if (null == runtimeDatabaseConfig) {
            throw new MCPUnavailableException(String.format("Database `%s` is not configured.", databaseName));
        }
        return runtimeDatabaseConfig.openConnection(databaseName);
    }
    
    private String getDatabaseType(final String databaseName) {
        return getDatabaseCapability(databaseName).getDatabaseType();
    }
    
    private MCPDatabaseCapability getDatabaseCapability(final String databaseName) {
        return databaseCapabilityProvider.provide(WorkflowSQLUtils.normalizeIdentifier(databaseName)).orElseThrow(DatabaseCapabilityNotFoundException::new);
    }
    
    private ResultSet executeQuery(final Connection connection, final Statement statement, final String schemaName, final String sql) throws SQLException {
        String actualSchemaName = WorkflowSQLUtils.normalizeIdentifier(schemaName);
        if (!actualSchemaName.isEmpty()) {
            connection.setSchema(actualSchemaName);
        }
        return statement.executeQuery(sql);
    }
    
    private List<Map<String, Object>> extractRows(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        List<Map<String, Object>> result = new LinkedList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>(resultSetMetaData.getColumnCount(), 1F);
            for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
                row.put(resultSetMetaData.getColumnLabel(index).toLowerCase(), resultSet.getObject(index));
            }
            result.add(row);
        }
        return result;
    }
    
    private String formatColumnDefinition(final int columnType, final String columnTypeName, final int precision, final int scale) {
        String actualColumnTypeName = normalize(columnTypeName);
        if (actualColumnTypeName.isEmpty()) {
            return DEFAULT_COLUMN_DEFINITION;
        }
        switch (columnType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return 0 < precision ? String.format("%s(%d)", actualColumnTypeName, precision) : actualColumnTypeName;
            case Types.DECIMAL:
            case Types.NUMERIC:
                if (0 < precision && 0 < scale) {
                    return String.format("%s(%d, %d)", actualColumnTypeName, precision, scale);
                }
                return 0 < precision ? String.format("%s(%d)", actualColumnTypeName, precision) : actualColumnTypeName;
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return 0 < scale ? String.format("%s(%d)", actualColumnTypeName, scale) : actualColumnTypeName;
            default:
                return actualColumnTypeName;
        }
    }
    
    private String normalize(final String value) {
        return null == value ? "" : value.trim();
    }
}
