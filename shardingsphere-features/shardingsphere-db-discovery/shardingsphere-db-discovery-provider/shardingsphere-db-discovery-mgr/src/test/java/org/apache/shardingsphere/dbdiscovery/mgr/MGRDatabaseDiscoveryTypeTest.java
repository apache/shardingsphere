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

package org.apache.shardingsphere.dbdiscovery.mgr;

import com.google.common.eventbus.EventBus;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public final class MGRDatabaseDiscoveryTypeTest {
    
    private static final String PLUGIN_STATUS = "SELECT * FROM information_schema.PLUGINS WHERE PLUGIN_NAME='group_replication'";
    
    private static final String MEMBER_COUNT = "SELECT count(*) FROM performance_schema.replication_group_members";
    
    private static final String GROUP_NAME = "SELECT * FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_group_name'";
    
    private static final String SINGLE_PRIMARY = "SELECT * FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_single_primary_mode'";
    
    private final MGRDatabaseDiscoveryType mgrHaType = new MGRDatabaseDiscoveryType();
    
    @Test
    public void checkHAConfig() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(PLUGIN_STATUS)).thenReturn(resultSet);
            when(statement.executeQuery(MEMBER_COUNT)).thenReturn(resultSet);
            when(statement.executeQuery(GROUP_NAME)).thenReturn(resultSet);
            when(statement.executeQuery(SINGLE_PRIMARY)).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false, true, false, true, false, true, false);
            when(resultSet.getString("PLUGIN_STATUS")).thenReturn("ACTIVE");
            when(resultSet.getInt(1)).thenReturn(3);
            when(resultSet.getString("VARIABLE_VALUE")).thenReturn("group_name", "ON");
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
        Map<String, DataSource> dataSourceMap = mock(HashMap.class);
        when(dataSourceMap.get(null)).thenReturn(dataSource);
        try {
            mgrHaType.getProps().setProperty("groupName", "group_name");
            mgrHaType.checkDatabaseDiscoveryConfiguration("discovery_db", dataSourceMap);
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    @Test
    public void updatePrimaryDataSource() {
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
        try {
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
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        for (int i = 0; i < 3; i++) {
            dataSourceMap.put(String.format("ds_%s", i), dataSources.get(i));
        }
        mgrHaType.getProps().setProperty("groupName", "group_name");
        mgrHaType.updatePrimaryDataSource("discovery_db", dataSourceMap, Collections.emptySet(), "group_name");
        assertThat(mgrHaType.getPrimaryDataSource(), is("ds_2"));
    }
    
    @Test
    public void updateMemberState() throws IllegalAccessException, NoSuchFieldException {
        Field declaredField = MGRDatabaseDiscoveryType.class.getDeclaredField("oldPrimaryDataSource");
        declaredField.setAccessible(true);
        declaredField.set(mgrHaType, "ds_0");
        EventBus eventBus = mock(EventBus.class);
        mockStatic(ShardingSphereEventBus.class);
        when(ShardingSphereEventBus.getInstance()).thenReturn(eventBus);
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
        String sql = "SELECT MEMBER_HOST, MEMBER_PORT, MEMBER_STATE FROM performance_schema.replication_group_members";
        try {
            for (int i = 0; i < 3; i++) {
                when(dataSources.get(i).getConnection()).thenReturn(connections.get(i));
                when(connections.get(i).createStatement()).thenReturn(statements.get(i));
                when(statements.get(i).executeQuery(sql)).thenReturn(resultSets.get(i));
                when(resultSets.get(i).next()).thenReturn(true, false);
                when(resultSets.get(i).getString("MEMBER_HOST")).thenReturn("127.0.0.1");
                when(resultSets.get(i).getString("MEMBER_PORT")).thenReturn(Integer.toString(3306 + i));
                when(resultSets.get(i).getString("MEMBER_STATE")).thenReturn("ONLINE");
                when(connections.get(i).getMetaData()).thenReturn(databaseMetaData.get(i));
                when(databaseMetaData.get(i).getURL()).thenReturn("jdbc:mysql://127.0.0.1:" + (3306 + i) + "/ds_0?serverTimezone=UTC&useSSL=false");
            }
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        List<String> disabledDataSourceNames = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            dataSourceMap.put(String.format("ds_%s", i), dataSources.get(i));
            if (disabledDataSourceNames.isEmpty()) {
                disabledDataSourceNames.add(String.format("ds_1", i));
            }
        }
        mgrHaType.updateMemberState("discovery_db", dataSourceMap, disabledDataSourceNames);
        verify(eventBus).post(Mockito.refEq(new DataSourceDisabledEvent("discovery_db", "ds_2", true)));
    }
}
