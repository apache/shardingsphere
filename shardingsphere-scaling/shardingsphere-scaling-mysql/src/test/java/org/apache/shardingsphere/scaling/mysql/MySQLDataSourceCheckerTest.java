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

package org.apache.shardingsphere.scaling.mysql;

import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLDataSourceCheckerTest {
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    private Collection<DataSource> dataSources;
    
    private MySQLDataSourceChecker dataSourceChecker;
    
    @Before
    public void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        dataSources = new LinkedList<>();
        dataSources.add(dataSource);
        dataSourceChecker = new MySQLDataSourceChecker();
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }
    
    @Test
    public void assertCheckPrivilegeWithParticularSuccess() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '%'@'%'");
        dataSourceChecker.checkPrivilege(dataSources);
        verify(preparedStatement, Mockito.times(1)).executeQuery();
    }
    
    @Test
    public void assertCheckPrivilegeWithAllSuccess() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT ALL PRIVILEGES CLIENT ON *.* TO '%'@'%'");
        dataSourceChecker.checkPrivilege(dataSources);
        verify(preparedStatement, Mockito.times(1)).executeQuery();
    }
    
    @Test(expected = PrepareFailedException.class)
    public void assertCheckPrivilegeLackPrivileges() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        dataSourceChecker.checkPrivilege(dataSources);
    }
    
    @Test(expected = PrepareFailedException.class)
    public void assertCheckPrivilegeFailure() throws SQLException {
        when(resultSet.next()).thenThrow(new SQLException(""));
        dataSourceChecker.checkPrivilege(dataSources);
    }
    
    @Test
    public void assertCheckVariableSuccess() throws SQLException {
        when(resultSet.next()).thenReturn(true, true);
        when(resultSet.getString(2)).thenReturn("ON", "ROW", "FULL");
        dataSourceChecker.checkVariable(dataSources);
        verify(preparedStatement, Mockito.times(3)).executeQuery();
    }
    
    @Test(expected = PrepareFailedException.class)
    public void assertCheckVariableWithWrongVariable() throws SQLException {
        when(resultSet.next()).thenReturn(true, true);
        when(resultSet.getString(2)).thenReturn("OFF", "ROW");
        dataSourceChecker.checkVariable(dataSources);
    }
    
    @Test(expected = PrepareFailedException.class)
    public void assertCheckVariableFailure() throws SQLException {
        when(resultSet.next()).thenThrow(new SQLException(""));
        dataSourceChecker.checkVariable(dataSources);
    }
}
