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

package org.apache.shardingsphere.data.pipeline.postgresql.check.datasource;

import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithoutEnoughPrivilegeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLDataSourceCheckerTest {
    
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
    
    @Before
    public void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getUserName()).thenReturn("postgres");
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
    }
    
    @Test
    public void assertCheckRolReplication() throws SQLException {
        PostgreSQLDataSourceChecker dataSourceChecker = new PostgreSQLDataSourceChecker();
        when(resultSet.getString("rolreplication")).thenReturn("t");
        when(resultSet.getString("rolsuper")).thenReturn("f");
        dataSourceChecker.checkPrivilege(Collections.singletonList(dataSource));
        verify(resultSet, Mockito.atLeastOnce()).getString("rolsuper");
    }
    
    @Test
    public void assertCheckRolSuper() throws SQLException {
        PostgreSQLDataSourceChecker dataSourceChecker = new PostgreSQLDataSourceChecker();
        when(resultSet.getString("rolsuper")).thenReturn("t");
        when(resultSet.getString("rolreplication")).thenReturn("f");
        dataSourceChecker.checkPrivilege(Collections.singletonList(dataSource));
        verify(resultSet, Mockito.atLeastOnce()).getString("rolreplication");
    }
    
    @Test(expected = PrepareJobWithoutEnoughPrivilegeException.class)
    public void asserCheckNoPrivilege() throws SQLException {
        PostgreSQLDataSourceChecker dataSourceChecker = new PostgreSQLDataSourceChecker();
        when(resultSet.getString("rolsuper")).thenReturn("f");
        when(resultSet.getString("rolreplication")).thenReturn("f");
        dataSourceChecker.checkPrivilege(Collections.singletonList(dataSource));
        verify(resultSet, Mockito.atLeastOnce()).getString("rolreplication");
    }
}
