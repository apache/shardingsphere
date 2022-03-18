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

import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowSlaveStatusDatabaseDiscoveryTypeTest {
    
    private final ShowSlaveStatusDatabaseDiscoveryType showSlaveStatusDatabaseDiscoveryType = new ShowSlaveStatusDatabaseDiscoveryType();
    
    @Test
    public void assertUpdatePrimaryDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", getDataSource(false, 3306));
        dataSourceMap.put("ds_1", getDataSource(true, 3307));
        showSlaveStatusDatabaseDiscoveryType.updatePrimaryDataSource("discovery_db", dataSourceMap, Collections.emptySet(), "group_name");
        assertThat(showSlaveStatusDatabaseDiscoveryType.getPrimaryDataSource(), is("ds_0"));
    }
    
    private DataSource getDataSource(final boolean slave, final int port) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(result.getConnection()).thenReturn(connection);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery("SHOW SLAVE STATUS")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        if (slave) {
            when(resultSet.getString("Master_Host")).thenReturn("127.0.0.1");
            when(resultSet.getString("Master_Port")).thenReturn(Integer.toString(3306));
        }
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc:mysql://127.0.0.1:" + port + "/test?serverTimezone=UTC&useSSL=false");
        return result;
    }
}
