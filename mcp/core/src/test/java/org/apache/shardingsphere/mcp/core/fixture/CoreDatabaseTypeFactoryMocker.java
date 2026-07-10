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

package org.apache.shardingsphere.mcp.core.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.mockito.MockedStatic;

import java.sql.DatabaseMetaData;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Database type factory mocker for MCP core tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreDatabaseTypeFactoryMocker {
    
    private static final String JDBC_URL_PREFIX = "jdbc:mcp-fixture:";
    
    /**
     * Create MCP fixture JDBC URL.
     *
     * @param databaseType database type
     * @return MCP fixture JDBC URL
     */
    public static String createJdbcUrl(final String databaseType) {
        return JDBC_URL_PREFIX + databaseType + ":test";
    }
    
    /**
     * Mock database type factory by JDBC connection metadata.
     *
     * @return mocked static resource
     */
    public static MockedStatic<DatabaseTypeFactory> mockByConnectionMetadata() {
        MockedStatic<DatabaseTypeFactory> result = mockStatic(DatabaseTypeFactory.class, CALLS_REAL_METHODS);
        result.when(() -> DatabaseTypeFactory.get(anyString())).thenAnswer(invocation -> createDatabaseType(invocation.getArgument(0, String.class)));
        result.when(() -> DatabaseTypeFactory.get(any(DatabaseMetaData.class)))
                .thenAnswer(invocation -> createDatabaseType(invocation.getArgument(0, DatabaseMetaData.class).getURL()));
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
    
    private static DatabaseType createDatabaseType(final String url) {
        return mockDatabaseType(resolveTypeByURL(url));
    }
    
    private static String resolveTypeByURL(final String url) {
        String actualURL = Objects.toString(url, "");
        if (!actualURL.startsWith(JDBC_URL_PREFIX)) {
            return "";
        }
        String databaseType = actualURL.substring(JDBC_URL_PREFIX.length());
        int delimiterIndex = databaseType.indexOf(':');
        return -1 == delimiterIndex ? databaseType : databaseType.substring(0, delimiterIndex);
    }
    
    private static DatabaseType mockDatabaseType(final String databaseType) {
        DatabaseType result = mock(DatabaseType.class);
        when(result.getType()).thenReturn(databaseType);
        return result;
    }
}
