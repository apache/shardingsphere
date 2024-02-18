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

package org.apache.shardingsphere.transaction.xa.jta.datasource.checker;

import org.apache.shardingsphere.transaction.xa.jta.datasource.checker.dialect.MySQLXATransactionPrivilegeChecker;
import org.apache.shardingsphere.transaction.xa.jta.exception.XATransactionCheckPrivilegeFailedException;
import org.apache.shardingsphere.transaction.xa.jta.exception.XATransactionPrivilegeException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLXATransactionPrivilegeCheckerTest {
    
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
    void assertCheckPrivilegeWithParticularSuccessInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT XA_RECOVER_ADMIN ON *.* TO '%'@'%'");
        new MySQLXATransactionPrivilegeChecker().check(dataSource);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertUnCheckPrivilegeInMySQL5() throws SQLException {
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(5);
        new MySQLXATransactionPrivilegeChecker().check(dataSource);
        verify(preparedStatement, times(0)).executeQuery();
    }
    
    @Test
    void assertCheckPrivilegeWithAllSuccessInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT ALL PRIVILEGES ON *.* TO '%'@'%'");
        new MySQLXATransactionPrivilegeChecker().check(dataSource);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertCheckPrivilegeLackPrivilegesInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        assertThrows(XATransactionPrivilegeException.class, () -> new MySQLXATransactionPrivilegeChecker().check(dataSource));
    }
    
    @Test
    void assertCheckPrivilegeFailureInMySQL8() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(dataSource.getConnection().getMetaData().getDatabaseMajorVersion()).thenReturn(8);
        when(resultSet.next()).thenThrow(new SQLException(""));
        assertThrows(XATransactionCheckPrivilegeFailedException.class, () -> new MySQLXATransactionPrivilegeChecker().check(dataSource));
    }
}
