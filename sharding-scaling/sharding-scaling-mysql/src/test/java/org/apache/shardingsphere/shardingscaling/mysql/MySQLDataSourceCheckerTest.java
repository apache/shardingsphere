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

package org.apache.shardingsphere.shardingscaling.mysql;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingscaling.core.exception.DatasourceCheckFailedException;
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
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MySQLDataSourceCheckerTest {

    private static final String CATALOG = "test";

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
        mockConnection();
        mockResultSet();
        when(dataSource.getConnection()).thenReturn(connection);
        dataSources = new ArrayList<>();
        dataSources.add(dataSource);
        dataSourceChecker = new MySQLDataSourceChecker();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @SneakyThrows
    private void mockConnection() {
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getTables(CATALOG, null, "%", new String[]{"TABLE"})).thenReturn(resultSet);
        when(connection.getCatalog()).thenReturn(CATALOG);
        when(connection.prepareStatement("SELECT * FROM test LIMIT 1")).thenReturn(preparedStatement);
        when(connection.prepareStatement("SHOW MASTER STATUS")).thenReturn(preparedStatement);
    }

    @SneakyThrows
    private void mockResultSet() {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(3)).thenReturn("test");
    }

    @Test
    public void assertCheckPrivilegeSuccess() throws SQLException {
        dataSourceChecker.checkPrivilege(dataSources);
        verify(preparedStatement, Mockito.times(2)).executeQuery();
    }

    @Test
    public void assertCheckPrivilegeWithGettingTableFailure() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        try {
            dataSourceChecker.checkPrivilege(dataSources);
        } catch (DatasourceCheckFailedException checkFailedEx) {
            assertThat(checkFailedEx.getMessage(), is("No tables find in the source datasource."));
        }
    }
    
    @Test
    public void assertCheckPrivilegeWithNoQueryPrivilegeFailure() throws SQLException {
        when(preparedStatement.executeQuery()).thenThrow(new SQLException());
        try {
            dataSourceChecker.checkPrivilege(dataSources);
        } catch (DatasourceCheckFailedException checkFailedEx) {
            assertThat(checkFailedEx.getMessage(), is("Source datasource is lack of query privileges."));
        }
    }
    
    @Test
    public void assertCheckPrivilegeWithNoReplicationPrivilegeFailure() throws SQLException {
        when(connection.prepareStatement("SHOW MASTER STATUS")).thenThrow(new SQLException());
        try {
            dataSourceChecker.checkPrivilege(dataSources);
        } catch (DatasourceCheckFailedException checkFailedEx) {
            assertThat(checkFailedEx.getMessage(), is("Source datasource is lack of replication(binlog) privileges."));
        }
    }
    
    @Test
    public void assertCheckPrivilegeWithNoBinlogFailure() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        try {
            dataSourceChecker.checkPrivilege(dataSources);
        } catch (DatasourceCheckFailedException checkFailedEx) {
            assertThat(checkFailedEx.getMessage(), is("Source datasource do not open binlog."));
        }
    }
}
