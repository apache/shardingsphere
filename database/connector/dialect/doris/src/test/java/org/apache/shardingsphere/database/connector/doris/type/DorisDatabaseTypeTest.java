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

package org.apache.shardingsphere.database.connector.doris.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DorisDatabaseTypeTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Doris");
    
    @Test
    void assertGetJdbcUrlPrefixes() {
        assertThat(databaseType.getJdbcUrlPrefixes(), is(Arrays.asList("jdbc:mysql:", "jdbc:mysqlx:")));
    }
    
    @Test
    void assertGetTrunkDatabaseType() {
        Optional<DatabaseType> actual = databaseType.getTrunkDatabaseType();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getType(), is("MySQL"));
    }
    
    @Test
    void assertActualBranchDatabaseTypeWithProductName() throws SQLException {
        assertTrue(databaseType.isActualBranchDatabaseType(createConnection("Apache Doris", "", "")));
    }
    
    @Test
    void assertActualBranchDatabaseTypeWithVersionComment() throws SQLException {
        assertTrue(databaseType.isActualBranchDatabaseType(createConnection("MySQL", "8.0.36", "Doris version doris-3.0.3")));
    }
    
    @Test
    void assertNotActualBranchDatabaseType() throws SQLException {
        assertFalse(databaseType.isActualBranchDatabaseType(createConnection("MySQL", "8.0.36", "MySQL Community Server - GPL")));
    }
    
    @Test
    void assertNotActualBranchDatabaseTypeWithVersionCommentQueryFailure() throws SQLException {
        assertFalse(databaseType.isActualBranchDatabaseType(createConnectionWithQueryFailure()));
    }
    
    private Connection createConnection(final String productName, final String productVersion, final String versionComment) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getMetaData()).thenReturn(metaData);
        when(result.createStatement()).thenReturn(statement);
        when(metaData.getDatabaseProductName()).thenReturn(productName);
        when(metaData.getDatabaseProductVersion()).thenReturn(productVersion);
        when(statement.executeQuery("SELECT @@version_comment")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn(versionComment);
        return result;
    }
    
    private Connection createConnectionWithQueryFailure() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(result.getMetaData()).thenReturn(metaData);
        when(result.createStatement()).thenThrow(SQLException.class);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("8.0.36");
        return result;
    }
}
