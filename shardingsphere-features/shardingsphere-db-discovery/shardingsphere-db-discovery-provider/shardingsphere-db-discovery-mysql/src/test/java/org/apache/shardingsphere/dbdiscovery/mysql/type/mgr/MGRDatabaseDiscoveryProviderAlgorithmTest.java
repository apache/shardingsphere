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

package org.apache.shardingsphere.dbdiscovery.mysql.type.mgr;

import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MGRDatabaseDiscoveryProviderAlgorithmTest {
    
    @Test
    public void assertLoadHighlyAvailableStatus() throws SQLException {
        MGRHighlyAvailableStatus actual = new MGRMySQLDatabaseDiscoveryProviderAlgorithm().loadHighlyAvailableStatus(mockToBeLoadedHighlyAvailableStatusDataSource());
        assertThat(actual.getDatabaseInstanceURLs(), is(Arrays.asList("127.0.0.1:3306", "127.0.0.1:3307")));
    }
    
    private DataSource mockToBeLoadedHighlyAvailableStatusDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().createStatement().executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, true, true, false);
        when(resultSet.getString("PLUGIN_STATUS")).thenReturn("ACTIVE");
        when(resultSet.getString("VARIABLE_VALUE")).thenReturn("ON", "foo_group");
        when(resultSet.getString("MEMBER_HOST")).thenReturn("127.0.0.1", "127.0.0.1");
        when(resultSet.getString("MEMBER_PORT")).thenReturn("3306", "3307");
        when(resultSet.getString("MEMBER_STATE")).thenReturn("ONLINE");
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        return result;
    }
    
    @Test
    public void assertCheckEnvironment() throws SQLException {
        MGRMySQLDatabaseDiscoveryProviderAlgorithm actual = new MGRMySQLDatabaseDiscoveryProviderAlgorithm();
        actual.getProps().setProperty("group-name", "foo_group");
        actual.checkEnvironment("foo_db", mockEnvironmentAvailableDataSource());
    }
    
    private DataSource mockEnvironmentAvailableDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().createStatement().executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("PLUGIN_STATUS")).thenReturn("ACTIVE");
        when(resultSet.getString("VARIABLE_VALUE")).thenReturn("ON", "foo_group");
        return result;
    }
    
    @Test
    public void assertIsPrimaryInstance() throws SQLException {
        assertTrue(new MGRMySQLDatabaseDiscoveryProviderAlgorithm().isPrimaryInstance(mockPrimaryDataSource()));
    }
    
    private DataSource mockPrimaryDataSource() throws SQLException {
        String sql = "SELECT MEMBER_HOST, MEMBER_PORT FROM performance_schema.replication_group_members WHERE MEMBER_ID = "
                + "(SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'group_replication_primary_member')";
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().createStatement().executeQuery(sql)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("MEMBER_HOST")).thenReturn("127.0.0.1");
        when(resultSet.getString("MEMBER_PORT")).thenReturn("3306");
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        return result;
    }
    
    @Test
    public void assertGetDisabledStorageNodeDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        StorageNodeDataSource actual = new MGRMySQLDatabaseDiscoveryProviderAlgorithm().getStorageNodeDataSource(dataSource);
        assertThat(actual.getRole(), is("member"));
        assertThat(actual.getStatus(), is("disabled"));
        assertThat(actual.getReplicationDelayMilliseconds(), is(0L));
    }
    
    @Test
    public void assertGetEnabledStorageNodeDataSource() {
        // TODO
    }
}
