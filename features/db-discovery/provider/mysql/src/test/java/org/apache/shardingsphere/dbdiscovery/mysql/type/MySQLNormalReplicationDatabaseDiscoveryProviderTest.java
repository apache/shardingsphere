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

import org.apache.shardingsphere.dbdiscovery.mysql.exception.replica.DuplicatePrimaryDataSourceException;
import org.apache.shardingsphere.dbdiscovery.mysql.exception.replica.PrimaryDataSourceNotFoundException;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProvider;
import org.apache.shardingsphere.dbdiscovery.spi.ReplicaDataSourceStatus;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLNormalReplicationDatabaseDiscoveryProviderTest {
    
    @Test
    public void assertCheckEnvironmentNoPrimaryDataSource() {
        assertThrows(PrimaryDataSourceNotFoundException.class,
                () -> new MySQLNormalReplicationDatabaseDiscoveryProvider().checkEnvironment("foo_db", Collections.singleton(mockDataSourceForReplicationInstances("ON"))));
    }
    
    @Test
    public void assertCheckEnvironmentHasSinglePrimaryDataSource() throws SQLException {
        new MySQLNormalReplicationDatabaseDiscoveryProvider().checkEnvironment("foo_db", Collections.singleton(mockDataSourceForReplicationInstances("OFF")));
    }
    
    @Test
    public void assertCheckEnvironmentHasManyPrimaryDataSources() {
        assertThrows(DuplicatePrimaryDataSourceException.class,
                () -> new MySQLNormalReplicationDatabaseDiscoveryProvider().checkEnvironment("foo_db", Arrays.asList(mockDataSourceForReplicationInstances("OFF"),
                        mockDataSourceForReplicationInstances("OFF"))));
    }
    
    @Test
    public void assertIsPrimaryInstance() throws SQLException {
        assertTrue(new MySQLNormalReplicationDatabaseDiscoveryProvider().isPrimaryInstance(mockDataSourceForReplicationInstances("OFF")));
    }
    
    private DataSource mockDataSourceForReplicationInstances(final String readOnly) throws SQLException {
        ResultSet slaveHostsResultSet = mock(ResultSet.class);
        when(slaveHostsResultSet.next()).thenReturn(true, false);
        when(slaveHostsResultSet.getString("Host")).thenReturn("127.0.0.1");
        when(slaveHostsResultSet.getString("Port")).thenReturn("3306");
        ResultSet readonlyResultSet = mock(ResultSet.class);
        when(readonlyResultSet.next()).thenReturn(true, false);
        when(readonlyResultSet.getString("Value")).thenReturn(readOnly);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.createStatement().executeQuery("SHOW SLAVE HOSTS")).thenReturn(slaveHostsResultSet);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1:3306/foo_ds");
        when(connection.createStatement().executeQuery("SHOW VARIABLES LIKE 'read_only'")).thenReturn(readonlyResultSet);
        return new MockedDataSource(connection);
    }
    
    @Test
    public void assertLoadReplicaStatus() throws SQLException {
        DatabaseDiscoveryProvider provider = new MySQLNormalReplicationDatabaseDiscoveryProvider();
        provider.init(PropertiesBuilder.build(new Property("delay-milliseconds-threshold", "15000")));
        DataSource dataSource = mockDataSourceForReplicaStatus();
        ReplicaDataSourceStatus actual = provider.loadReplicaStatus(dataSource);
        assertTrue(actual.isOnline());
        assertThat(actual.getReplicationDelayMilliseconds(), is(10000L));
    }
    
    private DataSource mockDataSourceForReplicaStatus() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("Seconds_Behind_Master")).thenReturn(10L);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.createStatement().executeQuery("SHOW SLAVE STATUS")).thenReturn(resultSet);
        return new MockedDataSource(connection);
    }
}
