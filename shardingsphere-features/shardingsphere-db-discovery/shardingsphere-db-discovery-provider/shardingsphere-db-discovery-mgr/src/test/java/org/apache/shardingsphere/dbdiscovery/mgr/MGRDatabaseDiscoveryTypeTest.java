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
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.zookeeper.CreateMode;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MGRDatabaseDiscoveryTypeTest {
    
    private static final String PLUGIN_STATUS = "SELECT * FROM information_schema.PLUGINS WHERE PLUGIN_NAME='group_replication'";
    
    private static final String MEMBER_COUNT = "SELECT count(*) FROM performance_schema.replication_group_members";
    
    private static final String GROUP_NAME = "SELECT * FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_group_name'";
    
    private static final String SINGLE_PRIMARY = "SELECT * FROM performance_schema.global_variables WHERE VARIABLE_NAME='group_replication_single_primary_mode'";
    
    private final MGRDatabaseDiscoveryType mgrHaType = new MGRDatabaseDiscoveryType();
    
    @Test
    public void assertCheckHAConfig() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
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
        Map<String, DataSource> dataSourceMap = mock(HashMap.class);
        when(dataSourceMap.get(null)).thenReturn(dataSource);
        mgrHaType.getProps().setProperty("groupName", "group_name");
        mgrHaType.checkDatabaseDiscoveryConfiguration("discovery_db", dataSourceMap);
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
        mgrHaType.getProps().setProperty("groupName", "group_name");
        mgrHaType.updatePrimaryDataSource("discovery_db", dataSourceMap, Collections.emptySet(), "group_name");
        assertThat(mgrHaType.getPrimaryDataSource(), is("ds_2"));
    }
    
    @Test
    public void assertUpdateMemberState() throws SQLException, IllegalAccessException, NoSuchFieldException {
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
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        List<String> disabledDataSourceNames = Arrays.asList("ds_1");
        for (int i = 0; i < 3; i++) {
            dataSourceMap.put(String.format("ds_%s", i), dataSources.get(i));
        }
        mgrHaType.updateMemberState("discovery_db", dataSourceMap, disabledDataSourceNames);
        verify(eventBus).post(Mockito.refEq(new DataSourceDisabledEvent("discovery_db", "ds_2", true)));
    }
    
    @Test
    @Ignore
    public void assertStartPeriodicalUpdate() throws NoSuchFieldException, IllegalAccessException {
        Properties props = mock(Properties.class);
        when(props.getProperty("zkServerLists")).thenReturn("127.0.0.1:2181");
        when(props.getProperty("keepAliveCron")).thenReturn("0/5 * * * * ?");
        Field propsFiled = MGRDatabaseDiscoveryType.class.getDeclaredField("props");
        propsFiled.setAccessible(true);
        propsFiled.set(mgrHaType, props);
        final Map<String, ScheduleJobBootstrap> scheduleJobHashMap = spy(HashMap.class);
        Field field = MGRDatabaseDiscoveryType.class.getDeclaredField("SCHEDULE_JOB_BOOTSTRAP_MAP");
        makeAccessible(field);
        field.set(mgrHaType, scheduleJobHashMap);
        Map<String, DataSource> originalDataSourceMap = new HashMap<>(3, 1);
        mgrHaType.startPeriodicalUpdate("discovery_db", originalDataSourceMap, null, "group_name");
        verify(scheduleJobHashMap, times(2)).get("group_name");
        assertThat(scheduleJobHashMap.get("group_name").getClass(), Matchers.equalTo(ScheduleJobBootstrap.class));
        scheduleJobHashMap.get("group_name").shutdown();
    }
    
    @Test
    @Ignore
    public void assertUpdateProperties() throws Exception {
        Properties props = mock(Properties.class);
        when(props.getProperty("zkServerLists")).thenReturn("127.0.0.1:2181");
        when(props.getProperty("keepAliveCron")).thenReturn("0/5 * * * * ?");
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(props.getProperty("zkServerLists"), "");
        CoordinatorRegistryCenter coordinatorRegistryCenter = new ZookeeperRegistryCenter(zkConfig);
        coordinatorRegistryCenter.init();
        ((CuratorFramework) coordinatorRegistryCenter.getRawClient()).create().withMode(CreateMode.PERSISTENT).forPath("/MGR-group_name", "123".getBytes("utf-8"));
        ((CuratorFramework) coordinatorRegistryCenter.getRawClient()).create().withMode(CreateMode.PERSISTENT).forPath("/MGR-group_name/config", "123".getBytes("utf-8"));
        mgrHaType.updateProperties("group_name", props);
        assertThat(coordinatorRegistryCenter.get("/MGR-group_name/config"), is("cron: 0/5 * * * * ?\n" + "disabled: false\n"
                + "failover: false\n" + "jobName: MGR-group_name\n" + "maxTimeDiffSeconds: -1\n" + "misfire: false\n"
                + "monitorExecution: false\n" + "overwrite: false\n" + "reconcileIntervalMinutes: 0\n" + "shardingTotalCount: 1\n" + "staticSharding: false\n"));
    }
    
    @SneakyThrows
    private void makeAccessible(final Field field) {
        field.setAccessible(true);
        Field modifiersField = getModifiersField();
        modifiersField.setAccessible(true);
        modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);
    }
    
    @SneakyThrows
    private Field getModifiersField() {
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
        for (Field each : fields) {
            if ("modifiers".equals(each.getName())) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
}
