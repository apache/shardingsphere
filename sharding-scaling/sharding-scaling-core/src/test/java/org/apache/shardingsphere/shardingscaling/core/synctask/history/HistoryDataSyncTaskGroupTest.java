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

package org.apache.shardingsphere.shardingscaling.core.synctask.history;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.shardingscaling.core.util.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class HistoryDataSyncTaskGroupTest {
    
    private static String dataSourceUrl = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static String userName = "root";
    
    private static String password = "password";
    
    private SyncConfiguration syncConfiguration;
    
    private DataSourceManager dataSourceManager;
    
    @Before
    public void setUp() {
        RdbmsConfiguration readerConfig = mockReaderConfig();
        RdbmsConfiguration writerConfig = new RdbmsConfiguration();
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put("t_order", "t_order");
        syncConfiguration = new SyncConfiguration(3, tableMap,
                readerConfig, writerConfig);
        dataSourceManager = new DataSourceManager();
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    public void assertPrepareWithIntPrimaryRangeSplit() throws NoSuchFieldException, IllegalAccessException {
        initIntPrimaryEnvironment(syncConfiguration.getReaderConfiguration());
        HistoryDataSyncTaskGroup historyDataSyncTaskGroup = new HistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        historyDataSyncTaskGroup.prepare();
        List<SyncTask> syncTasks = ReflectionUtil.getFieldValueFromClass(historyDataSyncTaskGroup, "syncTasks", List.class);
        assertNotNull(syncTasks);
        assertThat(syncTasks.size(), is(3));
    }
    
    @Test
    public void assertPrepareWithCharPrimaryRangeSplit() throws NoSuchFieldException, IllegalAccessException {
        initCharPrimaryEnvironment(syncConfiguration.getReaderConfiguration());
        HistoryDataSyncTaskGroup historyDataSyncTaskGroup = new HistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        historyDataSyncTaskGroup.prepare();
        List<SyncTask> syncTasks = ReflectionUtil.getFieldValueFromClass(historyDataSyncTaskGroup, "syncTasks", List.class);
        assertNotNull(syncTasks);
        assertThat(syncTasks.size(), is(1));
    }
    
    @Test
    public void assertPrepareWithUnionPrimaryRangeSplit() throws NoSuchFieldException, IllegalAccessException {
        initUnionPrimaryEnvironment(syncConfiguration.getReaderConfiguration());
        HistoryDataSyncTaskGroup historyDataSyncTaskGroup = new HistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        historyDataSyncTaskGroup.prepare();
        List<SyncTask> syncTasks = ReflectionUtil.getFieldValueFromClass(historyDataSyncTaskGroup, "syncTasks", List.class);
        assertNotNull(syncTasks);
        assertThat(syncTasks.size(), is(1));
    }
    
    @Test
    public void assertPrepareWithoutPrimaryRangeSplit() throws NoSuchFieldException, IllegalAccessException {
        initNoPrimaryEnvironment(syncConfiguration.getReaderConfiguration());
        HistoryDataSyncTaskGroup historyDataSyncTaskGroup = new HistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        historyDataSyncTaskGroup.prepare();
        List<SyncTask> syncTasks = ReflectionUtil.getFieldValueFromClass(historyDataSyncTaskGroup, "syncTasks", List.class);
        assertNotNull(syncTasks);
        assertThat(syncTasks.size(), is(1));
    }
    
    @Test
    public void assertStart() throws NoSuchFieldException, IllegalAccessException {
        SyncTask syncTask = mock(SyncTask.class);
        HistoryDataSyncTaskGroup historyDataSyncTaskGroup = new HistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        List<SyncTask> syncTasks = new LinkedList<>();
        syncTasks.add(syncTask);
        ReflectionUtil.setFieldValueToClass(historyDataSyncTaskGroup, "syncTasks", syncTasks);
        historyDataSyncTaskGroup.start(event -> { });
        verify(syncTask).start(any(ReportCallback.class));
    }
    
    @Test
    public void assertStop() throws NoSuchFieldException, IllegalAccessException {
        SyncTask syncTask = mock(SyncTask.class);
        HistoryDataSyncTaskGroup historyDataSyncTaskGroup = new HistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        List<SyncTask> syncTasks = new LinkedList<>();
        syncTasks.add(syncTask);
        ReflectionUtil.setFieldValueToClass(historyDataSyncTaskGroup, "syncTasks", syncTasks);
        historyDataSyncTaskGroup.stop();
        verify(syncTask).stop();
    }
    
    @Test
    public void assertGetProgress() {
        HistoryDataSyncTaskGroup historyDataSyncTaskGroup = new HistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        assertThat(historyDataSyncTaskGroup.getProgress(), instanceOf(SyncProgress.class));
    }
    
    @SneakyThrows
    private void initIntPrimaryEnvironment(final RdbmsConfiguration readerConfig) {
        DataSource dataSource = dataSourceManager.getDataSource(readerConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @SneakyThrows
    private void initCharPrimaryEnvironment(final RdbmsConfiguration readerConfig) {
        DataSource dataSource = dataSourceManager.getDataSource(readerConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id CHAR(3) PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES ('1', 'xxx'), ('999', 'yyy')");
        }
    }
    
    @SneakyThrows
    private void initUnionPrimaryEnvironment(final RdbmsConfiguration readerConfig) {
        DataSource dataSource = dataSourceManager.getDataSource(readerConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT, user_id VARCHAR(12), PRIMARY KEY (id, user_id))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @SneakyThrows
    private void initNoPrimaryEnvironment(final RdbmsConfiguration readerConfig) {
        DataSource dataSource = dataSourceManager.getDataSource(readerConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    private RdbmsConfiguration mockReaderConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JDBCDataSourceConfiguration(dataSourceUrl, userName, password);
        RdbmsConfiguration result = new RdbmsConfiguration();
        result.setDataSourceConfiguration(dataSourceConfiguration);
        return result;
    }
}
