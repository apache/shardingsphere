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

package org.apache.shardingsphere.database.connector.mysql.metadata.database.schema;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.database.connector.core.metadata.database.schema.SystemSchemaProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * MySQL system schema provider.
 */
@Slf4j
public final class MySQLSystemSchemaProvider implements SystemSchemaProvider {
    
    private static final String SYSTEM_TABLE_SQL = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=?";
    
    private static final String TABLE_TYPE_SQL = "SELECT TABLE_TYPE FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?";
    
    private static final String TABLE_TYPE_VIEW = "VIEW";
    
    private static final String YAML_TABLE_TEMPLATE = "name: %s%s%stype: %s%scolumns: {}%s";
    
    @Override
    public Optional<Collection<String>> getSystemTables(final String schemaName) {
        if (null == schemaName) {
            return Optional.empty();
        }
        Map<String, DataSource> cachedDataSources = GlobalDataSourceRegistry.getInstance().getCachedDataSources();
        if (cachedDataSources.isEmpty()) {
            return Optional.empty();
        }
        DataSource dataSource = cachedDataSources.values().iterator().next();
        return loadSystemTableNames(schemaName, dataSource);
    }
    
    @Override
    public Optional<Collection<InputStream>> getSystemSchemaInputStreams(final String schemaName) {
        if (null == schemaName) {
            return Optional.empty();
        }
        Map<String, DataSource> cachedDataSources = GlobalDataSourceRegistry.getInstance().getCachedDataSources();
        if (cachedDataSources.isEmpty()) {
            return Optional.empty();
        }
        DataSource dataSource = cachedDataSources.values().iterator().next();
        Optional<Collection<String>> tableNames = loadSystemTableNames(schemaName, dataSource);
        if (!tableNames.isPresent()) {
            return Optional.empty();
        }
        Collection<InputStream> result = new LinkedList<>();
        for (String tableName : tableNames.get()) {
            String tableType = loadTableType(schemaName, tableName, dataSource);
            result.add(new ByteArrayInputStream(buildTableYaml(tableName, tableType).getBytes(StandardCharsets.UTF_8)));
        }
        return Optional.of(result);
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    private Optional<Collection<String>> loadSystemTableNames(final String schemaName, final DataSource dataSource) {
        Collection<String> result = new CaseInsensitiveSet<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SYSTEM_TABLE_SQL)) {
            preparedStatement.setString(1, schemaName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("TABLE_NAME"));
                }
            }
        } catch (final SQLException ex) {
            log.debug("Load MySQL system tables failed for schema {}", schemaName, ex);
            return Optional.empty();
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private String loadTableType(final String schemaName, final String tableName, final DataSource dataSource) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(TABLE_TYPE_SQL)) {
            preparedStatement.setString(1, schemaName);
            preparedStatement.setString(2, tableName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return TABLE_TYPE_VIEW.equalsIgnoreCase(resultSet.getString("TABLE_TYPE")) ? "VIEW" : "TABLE";
                }
            }
        } catch (final SQLException ex) {
            log.debug("Load MySQL system table type failed for {}.{}", schemaName, tableName, ex);
        }
        return "TABLE";
    }
    
    private String buildTableYaml(final String tableName, final String tableType) {
        return String.format(YAML_TABLE_TEMPLATE, tableName, System.lineSeparator(), System.lineSeparator(), tableType, System.lineSeparator(), System.lineSeparator());
    }
}
