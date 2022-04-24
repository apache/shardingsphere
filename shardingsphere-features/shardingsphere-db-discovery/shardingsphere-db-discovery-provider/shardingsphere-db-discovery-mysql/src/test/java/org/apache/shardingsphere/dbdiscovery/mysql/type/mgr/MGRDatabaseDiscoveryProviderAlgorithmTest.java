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

import org.apache.shardingsphere.dbdiscovery.spi.instance.type.IPPortPrimaryDatabaseInstance;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MGRDatabaseDiscoveryProviderAlgorithmTest {
    
    private final MGRMySQLDatabaseDiscoveryProviderAlgorithm databaseDiscoveryType = new MGRMySQLDatabaseDiscoveryProviderAlgorithm();
    
    @Test
    public void assertLoadHighlyAvailableStatus() throws SQLException {
        MGRHighlyAvailableStatus actual = databaseDiscoveryType.loadHighlyAvailableStatus(mockToBeLoadedHighlyAvailableStatusDataSource());
        assertTrue(actual.isPluginActive());
        assertTrue(actual.isSinglePrimaryMode());
        assertThat(actual.getGroupName(), is("group_name"));
        Iterator<IPPortPrimaryDatabaseInstance> databaseInstances = actual.getDatabaseInstances().iterator();
        assertThat(databaseInstances.next().toString(), is("127.0.0.1:3306"));
        assertThat(databaseInstances.next().toString(), is("127.0.0.1:3307"));
        assertFalse(databaseInstances.hasNext());
    }
    
    private DataSource mockToBeLoadedHighlyAvailableStatusDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().createStatement().executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, true, true, false);
        when(resultSet.getString("PLUGIN_STATUS")).thenReturn("ACTIVE");
        when(resultSet.getString("VARIABLE_VALUE")).thenReturn("ON", "group_name");
        when(resultSet.getString("MEMBER_HOST")).thenReturn("127.0.0.1", "127.0.0.1");
        when(resultSet.getString("MEMBER_PORT")).thenReturn("3306", "3307");
        when(resultSet.getString("MEMBER_STATE")).thenReturn("ONLINE");
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        return result;
    }
    
    @Test
    public void assertFindPrimaryDataSource() throws SQLException {
        String sql = "SELECT MEMBER_HOST, MEMBER_PORT FROM performance_schema.replication_group_members WHERE MEMBER_ID = "
                + "(SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'group_replication_primary_member')";
        Optional<IPPortPrimaryDatabaseInstance> actual = databaseDiscoveryType.findPrimaryInstance("foo_ds", mockToBeFoundPrimaryDataSource(sql));
        assertTrue(actual.isPresent());
        assertThat(actual.get().toString(), is("127.0.0.1:3306"));
    }
    
    private DataSource mockToBeFoundPrimaryDataSource(final String sql) throws SQLException {
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
        StorageNodeDataSource actual = databaseDiscoveryType.getStorageNodeDataSource(dataSource);
        assertThat(actual.getRole(), is("member"));
        assertThat(actual.getStatus(), is("disabled"));
        assertThat(actual.getReplicationDelayMilliseconds(), is(0L));
    }
    
    @Test
    public void assertGetEnabledStorageNodeDataSource() {
        // TODO
    }
}
