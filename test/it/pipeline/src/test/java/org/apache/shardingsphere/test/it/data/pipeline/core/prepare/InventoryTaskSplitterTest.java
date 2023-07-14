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

package org.apache.shardingsphere.test.it.data.pipeline.core.prepare;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.PipelineTableMetaDataUtils;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.preparer.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTaskSplitterTest {
    
    private MigrationJobItemContext jobItemContext;
    
    private InventoryDumperConfiguration dumperConfig;
    
    private PipelineDataSourceManager dataSourceManager;
    
    private InventoryTaskSplitter inventoryTaskSplitter;
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @BeforeEach
    void setUp() {
        initJobItemContext();
        dumperConfig = new InventoryDumperConfiguration(jobItemContext.getTaskConfig().getDumperConfig());
        PipelineColumnMetaData columnMetaData = new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "int", false, true, true);
        dumperConfig.setUniqueKeyColumns(Collections.singletonList(columnMetaData));
        inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), dumperConfig, jobItemContext.getTaskConfig().getImporterConfig());
    }
    
    private void initJobItemContext() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobItemContext = PipelineContextUtils.mockMigrationJobItemContext(jobConfig);
        dataSourceManager = jobItemContext.getDataSourceManager();
    }
    
    @AfterEach
    void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    void assertSplitInventoryDataWithEmptyTable() throws SQLException {
        initEmptyTablePrimaryEnvironment(dumperConfig);
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        assertThat(actual.size(), is(1));
        InventoryTask task = actual.get(0);
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getBeginValue(), is(0L));
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getEndValue(), is(0L));
    }
    
    @Test
    void assertSplitInventoryDataWithIntPrimary() throws SQLException {
        initIntPrimaryEnvironment(dumperConfig);
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        assertThat(actual.size(), is(10));
        InventoryTask task = actual.get(9);
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getBeginValue(), is(91L));
        assertThat(((IntegerPrimaryKeyPosition) task.getTaskProgress().getPosition()).getEndValue(), is(100L));
    }
    
    @Test
    void assertSplitInventoryDataWithCharPrimary() throws SQLException {
        initCharPrimaryEnvironment(dumperConfig);
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getTaskId(), is("ds_0.t_order#0"));
        IntegerPrimaryKeyPosition keyPosition = (IntegerPrimaryKeyPosition) actual.get(0).getTaskProgress().getPosition();
        assertThat(keyPosition.getBeginValue(), is(1L));
        assertThat(keyPosition.getEndValue(), is(999L));
    }
    
    @Test
    void assertSplitInventoryDataWithoutPrimaryButWithUniqueIndex() throws SQLException {
        initUniqueIndexOnNotNullColumnEnvironment(dumperConfig);
        List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertSplitInventoryDataWithMultipleColumnsKey() throws SQLException {
        initUnionPrimaryEnvironment(dumperConfig);
        try (PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig())) {
            List<PipelineColumnMetaData> uniqueKeyColumns = PipelineTableMetaDataUtils.getUniqueKeyColumns(null, "t_order", new StandardPipelineTableMetaDataLoader(dataSource));
            dumperConfig.setUniqueKeyColumns(uniqueKeyColumns);
            List<InventoryTask> actual = inventoryTaskSplitter.splitInventoryData(jobItemContext);
            assertThat(actual.size(), is(1));
        }
    }
    
    @Test
    void assertSplitInventoryDataWithoutPrimaryAndUniqueIndex() throws SQLException {
        initNoPrimaryEnvironment(dumperConfig);
        try (PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig())) {
            List<PipelineColumnMetaData> uniqueKeyColumns = PipelineTableMetaDataUtils.getUniqueKeyColumns(null, "t_order", new StandardPipelineTableMetaDataLoader(dataSource));
            assertTrue(uniqueKeyColumns.isEmpty());
            List<InventoryTask> inventoryTasks = inventoryTaskSplitter.splitInventoryData(jobItemContext);
            assertThat(inventoryTasks.size(), is(1));
        }
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
