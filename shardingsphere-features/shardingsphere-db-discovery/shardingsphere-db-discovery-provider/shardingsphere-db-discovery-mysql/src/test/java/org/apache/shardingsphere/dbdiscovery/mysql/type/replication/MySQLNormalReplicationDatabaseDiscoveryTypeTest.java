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

package org.apache.shardingsphere.dbdiscovery.mysql.type.replication;

import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLNormalReplicationDatabaseDiscoveryTypeTest {
    
    @Test
    public void assertLoadHighlyAvailableStatus() throws SQLException {
        MySQLNormalReplicationHighlyAvailableStatus actual = new MySQLNormalReplicationDatabaseDiscoveryType().loadHighlyAvailableStatus(mockDataSource(3306));
        assertThat(actual.getPrimaryInstanceURL(), is("127.0.0.1:3306"));
    }
    
    @Test
    public void assertFindPrimaryDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", mockDataSource(3306));
        dataSourceMap.put("ds_1", mockDataSource(3307));
        Optional<String> actual = new MySQLNormalReplicationDatabaseDiscoveryType().findPrimaryDataSource(dataSourceMap);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("ds_0"));
    }
    
    private DataSource mockDataSource(final int port) throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().createStatement().executeQuery("SHOW SLAVE STATUS")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Master_Host")).thenReturn("127.0.0.1");
        when(resultSet.getString("Master_Port")).thenReturn(Integer.toString(3306));
        when(result.getConnection().getMetaData().getURL()).thenReturn(String.format("jdbc:mysql://127.0.0.1:%s/test?serverTimezone=UTC&useSSL=false", port));
        return result;
    }
}
