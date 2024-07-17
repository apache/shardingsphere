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

package org.apache.shardingsphere.infra.database.mysql.checker;

import org.apache.shardingsphere.infra.database.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.infra.database.core.exception.CheckDatabaseEnvironmentFailedException;
import org.apache.shardingsphere.infra.database.core.exception.MissingRequiredPrivilegeException;
import org.apache.shardingsphere.infra.database.core.exception.UnexpectedVariableValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLDatabaseEnvironmentCheckerTest {
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection().prepareStatement(anyString())).thenReturn(preparedStatement);
    }
    
    @Test
    void assertCheckPrivilegeWithParticularSuccess() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '%'@'%'");
        new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.PIPELINE);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertCheckPrivilegeWithAllSuccess() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT ALL PRIVILEGES CLIENT ON *.* TO '%'@'%'");
        new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.PIPELINE);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertCheckPrivilegeLackPrivileges() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        assertThrows(MissingRequiredPrivilegeException.class, () -> new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckPrivilegeFailure() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenThrow(new SQLException(""));
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.PIPELINE));
    }
    
    @Test
    void assertCheckVariableSuccess() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString(1)).thenReturn("LOG_BIN", "BINLOG_FORMAT", "BINLOG_ROW_IMAGE");
        when(resultSet.getString(2)).thenReturn("ON", "ROW", "FULL");
        assertDoesNotThrow(() -> new MySQLDatabaseEnvironmentChecker().checkVariable(dataSource));
        verify(preparedStatement, times(1)).executeQuery();
    }
    
    @Test
    void assertCheckVariableWithWrongVariable() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(1)).thenReturn("BINLOG_FORMAT", "LOG_BIN");
        when(resultSet.getString(2)).thenReturn("ROW", "OFF");
        assertThrows(UnexpectedVariableValueException.class, () -> new MySQLDatabaseEnvironmentChecker().checkVariable(dataSource));
    }
    
    @Test
    void assertCheckVariableFailure() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenThrow(new SQLException(""));
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> new MySQLDatabaseEnvironmentChecker().checkVariable(dataSource));
    }
    
    @Test
    void assertCheckXAPrivilegeWithParticularSuccessInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT XA_RECOVER_ADMIN ON *.* TO '%'@'%'");
        new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.XA);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertUnCheckXAPrivilegeInMySQL5() throws SQLException {
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(5);
        new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.XA);
        verify(preparedStatement, times(0)).executeQuery();
    }
    
    @Test
    void assertCheckXAPrivilegeWithAllSuccessInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT ALL PRIVILEGES ON *.* TO '%'@'%'");
        new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.XA);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertCheckXAPrivilegeLackPrivilegesInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        assertThrows(MissingRequiredPrivilegeException.class, () -> new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.XA));
    }
    
    @Test
    void assertCheckXAPrivilegeFailureInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        when(resultSet.next()).thenThrow(new SQLException(""));
        assertThrows(CheckDatabaseEnvironmentFailedException.class, () -> new MySQLDatabaseEnvironmentChecker().checkPrivilege(dataSource, PrivilegeCheckType.XA));
    }
}
