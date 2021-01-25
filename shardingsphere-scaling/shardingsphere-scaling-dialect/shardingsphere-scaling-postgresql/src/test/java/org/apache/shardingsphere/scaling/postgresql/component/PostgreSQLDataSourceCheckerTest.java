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

package org.apache.shardingsphere.scaling.postgresql.component;

import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLDataSourceCheckerTest {

    private static final String CATALOG = "test";
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private DatabaseMetaData metaData;

    private Collection<DataSource> dataSources;

    @Before
    public void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mockConnection();
        when(dataSource.getConnection()).thenReturn(connection);
        when(metaData.getTables(CATALOG, null, "%", new String[]{"TABLE"})).thenReturn(resultSet);
        dataSources = new LinkedList<>();
        dataSources.add(dataSource);
    }

    private Connection mockConnection() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.getCatalog()).thenReturn(CATALOG);
        when(connection.prepareStatement("SELECT * FROM test LIMIT 1")).thenReturn(preparedStatement);
        return connection;
    }

    @Test
    public void assertCheckPrivilege() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(3)).thenReturn("test");
        PostgreSQLDataSourceChecker dataSourceChecker = new PostgreSQLDataSourceChecker();
        dataSourceChecker.checkPrivilege(dataSources);
        verify(preparedStatement).executeQuery();
    }
    
    @Test(expected = PrepareFailedException.class)
    public void assertCheckPrivilegeWithoutTable() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        PostgreSQLDataSourceChecker dataSourceChecker = new PostgreSQLDataSourceChecker();
        dataSourceChecker.checkPrivilege(dataSources);
    }
    
    @Test(expected = PrepareFailedException.class)
    public void assertCheckPrivilegeFailure() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(3)).thenReturn("test");
        when(connection.prepareStatement("SELECT * FROM test LIMIT 1")).thenThrow(new SQLException(""));
        PostgreSQLDataSourceChecker dataSourceChecker = new PostgreSQLDataSourceChecker();
        dataSourceChecker.checkPrivilege(dataSources);
    }
    
    @Test
    public void assertCheckVariable() {
        PostgreSQLDataSourceChecker dataSourceChecker = new PostgreSQLDataSourceChecker();
        dataSourceChecker.checkVariable(dataSources);
    }
}
