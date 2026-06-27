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

package org.apache.shardingsphere.mcp.support.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Database type factory mocker for MCP support tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SupportDatabaseTypeFactoryMocker {
    
    /**
     * Mock database type factory by JDBC connection metadata.
     *
     * @return mocked static resource
     */
    public static MockedStatic<DatabaseTypeFactory> mockByConnectionMetadata() {
        MockedStatic<DatabaseTypeFactory> result = mockStatic(DatabaseTypeFactory.class, CALLS_REAL_METHODS);
        result.when(() -> DatabaseTypeFactory.get(any(Connection.class))).thenAnswer(invocation -> createDatabaseType(invocation.getArgument(0, Connection.class)));
        return result;
    }
    
    /**
     * Create MCP database capability provider with database type factory mocked by JDBC connection metadata.
     *
     * @param runtimeDatabases runtime database configurations
     * @return MCP database capability provider
     */
    public static MCPDatabaseCapabilityProvider createDatabaseCapabilityProvider(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        try (MockedStatic<DatabaseTypeFactory> ignored = mockByConnectionMetadata()) {
            return new MCPDatabaseCapabilityProvider(runtimeDatabases);
        }
    }
    
    private static DatabaseType createDatabaseType(final Connection connection) throws SQLException {
        String productName = Objects.toString(connection.getMetaData().getDatabaseProductName(), "").trim();
        if (!productName.isEmpty()) {
            return mockDatabaseType(normalizeProductName(productName));
        }
        return mockDatabaseType(resolveTypeByURL(connection.getMetaData().getURL()));
    }
    
    private static String normalizeProductName(final String productName) {
        String upperProductName = productName.toUpperCase(Locale.ENGLISH);
        if (upperProductName.contains("DORIS")) {
            return "Doris";
        }
        if (upperProductName.contains("MARIADB")) {
            return "MariaDB";
        }
        if (upperProductName.contains("SQL SERVER")) {
            return "SQLServer";
        }
        return productName;
    }
    
    private static String resolveTypeByURL(final String url) {
        String actualURL = Objects.toString(url, "").toLowerCase(Locale.ENGLISH);
        if (actualURL.startsWith("jdbc:mysql:")) {
            return "MySQL";
        }
        if (actualURL.startsWith("jdbc:postgresql:")) {
            return "PostgreSQL";
        }
        if (actualURL.startsWith("jdbc:opengauss:")) {
            return "openGauss";
        }
        if (actualURL.startsWith("jdbc:sqlserver:")) {
            return "SQLServer";
        }
        if (actualURL.startsWith("jdbc:mariadb:")) {
            return "MariaDB";
        }
        if (actualURL.startsWith("jdbc:oracle:")) {
            return "Oracle";
        }
        if (actualURL.startsWith("jdbc:clickhouse:")) {
            return "ClickHouse";
        }
        if (actualURL.startsWith("jdbc:hive2:") || actualURL.startsWith("jdbc:hive:")) {
            return "Hive";
        }
        if (actualURL.startsWith("jdbc:presto:")) {
            return "Presto";
        }
        if (actualURL.startsWith("jdbc:firebirdsql:")) {
            return "Firebird";
        }
        return "";
    }
    
    private static DatabaseType mockDatabaseType(final String databaseType) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getType()).thenReturn(databaseType);
        return result;
    }
}
