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

package org.apache.shardingsphere.data.pipeline.core.task;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixturePipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationTaskConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class InventoryTaskTest {
    
    private static final PipelineDataSourceManager DATA_SOURCE_MANAGER = new DefaultPipelineDataSourceManager();
    
    private MigrationTaskConfiguration taskConfig;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @AfterClass
    public static void afterClass() {
        DATA_SOURCE_MANAGER.close();
    }
    
    @Before
    public void setUp() {
        taskConfig = PipelineContextUtil.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration()).getTaskConfig();
    }
    
    @Test(expected = IngestException.class)
    public void assertStartWithGetEstimatedRowsFailure() {
        InventoryDumperConfiguration inventoryDumperConfig = createInventoryDumperConfiguration("t_non_exist", "t_non_exist");
        PipelineDataSourceWrapper dataSource = DATA_SOURCE_MANAGER.getDataSource(inventoryDumperConfig.getDataSourceConfig());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(dataSource);
        try (
                InventoryTask inventoryTask = new InventoryTask(inventoryDumperConfig, taskConfig.getImporterConfig(),
                        PipelineContextUtil.getPipelineChannelCreator(),
                        DATA_SOURCE_MANAGER, dataSource, metaDataLoader, PipelineContextUtil.getExecuteEngine(), new FixturePipelineJobProgressListener())) {
            inventoryTask.start();
        }
    }
    
    @Test
    public void assertGetProgress() throws SQLException {
        initTableData(taskConfig.getDumperConfig());
        // TODO use t_order_0, and also others
        InventoryDumperConfiguration inventoryDumperConfig = createInventoryDumperConfiguration("t_order", "t_order");
        PipelineDataSourceWrapper dataSource = DATA_SOURCE_MANAGER.getDataSource(inventoryDumperConfig.getDataSourceConfig());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(dataSource);
        try (
                InventoryTask inventoryTask = new InventoryTask(inventoryDumperConfig, taskConfig.getImporterConfig(),
                        PipelineContextUtil.getPipelineChannelCreator(),
                        new DefaultPipelineDataSourceManager(), dataSource, metaDataLoader, PipelineContextUtil.getExecuteEngine(), new FixturePipelineJobProgressListener())) {
            inventoryTask.start();
            assertThat(inventoryTask.getTaskProgress().getPosition(), instanceOf(IntegerPrimaryKeyPosition.class));
        }
    }
    
    private void initTableData(final DumperConfiguration dumperConfig) throws SQLException {
        PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
        try (
                PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
        dataSourceManager.close();
    }
    
    private InventoryDumperConfiguration createInventoryDumperConfiguration(final String logicTableName, final String actualTableName) {
        InventoryDumperConfiguration result = new InventoryDumperConfiguration(taskConfig.getDumperConfig());
        result.setLogicTableName(logicTableName);
        result.setActualTableName(actualTableName);
        result.setUniqueKey("order_id");
        result.setUniqueKeyDataType(Types.INTEGER);
        result.setPosition(null == taskConfig.getDumperConfig().getPosition() ? new IntegerPrimaryKeyPosition(0, 1000) : taskConfig.getDumperConfig().getPosition());
        return result;
    }
}
