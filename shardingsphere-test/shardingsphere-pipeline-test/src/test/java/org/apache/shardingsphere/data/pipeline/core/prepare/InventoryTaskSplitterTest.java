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

package org.apache.shardingsphere.data.pipeline.core.prepare;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineTableMetaDataUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationTaskConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class InventoryTaskSplitterTest {
    
    private MigrationJobItemContext jobItemContext;
    
    private MigrationTaskConfiguration taskConfig;
    
    private PipelineDataSourceManager dataSourceManager;
    
    private InventoryTaskSplitter inventoryTaskSplitter;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        initJobItemContext();
        InventoryDumperConfiguration dumperConfig = new InventoryDumperConfiguration(jobItemContext.getTaskConfig().getDumperConfig());
        dumperConfig.setUniqueKeyDataType(Types.INTEGER);
        dumperConfig.setUniqueKey("order_id");
        inventoryTaskSplitter = new InventoryTaskSplitter(
                jobItemContext.getSourceDataSource(), dumperConfig, jobItemContext.getTaskConfig().getImporterConfig(), jobItemContext.getInitProgress(),
                jobItemContext.getSourceMetaDataLoader(), jobItemContext.getDataSourceManager(), jobItemContext.getJobProcessContext().getImporterExecuteEngine());
    }
    
    private void initJobItemContext() throws NoSuchFieldException, IllegalAccessException {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        ReflectionUtil.setFieldValue(jobConfig, "uniqueKeyColumn", new PipelineColumnMetaData(1, "order_id", 4, "", false, true));
        jobItemContext = PipelineContextUtil.mockMigrationJobItemContext(jobConfig);
        dataSourceManager = jobItemContext.getDataSourceManager();
        taskConfig = jobItemContext.getTaskConfig();
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    public void assertSplitInventoryDataWithEmptyTable() throws SQLException {
        initEmptyTablePrimaryEnvironment(taskConfig.getDumperConfig());
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        assertThat(actual.size(), is(1));
        InventoryTask task = actual.get(0);
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getBeginValue(), is(0L));
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getEndValue(), is(0L));
    }
    
    @Test
    public void assertSplitInventoryDataWithIntPrimary() throws SQLException {
        initIntPrimaryEnvironment(taskConfig.getDumperConfig());
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        assertThat(actual.size(), is(10));
        InventoryTask task = actual.get(9);
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getBeginValue(), is(91L));
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getEndValue(), is(100L));
    }
    
    @Test
    public void assertSplitInventoryDataWithCharPrimary() throws SQLException {
        initCharPrimaryEnvironment(taskConfig.getDumperConfig());
        inventoryTaskSplitter.splitInventoryData(jobItemContext);
    }
    
    @Test
    public void assertSplitInventoryDataWithoutPrimaryButWithUniqueIndex() throws SQLException {
        initUniqueIndexOnNotNullColumnEnvironment(taskConfig.getDumperConfig());
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertSplitInventoryDataWithIllegalKeyDataType() throws SQLException, NoSuchFieldException, IllegalAccessException {
        initUnionPrimaryEnvironment(taskConfig.getDumperConfig());
        InventoryDumperConfiguration dumperConfig = ReflectionUtil.getFieldValue(inventoryTaskSplitter, "dumperConfig", InventoryDumperConfiguration.class);
        assertNotNull(dumperConfig);
        dumperConfig.setUniqueKey("order_id,user_id");
        dumperConfig.setUniqueKeyDataType(Integer.MIN_VALUE);
        inventoryTaskSplitter.splitInventoryData(jobItemContext);
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertSplitInventoryDataWithoutPrimaryAndUniqueIndex() throws SQLException, NoSuchFieldException, IllegalAccessException {
        initNoPrimaryEnvironment(taskConfig.getDumperConfig());
        try (PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(taskConfig.getDumperConfig().getDataSourceConfig())) {
            PipelineColumnMetaData uniqueKeyColumn = PipelineTableMetaDataUtil.getUniqueKeyColumn(null, "t_order", dataSource, null);
            ReflectionUtil.setFieldValue(jobItemContext.getJobConfig(), "uniqueKeyColumn", uniqueKeyColumn);
        }
        inventoryTaskSplitter.splitInventoryData(jobItemContext);
    }
    
    private void initEmptyTablePrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
        }
    }
    
    private void initIntPrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (
                Connection connection = dataSource.getConnection();
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
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id CHAR(3) PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES ('1', 'xxx'), ('999', 'yyy')");
        }
    }
    
    private void initUnionPrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT, user_id VARCHAR(12), PRIMARY KEY (order_id, user_id))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    private void initNoPrimaryEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    private void initUniqueIndexOnNotNullColumnEnvironment(final DumperConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT NOT NULL, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
            statement.execute("CREATE UNIQUE INDEX unique_order_id ON t_order (order_id)");
        }
    }
}
