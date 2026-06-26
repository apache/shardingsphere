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

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPJdbcDatabaseProfileLoaderTest {
    
    @Test
    void assertLoadUsesConfiguredDatabaseType() throws SQLException {
        Connection connection = mockConnection("MySQL", "jdbc:mysql://localhost:3306/logic_db", "8.0.36");
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration("Apache Doris", connection);
        RuntimeDatabaseProfile actual = new MCPJdbcDatabaseProfileLoader().load("logic_db", runtimeDatabaseConfig);
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getDatabaseType(), is("Doris"));
        assertThat(actual.getDatabaseVersion(), is("8.0.36"));
        verify(connection, never()).createStatement();
    }
    
    @Test
    void assertLoadDeterminesDatabaseTypeWithoutConfiguration() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration("", mockConnection("PostgreSQL", "jdbc:postgresql://localhost:5432/logic_db", "16.2"));
        RuntimeDatabaseProfile actual = new MCPJdbcDatabaseProfileLoader().load("logic_db", runtimeDatabaseConfig);
        assertThat(actual.getDatabaseType(), is("PostgreSQL"));
    }
    
    @Test
    void assertLoadFailsWhenDatabaseTypeUndetermined() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = mockRuntimeDatabaseConfiguration("", mockConnection("", "jdbc:unknown://localhost/logic_db", ""));
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcDatabaseProfileLoader().load("logic_db", runtimeDatabaseConfig));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION));
    }
    
    private RuntimeDatabaseConfiguration mockRuntimeDatabaseConfiguration(final String databaseType, final Connection connection) throws SQLException {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        when(result.getDatabaseType()).thenReturn(databaseType);
        when(result.openConnection("logic_db")).thenReturn(connection);
        return result;
    }
    
    private Connection mockConnection(final String productName, final String jdbcUrl, final String productVersion) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(productName);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(productVersion);
        when(databaseMetaData.getURL()).thenReturn(jdbcUrl);
        return result;
    }
}
