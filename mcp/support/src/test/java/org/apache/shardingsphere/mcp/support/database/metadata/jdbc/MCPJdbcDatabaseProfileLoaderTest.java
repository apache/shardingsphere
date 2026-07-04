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

package org.apache.shardingsphere.mcp.support.database.metadata.jdbc;

import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.mcp.support.fixture.SupportDatabaseTypeFactoryMocker;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPJdbcDatabaseProfileLoaderTest {
    
    @Test
    void assertLoad() throws SQLException {
        try (MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata()) {
            RuntimeDatabaseProfile actual = new MCPJdbcDatabaseProfileLoader().load("logic_db", createRuntimeDatabaseConfiguration("jdbc:mysql:test", "8.0.32"));
            assertThat(actual.getDatabase(), is("logic_db"));
            assertThat(actual.getDatabaseType(), is("MySQL"));
            assertThat(actual.getDatabaseVersion(), is("8.0.32"));
        }
    }
    
    @Test
    void assertLoadWithInvalidJdbcUrl() {
        try (MockedStatic<DatabaseTypeFactory> mocked = mockStatic(DatabaseTypeFactory.class)) {
            ShardingSphereExternalException expectedCause = mock(ShardingSphereExternalException.class);
            mocked.when(() -> DatabaseTypeFactory.get("jdbc:unknown:test")).thenThrow(expectedCause);
            RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                    () -> new MCPJdbcDatabaseProfileLoader().load("logic_db", createRuntimeDatabaseConfiguration("jdbc:unknown:test", "8.0.32")));
            assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION));
            assertThat(actual.getCause(), is(expectedCause));
        }
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl, final String databaseVersion) throws SQLException {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(result.openConnection(anyString())).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn(jdbcUrl);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseVersion);
        return result;
    }
}
