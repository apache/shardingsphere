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

import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProvider;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MGRDatabaseDiscoveryProviderTest {
    
    @Test
    public void assertCheckEnvironment() throws SQLException {
        DatabaseDiscoveryProvider actual = TypedSPILoader.getService(DatabaseDiscoveryProvider.class, "MySQL.MGR", PropertiesBuilder.build(new Property("group-name", "foo_group")));
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
        DatabaseDiscoveryProvider actual = TypedSPILoader.getService(DatabaseDiscoveryProvider.class, "MySQL.MGR");
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
        ReplicaDataSourceStatus actual = new MGRMySQLDatabaseDiscoveryProvider().loadReplicaStatus(dataSource);
        assertFalse(actual.isOnline());
        assertThat(actual.getReplicationDelayMilliseconds(), is(0L));
    }
}
