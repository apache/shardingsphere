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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered.prepare;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class InventoryTaskSplitterTest {
    
    private RuleAlteredJobContext jobContext;
    
    private TaskConfiguration taskConfig;
    
    private PipelineDataSourceManager dataSourceManager;
    
    private InventoryTaskSplitter inventoryTaskSplitter;
    
    @Before
    public void setUp() {
        initJobContext();
        inventoryTaskSplitter = new InventoryTaskSplitter();
    }
    
    private void initJobContext() {
        jobContext = new RuleAlteredJobContext(JobConfigurationBuilder.createJobConfiguration());
        dataSourceManager = jobContext.getDataSourceManager();
        taskConfig = jobContext.getTaskConfig();
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    public void assertSplitInventoryDataWithEmptyTable() throws SQLException {
        taskConfig.getHandleConfig().setShardingSize(10);
        initEmptyTablePrimaryEnvironment(taskConfig.getDumperConfig());
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobContext);
        assertNotNull(actual);
        assertThat(actual.size(), is(1));
        assertThat(((PrimaryKeyPosition) actual.get(0).getProgress().getPosition()).getBeginValue(), is(0L));
        assertThat(((PrimaryKeyPosition) actual.get(0).getProgress().getPosition()).getEndValue(), is(0L));
    }
    
    @Test
    public void assertSplitInventoryDataWithIntPrimary() throws SQLException {
        taskConfig.getHandleConfig().setShardingSize(10);
        initIntPrimaryEnvironment(taskConfig.getDumperConfig());
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobContext);
        assertNotNull(actual);
        assertThat(actual.size(), is(10));
        assertThat(((PrimaryKeyPosition) actual.get(9).getProgress().getPosition()).getBeginValue(), is(91L));
        assertThat(((PrimaryKeyPosition) actual.get(9).getProgress().getPosition()).getEndValue(), is(100L));
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertSplitInventoryDataWithCharPrimary() throws SQLException {
        initCharPrimaryEnvironment(taskConfig.getDumperConfig());
        inventoryTaskSplitter.splitInventoryData(jobContext);
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertSplitInventoryDataWithUnionPrimary() throws SQLException {
        initUnionPrimaryEnvironment(taskConfig.getDumperConfig());
        inventoryTaskSplitter.splitInventoryData(jobContext);
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertSplitInventoryDataWithoutPrimary() throws SQLException {
        initNoPrimaryEnvironment(taskConfig.getDumperConfig());
        inventoryTaskSplitter.splitInventoryData(jobContext);
    }
    
    private void initEmptyTablePrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
        }
    }
    
    private void initIntPrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            for (int i = 1; i <= 100; i++) {
                statement.execute(String.format("INSERT INTO t_order (order_id, user_id) VALUES (%d, 'x')", i));
            }
        }
    }
    
    private void initCharPrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id CHAR(3) PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES ('1', 'xxx'), ('999', 'yyy')");
        }
    }
    
    private void initUnionPrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT, user_id VARCHAR(12), PRIMARY KEY (order_id, user_id))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    private void initNoPrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
}
