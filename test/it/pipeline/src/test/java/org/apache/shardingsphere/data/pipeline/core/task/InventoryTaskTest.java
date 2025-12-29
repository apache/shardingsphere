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

import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InventoryTaskTest {
    
    private static final PipelineDataSourceManager DATA_SOURCE_MANAGER = new PipelineDataSourceManager();
    
    private MigrationTaskConfiguration taskConfig;
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.initPipelineContextManager();
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
    void assertGetProgress() throws SQLException, ExecutionException, InterruptedException, TimeoutException {
        initTableData(taskConfig.getDumperContext());
        // TODO use t_order_0, and also others
        InventoryDumperContext inventoryDumperContext = createInventoryDumperContext("t_order", "t_order");
        AtomicReference<IngestPosition> position = new AtomicReference<>(inventoryDumperContext.getCommonContext().getPosition());
        InventoryTask inventoryTask = new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(inventoryDumperContext),
                PipelineContextUtils.getExecuteEngine(), PipelineContextUtils.getExecuteEngine(), mock(Dumper.class), mock(Importer.class), position);
        CompletableFuture.allOf(inventoryTask.start().toArray(new CompletableFuture[0])).get(10L, TimeUnit.SECONDS);
        assertThat(inventoryTask.getTaskProgress().getPosition(), isA(IntegerPrimaryKeyIngestPosition.class));
    }
    
    private void initTableData(final IncrementalDumperContext dumperContext) throws SQLException {
        PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
        try (
                PipelineDataSource dataSource = dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig());
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
        dataSourceManager.close();
    }
    
    private InventoryDumperContext createInventoryDumperContext(final String logicTableName, final String actualTableName) {
        InventoryDumperContext result = new InventoryDumperContext(taskConfig.getDumperContext().getCommonContext());
        result.setLogicTableName(logicTableName);
        result.setActualTableName(actualTableName);
        result.setUniqueKeyColumns(Collections.singletonList(PipelineContextUtils.mockOrderIdColumnMetaData()));
        result.getCommonContext().setPosition(null == taskConfig.getDumperContext().getCommonContext().getPosition()
                ? new IntegerPrimaryKeyIngestPosition(BigInteger.ONE, BigInteger.valueOf(1000L))
                : taskConfig.getDumperContext().getCommonContext().getPosition());
        return result;
    }
    
    @Test
    void assertStop() {
        Dumper dumper = mock(Dumper.class);
        Importer importer = mock(Importer.class);
        InventoryDumperContext inventoryDumperContext = createInventoryDumperContext("t_order", "t_order");
        AtomicReference<IngestPosition> position = new AtomicReference<>(inventoryDumperContext.getCommonContext().getPosition());
        InventoryTask inventoryTask = new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(inventoryDumperContext),
                PipelineContextUtils.getExecuteEngine(), PipelineContextUtils.getExecuteEngine(), dumper, importer, position);
        inventoryTask.stop();
        verify(dumper).stop();
        verify(importer).stop();
    }
}
