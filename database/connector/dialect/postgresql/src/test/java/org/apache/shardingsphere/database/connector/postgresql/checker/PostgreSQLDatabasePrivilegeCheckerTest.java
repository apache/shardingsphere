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

package org.apache.shardingsphere.database.connector.postgresql.checker;

import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.exception.CheckDatabaseEnvironmentFailedException;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredPrivilegeException;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredUserException;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLDatabasePrivilegeCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DialectDatabasePrivilegeChecker checker = DatabaseTypedSPILoader.getService(DialectDatabasePrivilegeChecker.class, databaseType);
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Test
    void assertCheckWithNoneType() {
        assertDoesNotThrow(() -> checker.check(dataSource, PrivilegeCheckType.NONE));
        verifyNoInteractions(dataSource);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkPipelinePrivilegeArguments")
    void assertCheckWithPipelinePrivilege(final String name, final String isSuperRole, final String isReplicationRole, final Class<? extends Throwable> expectedException) throws SQLException {
        mockShowGrantsQuery();
        mockPipelineResultSet(isSuperRole, isReplicationRole);
        if (null == expectedException) {
            assertDoesNotThrow(() -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
        } else {
            assertThrows(expectedException, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
        }
        verify(preparedStatement).executeQuery();
    }
    
    private void mockPipelineResultSet(final String isSuperRole, final String isReplicationRole) throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("rolsuper")).thenReturn(isSuperRole);
        when(resultSet.getString("rolreplication")).thenReturn(isReplicationRole);
    }
    
    @Test
    void assertCheckWhenUserMissing() throws SQLException {
        mockShowGrantsQuery();
        assertThrows(MissingRequiredUserException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckWhenSQLExceptionThrown() throws SQLException {
        mockShowGrantsQuery();
        when(preparedStatement.executeQuery()).thenThrow(SQLException.class);
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    private void mockShowGrantsQuery() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData().getUserName()).thenReturn("postgres");
        when(connection.prepareStatement("SELECT * FROM pg_roles WHERE rolname = ?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }
    
    private static Stream<Arguments> checkPipelinePrivilegeArguments() {
        return Stream.of(
                Arguments.of("pipeline with super role", "t", "f", null),
                Arguments.of("pipeline with replication role", "f", "t", null),
                Arguments.of("pipeline without required privilege", "f", "f", MissingRequiredPrivilegeException.class));
    }
}
