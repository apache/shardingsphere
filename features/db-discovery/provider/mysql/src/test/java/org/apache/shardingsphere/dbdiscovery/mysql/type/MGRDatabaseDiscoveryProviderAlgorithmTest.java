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

package org.apache.shardingsphere.dbdiscovery.mysql.type;

import org.apache.shardingsphere.dbdiscovery.factory.DatabaseDiscoveryProviderAlgorithmFactory;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MGRDatabaseDiscoveryProviderAlgorithmTest {
    
    @Test
    public void assertCheckEnvironment() throws SQLException {
        Properties props = new Properties();
        props.setProperty("group-name", "foo_group");
        DatabaseDiscoveryProviderAlgorithm actual = DatabaseDiscoveryProviderAlgorithmFactory.newInstance(new AlgorithmConfiguration("MySQL.MGR", props));
        actual.checkEnvironment("foo_db", Collections.singletonList(mockEnvironmentAvailableDataSource()));
    }
    
    private DataSource mockEnvironmentAvailableDataSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().createStatement().executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, true, false);
        when(resultSet.getString("PLUGIN_STATUS")).thenReturn("ACTIVE");
        when(resultSet.getString("VARIABLE_VALUE")).thenReturn("ON", "foo_group");
        when(resultSet.getString("MEMBER_HOST")).thenReturn("127.0.0.1");
        when(resultSet.getString("MEMBER_PORT")).thenReturn("3306");
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        return result;
    }
    
    @Test
    public void assertIsPrimaryInstance() throws SQLException {
        DatabaseDiscoveryProviderAlgorithm actual = DatabaseDiscoveryProviderAlgorithmFactory.newInstance(new AlgorithmConfiguration("MySQL.MGR", new Properties()));
        assertTrue(actual.isPrimaryInstance(mockPrimaryDataSource()));
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
    public void assertLoadReplicaStatus() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        ReplicaDataSourceStatus actual = new MGRMySQLDatabaseDiscoveryProviderAlgorithm().loadReplicaStatus(dataSource);
        assertFalse(actual.isOnline());
        assertThat(actual.getReplicationDelayMilliseconds(), is(0L));
    }
}
