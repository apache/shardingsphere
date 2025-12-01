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

import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.exception.MissingRequiredPrivilegeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLDatabasePrivilegeCheckerTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData metaData;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getUserName()).thenReturn("postgres");
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
    }
    
    @Test
    void assertCheckRolReplication() throws SQLException {
        PostgreSQLDatabasePrivilegeChecker dataSourceChecker = new PostgreSQLDatabasePrivilegeChecker();
        when(resultSet.getString("rolreplication")).thenReturn("t");
        when(resultSet.getString("rolsuper")).thenReturn("f");
        dataSourceChecker.check(dataSource, PrivilegeCheckType.PIPELINE);
        verify(resultSet, atLeastOnce()).getString("rolsuper");
    }
    
    @Test
    void assertCheckRolSuper() throws SQLException {
        PostgreSQLDatabasePrivilegeChecker dataSourceChecker = new PostgreSQLDatabasePrivilegeChecker();
        when(resultSet.getString("rolsuper")).thenReturn("t");
        when(resultSet.getString("rolreplication")).thenReturn("f");
        dataSourceChecker.check(dataSource, PrivilegeCheckType.PIPELINE);
        verify(resultSet, atLeastOnce()).getString("rolreplication");
    }
    
    @Test
    void assertCheckNoPrivilege() throws SQLException {
        PostgreSQLDatabasePrivilegeChecker dataSourceChecker = new PostgreSQLDatabasePrivilegeChecker();
        when(resultSet.getString("rolsuper")).thenReturn("f");
        when(resultSet.getString("rolreplication")).thenReturn("f");
        assertThrows(MissingRequiredPrivilegeException.class, () -> dataSourceChecker.check(dataSource, PrivilegeCheckType.PIPELINE));
        verify(resultSet, atLeastOnce()).getString("rolreplication");
    }
}
