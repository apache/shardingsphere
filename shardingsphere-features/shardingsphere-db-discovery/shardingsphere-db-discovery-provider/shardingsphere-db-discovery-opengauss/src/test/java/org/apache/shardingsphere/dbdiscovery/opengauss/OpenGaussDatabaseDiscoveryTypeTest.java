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

package org.apache.shardingsphere.dbdiscovery.opengauss;

import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussDatabaseDiscoveryTypeTest {
    
    private static final String DB_ROLE = "select local_role,db_state from pg_stat_get_stream_replications()";
    
    private static final String STANDBYS = "select client_addr,sync_state from pg_stat_replication";
    
    private final OpenGaussDatabaseDiscoveryType ogHaType = new OpenGaussDatabaseDiscoveryType();
    
    @Test
    public void assertCheckHAConfig() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(DB_ROLE)).thenReturn(resultSet);
        when(statement.executeQuery(STANDBYS)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false, true, false, true, false, true, false);
        when(resultSet.getString("local_role")).thenReturn("Primary");
        when(resultSet.getString("db_state")).thenReturn("Normal");
        when(resultSet.getString("db_state")).thenReturn("Sync");
        Map<String, DataSource> dataSourceMap = mock(HashMap.class);
        when(dataSourceMap.get(null)).thenReturn(dataSource);
        ogHaType.getProps().setProperty("groupName", "group_name");
        ogHaType.checkDatabaseDiscoveryConfiguration("discovery_db", dataSourceMap);
    }
    
    @Test
    public void assertUpdatePrimaryDataSource() throws SQLException {
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
        for (int i = 0; i < 3; i++) {
            when(dataSources.get(i).getConnection()).thenReturn(connections.get(i));
            when(connections.get(i).createStatement()).thenReturn(statements.get(i));
            when(statements.get(i).executeQuery(DB_ROLE)).thenReturn(resultSets.get(i));
            when(resultSets.get(i).next()).thenReturn(true, false);
            when(resultSets.get(i).getString("local_role")).thenReturn("Primary");
            when(resultSets.get(i).getString("db_state")).thenReturn("Normal");
            when(connections.get(i).getMetaData()).thenReturn(databaseMetaData.get(i));
            when(databaseMetaData.get(i).getURL()).thenReturn("jdbc:postgres://127.0.0.1:" + (3306 + i) + "/ds_0");
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        for (int i = 0; i < 3; i++) {
            dataSourceMap.put(String.format("ds_%s", i), dataSources.get(i));
        }
        ogHaType.getProps().setProperty("groupName", "group_name");
        ogHaType.updatePrimaryDataSource("discovery_db", dataSourceMap, Collections.emptySet(), "group_name");
        assertThat(ogHaType.getPrimaryDataSource(), is("ds_2"));
    }
}
