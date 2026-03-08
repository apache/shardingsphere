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
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredUserException;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OpenGaussDatabasePrivilegeCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DialectDatabasePrivilegeChecker checker = DatabaseTypedSPILoader.getService(DialectDatabasePrivilegeChecker.class, databaseType);
    
    @Test
    void assertCheckWithNoneType() {
        DataSource dataSource = mock(DataSource.class);
        assertDoesNotThrow(() -> checker.check(dataSource, PrivilegeCheckType.NONE));
        verifyNoInteractions(dataSource);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("pipelinePrivilegeRoles")
    void assertCheckWithPipelinePrivilegeRole(final String name, final String username, final String isSuperRole, final String isReplicationRole, final String isSystemAdminRole) {
        assertDoesNotThrow(() -> checker.check(mockPipelineDataSource(username, true, isSuperRole, isReplicationRole, isSystemAdminRole), PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckWhenUserMissing() {
        MissingRequiredUserException actual = assertThrows(MissingRequiredUserException.class,
                () -> checker.check(mockPipelineDataSource("ghost", false, "f", "f", "f"), PrivilegeCheckType.PIPELINE));
        assertThat(actual.getMessage(), is("User 'ghost' does exist"));
    }
    
    @Test
    void assertCheckWhenPrivilegeMissing() {
        MissingRequiredPrivilegeException actual = assertThrows(MissingRequiredPrivilegeException.class,
                () -> checker.check(mockPipelineDataSource("normal", true, "f", "f", "f"), PrivilegeCheckType.PIPELINE));
        assertThat(actual.getMessage(), is("Missing required privilege(s) `REPLICATION`"));
    }
    
    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "resource"})
    private DataSource mockPipelineDataSource(final String username, final boolean hasUser,
                                              final String isSuperRole, final String isReplicationRole, final String isSystemAdminRole) throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(result.getConnection().getMetaData().getUserName()).thenReturn(username);
        PreparedStatement preparedStatement = result.getConnection().prepareStatement("SELECT * FROM pg_roles WHERE rolname = ?");
        ResultSet resultSet = mockResultSet(hasUser, isSuperRole, isReplicationRole, isSystemAdminRole);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final boolean hasUser, final String isSuperRole, final String isReplicationRole, final String isSystemAdminRole) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(hasUser);
        when(result.getString("rolsuper")).thenReturn(isSuperRole);
        when(result.getString("rolreplication")).thenReturn(isReplicationRole);
        when(result.getString("rolsystemadmin")).thenReturn(isSystemAdminRole);
        return result;
    }
    
    @Test
    void assertCheckWhenSQLExceptionThrown() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        CheckDatabaseEnvironmentFailedException actual = assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
        assertThat(actual.getMessage(), is("Check database environment failed"));
    }
    
    private static Stream<Arguments> pipelinePrivilegeRoles() {
        return Stream.of(
                Arguments.of("super role", "admin", "t", "f", "f"),
                Arguments.of("replication role", "replication_user", "f", "t", "f"),
                Arguments.of("system admin role", "system_admin", "f", "f", "t"));
    }
}
