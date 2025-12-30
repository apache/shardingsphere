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

package org.apache.shardingsphere.database.connector.opengauss.checker;

import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.exception.CheckDatabaseEnvironmentFailedException;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredPrivilegeException;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussDatabasePrivilegeCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DialectDatabasePrivilegeChecker checker = DatabaseTypedSPILoader.getService(DialectDatabasePrivilegeChecker.class, databaseType);
    
    @Test
    void assertCheckWithNoneType() {
        assertDoesNotThrow(() -> checker.check(mock(DataSource.class), PrivilegeCheckType.NONE));
    }
    
    @Test
    void assertCheckWithSuperRole() {
        assertDoesNotThrow(() -> checker.check(mockPipelineDataSource("admin", "t", "f"), PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckWithReplicationRole() {
        assertDoesNotThrow(() -> checker.check(mockPipelineDataSource("replication_user", "f", "t"), PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckWhenPrivilegeMissing() {
        MissingRequiredPrivilegeException actual = assertThrows(MissingRequiredPrivilegeException.class,
                () -> checker.check(mockPipelineDataSource("normal", "f", "f"), PrivilegeCheckType.PIPELINE));
        assertThat(actual.getMessage(), is("Missing required privilege(s) `REPLICATION`"));
    }
    
    @Test
    void assertCheckWhenSQLExceptionThrown() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    private DataSource mockPipelineDataSource(final String username, final String isSuperRole, final String isReplicationRole) throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(result.getConnection().getMetaData().getUserName()).thenReturn(username);
        PreparedStatement preparedStatement = result.getConnection().prepareStatement("SELECT * FROM pg_roles WHERE rolname = ?");
        ResultSet resultSet = mockResultSet(isSuperRole, isReplicationRole);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final String isSuperRole, final String isReplicationRole) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true);
        when(result.getString("rolsuper")).thenReturn(isSuperRole);
        when(result.getString("rolreplication")).thenReturn(isReplicationRole);
        when(result.getString("rolsystemadmin")).thenReturn("f");
        return result;
    }
}
