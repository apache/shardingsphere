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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertFalse;

public final class InventoryTaskTest {
    
    private static TaskConfiguration taskConfig;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfig();
        taskConfig = new RuleAlteredJobContext(ResourceUtil.mockJobConfig()).getTaskConfig();
    }
    
    @Test(expected = IngestException.class)
    public void assertStartWithGetEstimatedRowsFailure() {
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(taskConfig.getDumperConfig());
        inventoryDumperConfig.setTableName("t_non_exist");
        IngestPosition<?> position = taskConfig.getDumperConfig().getPosition();
        if (null == position) {
            position = new PrimaryKeyPosition(0, 1000);
        }
        inventoryDumperConfig.setPosition(position);
        try (InventoryTask inventoryTask = new InventoryTask(inventoryDumperConfig, taskConfig.getImporterConfig(),
                PipelineContextUtil.getPipelineChannelFactory(),
                new PipelineDataSourceManager(), PipelineContextUtil.getExecuteEngine())) {
            inventoryTask.start();
        }
    }
    
    @Test
    public void assertGetProgress() throws SQLException {
        initTableData(taskConfig.getDumperConfig());
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(taskConfig.getDumperConfig());
        inventoryDumperConfig.setTableName("t_order");
        IngestPosition<?> position = taskConfig.getDumperConfig().getPosition();
        if (null == position) {
            position = new PrimaryKeyPosition(0, 1000);
        }
        inventoryDumperConfig.setPosition(position);
        try (InventoryTask inventoryTask = new InventoryTask(inventoryDumperConfig, taskConfig.getImporterConfig(),
                PipelineContextUtil.getPipelineChannelFactory(),
                new PipelineDataSourceManager(), PipelineContextUtil.getExecuteEngine())) {
            inventoryTask.start();
            assertFalse(inventoryTask.getProgress().getPosition() instanceof FinishedPosition);
        }
    }
    
    private void initTableData(final DumperConfiguration dumperConfig) throws SQLException {
        try (PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
             PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
}
