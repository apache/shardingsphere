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

package org.apache.shardingsphere.test.it.data.pipeline.core.task;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class InventoryTaskTest {
    
    private static final PipelineDataSourceManager DATA_SOURCE_MANAGER = new DefaultPipelineDataSourceManager();
    
    private MigrationTaskConfiguration taskConfig;
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @AfterAll
    static void afterClass() {
        DATA_SOURCE_MANAGER.close();
    }
    
    @BeforeEach
    void setUp() {
        taskConfig = PipelineContextUtils.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration()).getTaskConfig();
    }
    
    @Test
    void assertStartWithGetEstimatedRowsFailure() {
        InventoryDumperConfiguration inventoryDumperConfig = createInventoryDumperConfiguration("t_non_exist", "t_non_exist");
        AtomicReference<IngestPosition> position = new AtomicReference<>(inventoryDumperConfig.getPosition());
        PipelineChannel channel = PipelineTaskUtils.createInventoryChannel(PipelineContextUtils.getPipelineChannelCreator(), 100, position);
        PipelineDataSourceWrapper dataSource = DATA_SOURCE_MANAGER.getDataSource(inventoryDumperConfig.getDataSourceConfig());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(dataSource);
        InventoryTask inventoryTask = new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(inventoryDumperConfig),
                PipelineContextUtils.getExecuteEngine(), PipelineContextUtils.getExecuteEngine(),
                new InventoryDumper(inventoryDumperConfig, channel, dataSource, metaDataLoader), mock(Importer.class), position);
        assertThrows(ExecutionException.class, () -> CompletableFuture.allOf(inventoryTask.start().toArray(new CompletableFuture[0])).get(10L, TimeUnit.SECONDS));
    }
    
    @Test
    void assertGetProgress() throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        initTableData(taskConfig.getDumperConfig());
        // TODO use t_order_0, and also others
        InventoryDumperConfiguration inventoryDumperConfig = createInventoryDumperConfiguration("t_order", "t_order");
        AtomicReference<IngestPosition> position = new AtomicReference<>(inventoryDumperConfig.getPosition());
        InventoryTask inventoryTask = new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(inventoryDumperConfig),
                PipelineContextUtils.getExecuteEngine(), PipelineContextUtils.getExecuteEngine(), mock(Dumper.class), mock(Importer.class), position);
        CompletableFuture.allOf(inventoryTask.start().toArray(new CompletableFuture[0])).get(10L, TimeUnit.SECONDS);
        assertThat(inventoryTask.getTaskProgress().getPosition(), instanceOf(IntegerPrimaryKeyPosition.class));
        inventoryTask.close();
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
        result.setUniqueKeyColumns(Collections.singletonList(PipelineContextUtils.mockOrderIdColumnMetaData()));
        result.setPosition(null == taskConfig.getDumperConfig().getPosition() ? new IntegerPrimaryKeyPosition(0, 1000) : taskConfig.getDumperConfig().getPosition());
        return result;
    }
}
