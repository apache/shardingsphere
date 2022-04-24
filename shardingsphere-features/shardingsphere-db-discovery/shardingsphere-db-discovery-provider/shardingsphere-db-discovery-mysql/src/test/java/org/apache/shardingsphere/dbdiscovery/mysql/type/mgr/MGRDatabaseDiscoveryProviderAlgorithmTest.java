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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection().createStatement().executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, true, true, false);
        when(resultSet.getString("PLUGIN_STATUS")).thenReturn("ACTIVE");
        when(resultSet.getString("VARIABLE_VALUE")).thenReturn("ON", "group_name");
        when(resultSet.getString("MEMBER_HOST")).thenReturn("127.0.0.1", "127.0.0.1");
        when(resultSet.getString("MEMBER_PORT")).thenReturn("3306", "3307");
        when(resultSet.getString("MEMBER_STATE")).thenReturn("ONLINE");
        when(dataSource.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/ds_0?serverTimezone=UTC&useSSL=false");
        databaseDiscoveryType.getProps().setProperty("group-name", "group_name");
        MGRHighlyAvailableStatus actual = databaseDiscoveryType.loadHighlyAvailableStatus(dataSource);
        assertTrue(actual.isPluginActive());
        assertTrue(actual.isSinglePrimaryMode());
        assertThat(actual.getGroupName(), is("group_name"));
        Iterator<IPPortPrimaryDatabaseInstance> databaseInstances = actual.getDatabaseInstances().iterator();
        assertThat(databaseInstances.next().toString(), is("127.0.0.1:3306"));
        assertThat(databaseInstances.next().toString(), is("127.0.0.1:3307"));
        assertFalse(databaseInstances.hasNext());
    }
    
    @Test
    public void assertFindPrimaryDataSource() throws SQLException {
        List<DataSource> dataSources = new LinkedList<>();
        List<Connection> connections = new LinkedList<>();
        List<Statement> statements = new LinkedList<>();
        List<ResultSet> resultSets = new LinkedList<>();
        List<DatabaseMetaData> databaseMetaData = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            dataSources.add(mock(DataSource.class));
            connections.add(mock(Connection.class));
            statements.add(mock(Statement.class));
            resultSets.add(mock(ResultSet.class));
            databaseMetaData.add(mock(DatabaseMetaData.class));
        }
        String sql = "SELECT MEMBER_HOST, MEMBER_PORT FROM performance_schema.replication_group_members WHERE MEMBER_ID = "
                + "(SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'group_replication_primary_member')";
        for (int i = 0; i < 3; i++) {
            when(dataSources.get(i).getConnection()).thenReturn(connections.get(i));
            when(connections.get(i).createStatement()).thenReturn(statements.get(i));
            when(statements.get(i).executeQuery(sql)).thenReturn(resultSets.get(i));
            when(resultSets.get(i).next()).thenReturn(true, false);
            when(resultSets.get(i).getString("MEMBER_HOST")).thenReturn("127.0.0.1");
            when(resultSets.get(i).getString("MEMBER_PORT")).thenReturn(Integer.toString(3306 + i));
            when(connections.get(i).getMetaData()).thenReturn(databaseMetaData.get(i));
            when(databaseMetaData.get(i).getURL()).thenReturn("jdbc:mysql://127.0.0.1:" + (3306 + i) + "/ds_0?serverTimezone=UTC&useSSL=false");
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        for (int i = 0; i < 3; i++) {
            dataSourceMap.put(String.format("ds_%s", i), dataSources.get(i));
        }
        databaseDiscoveryType.getProps().setProperty("group-name", "group_name");
        Optional<IPPortPrimaryDatabaseInstance> actual = databaseDiscoveryType.findPrimaryInstance(dataSourceMap);
        assertTrue(actual.isPresent());
        assertThat(actual.get().toString(), is("127.0.0.1:3308"));
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
