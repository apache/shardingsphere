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

package org.apache.shardingsphere.database.connector.mysql.checker;

import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.exception.CheckDatabaseEnvironmentFailedException;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredPrivilegeException;
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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLDatabasePrivilegeCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectDatabasePrivilegeChecker checker = DatabaseTypedSPILoader.getService(DialectDatabasePrivilegeChecker.class, databaseType);
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkSuccessArguments")
    void assertCheckSuccess(final String name, final PrivilegeCheckType privilegeCheckType, final int majorVersion, final String catalog, final String grantedPrivilege) throws SQLException {
        mockShowGrantsQuery();
        if (PrivilegeCheckType.XA == privilegeCheckType) {
            mockXAMetaData(majorVersion);
        }
        if (PrivilegeCheckType.SELECT == privilegeCheckType) {
            when(connection.getCatalog()).thenReturn(catalog);
        }
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn(grantedPrivilege);
        checker.check(dataSource, privilegeCheckType);
        verify(preparedStatement).executeQuery();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkMissingPrivilegeArguments")
    void assertCheckWithMissingPrivilege(final String name,
                                         final PrivilegeCheckType privilegeCheckType, final int majorVersion, final String catalog, final boolean containsGrantRow) throws SQLException {
        mockShowGrantsQuery();
        if (PrivilegeCheckType.XA == privilegeCheckType) {
            mockXAMetaData(majorVersion);
        }
        if (PrivilegeCheckType.SELECT == privilegeCheckType) {
            when(connection.getCatalog()).thenReturn(catalog);
        }
        if (containsGrantRow) {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(1)).thenReturn("GRANT INSERT ON *.* TO '%'@'%'");
        }
        assertThrows(MissingRequiredPrivilegeException.class, () -> checker.check(dataSource, privilegeCheckType));
    }
    
    @Test
    void assertCheckFailureWhenConnectionAcquisitionFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckFailureWhenExecuteQueryFails() throws SQLException {
        mockConnectionAndPreparedStatement();
        when(preparedStatement.executeQuery()).thenThrow(SQLException.class);
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckFailureWhenReadResultSetFails() throws SQLException {
        mockShowGrantsQuery();
        when(resultSet.next()).thenThrow(SQLException.class);
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckFailureWhenReadMySQLMajorVersionFails() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseMajorVersion()).thenThrow(SQLException.class);
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.XA));
    }
    
    @Test
    void assertCheckFailureWhenCloseResultSetFails() throws SQLException {
        mockShowGrantsQuery();
        when(resultSet.next()).thenReturn(false);
        doThrow(SQLException.class).when(resultSet).close();
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckFailureWhenClosePreparedStatementFails() throws SQLException {
        mockShowGrantsQuery();
        when(resultSet.next()).thenReturn(false);
        doThrow(SQLException.class).when(preparedStatement).close();
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckFailureWhenReadResultSetAndCloseResultSetFail() throws SQLException {
        mockShowGrantsQuery();
        when(resultSet.next()).thenThrow(new SQLException("mocked result set failure"));
        doThrow(SQLException.class).when(resultSet).close();
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckFailureWhenReadResultSetAndClosePreparedStatementFail() throws SQLException {
        mockShowGrantsQuery();
        when(resultSet.next()).thenThrow(SQLException.class);
        doThrow(SQLException.class).when(preparedStatement).close();
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckFailureWhenCloseConnectionFails() throws SQLException {
        mockShowGrantsQuery();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT ALL PRIVILEGES ON *.* TO '%'@'%'");
        doThrow(SQLException.class).when(connection).close();
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> checker.check(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckSkipsXAInMySQL5() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseMajorVersion()).thenReturn(5);
        checker.check(dataSource, PrivilegeCheckType.XA);
        verify(preparedStatement, never()).executeQuery();
    }
    
    private void mockShowGrantsQuery() throws SQLException {
        mockConnectionAndPreparedStatement();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }
    
    private void mockConnectionAndPreparedStatement() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SHOW GRANTS")).thenReturn(preparedStatement);
    }
    
    private void mockXAMetaData(final int majorVersion) throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseMajorVersion()).thenReturn(majorVersion);
    }
    
    private static Stream<Arguments> checkSuccessArguments() {
        return Stream.of(
                Arguments.of("pipeline with replication privilege", PrivilegeCheckType.PIPELINE, 0, null, "GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '%'@'%'"),
                Arguments.of("pipeline with all privileges", PrivilegeCheckType.PIPELINE, 0, null, "GRANT ALL PRIVILEGES ON *.* TO '%'@'%'"),
                Arguments.of("xa with xa recover admin on mysql8", PrivilegeCheckType.XA, 8, null, "GRANT XA_RECOVER_ADMIN ON *.* TO '%'@'%'"),
                Arguments.of("xa with all privileges on mysql8", PrivilegeCheckType.XA, 8, null, "GRANT ALL PRIVILEGES ON *.* TO '%'@'%'"),
                Arguments.of("select with select privilege on all databases", PrivilegeCheckType.SELECT, 0, "foo_db", "GRANT SELECT ON *.* TO '%'@'%'"),
                Arguments.of("select with select privilege on target database", PrivilegeCheckType.SELECT, 0, "foo_db", "GRANT SELECT ON `FOO_DB`.* TO '%'@'%'"),
                Arguments.of("select with all privileges on all databases", PrivilegeCheckType.SELECT, 0, "foo_db", "GRANT ALL PRIVILEGES ON *.* TO '%'@'%'"),
                Arguments.of("select with all privileges on target database", PrivilegeCheckType.SELECT, 0, "foo_db", "GRANT ALL PRIVILEGES ON `FOO_DB`.* TO '%'@'%'"));
    }
    
    private static Stream<Arguments> checkMissingPrivilegeArguments() {
        return Stream.of(
                Arguments.of("pipeline without grant rows", PrivilegeCheckType.PIPELINE, 0, null, false),
                Arguments.of("pipeline with unrelated grant row", PrivilegeCheckType.PIPELINE, 0, null, true),
                Arguments.of("xa without grant rows on mysql8", PrivilegeCheckType.XA, 8, null, false),
                Arguments.of("select with unrelated grant row", PrivilegeCheckType.SELECT, 0, "foo_db", true),
                Arguments.of("none with unrelated grant row", PrivilegeCheckType.NONE, 0, null, true));
    }
}
