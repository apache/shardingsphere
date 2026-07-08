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
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseDialect;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
            throw new MCPQueryFailedException(ex.getMessage(), ex);
        }
    }
    
    @Override
    public List<Map<String, Object>> queryWithAnyDatabase(final String sql) {
        String databaseName = sessionManager.getTransactionResourceManager().getRuntimeDatabases().keySet().stream().findFirst()
                .orElseThrow(() -> new MCPUnavailableException("No runtime database is configured."));
        return query(databaseName, "", sql);
    }
    
    @Override
    public String getDatabaseType(final String databaseName) {
        return databaseCapabilityProvider.provide(WorkflowSQLUtils.normalizeIdentifier(databaseName)).map(MCPDatabaseCapability::getDatabaseType).orElse("");
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
            throw new MCPQueryFailedException(ex.getMessage(), ex);
        }
    }
    
    @Override
    public Set<String> queryInformationSchemaColumnNames(final String databaseName, final String schemaName, final String tableName, final Collection<String> columnNames) {
        WorkflowSQLUtils.checkSupportedIdentifier("database", databaseName);
        WorkflowSQLUtils.checkSupportedIdentifier("schema", schemaName);
        WorkflowSQLUtils.checkSupportedIdentifier("table", tableName);
        String actualDatabaseName = WorkflowSQLUtils.normalizeIdentifier(databaseName);
        String actualSchemaName = WorkflowSQLUtils.normalizeIdentifier(schemaName);
        if (columnNames.isEmpty()) {
            return Set.of();
        }
        StringBuilder result = new StringBuilder(columnNames.size() * 8);
        for (String each : columnNames) {
            WorkflowSQLUtils.checkSupportedIdentifier("column", each);
            if (!result.isEmpty()) {
                result.append(", ");
            }
            result.append('\'').append(WorkflowSQLUtils.escapeLiteral(WorkflowSQLUtils.normalizeIdentifier(each))).append('\'');
        }
        String sql = createInformationSchemaColumnQuery(actualDatabaseName, actualSchemaName, WorkflowSQLUtils.normalizeIdentifier(tableName), result.toString());
        Set<String> actualResult = new LinkedHashSet<>(columnNames.size(), 1F);
        for (Map<String, Object> each : query(actualDatabaseName, "", sql)) {
            String actualColumnName = Objects.toString(each.get("column_name"), "").trim();
            if (!actualColumnName.isEmpty()) {
                actualResult.add(actualColumnName);
            }
        }
        return actualResult;
    }
    
    private String createInformationSchemaColumnQuery(final String databaseName, final String schemaName, final String tableName, final String columnList) {
        if (!shouldFilterBySchema(databaseName, schemaName)) {
            return String.format("SELECT DISTINCT column_name FROM information_schema.columns WHERE table_name = '%s' AND column_name IN (%s)",
                    WorkflowSQLUtils.escapeLiteral(tableName), columnList);
        }
        String actualSchemaName = normalize(schemaName);
        return String.format("SELECT DISTINCT column_name FROM information_schema.columns WHERE table_schema = '%s' AND table_name = '%s' AND column_name IN (%s)",
                WorkflowSQLUtils.escapeLiteral(actualSchemaName), WorkflowSQLUtils.escapeLiteral(tableName), columnList);
    }
    
    private boolean shouldFilterBySchema(final String databaseName, final String schemaName) {
        String actualSchemaName = normalize(schemaName);
        if (actualSchemaName.isEmpty()) {
            return false;
        }
        String databaseType = databaseCapabilityProvider.provide(databaseName).map(MCPDatabaseCapability::getDatabaseType).orElse("");
        return MCPDatabaseDialect.of(databaseType).isInformationSchemaColumnSchemaFilterRequired();
    }
    
    private Connection openConnection(final String databaseName) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = sessionManager.getTransactionResourceManager().getRuntimeDatabases().get(databaseName);
        if (null == runtimeDatabaseConfig) {
            throw new MCPUnavailableException(String.format("Database `%s` is not configured.", databaseName));
        }
        return runtimeDatabaseConfig.openConnection(databaseName);
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
