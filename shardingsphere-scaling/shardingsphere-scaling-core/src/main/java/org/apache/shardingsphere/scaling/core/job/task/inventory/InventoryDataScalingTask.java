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

package org.apache.shardingsphere.scaling.core.job.task.inventory;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.scaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.scaling.core.execute.executor.AbstractShardingScalingExecutor;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.MemoryChannel;
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.Dumper;
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.DumperFactory;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.Importer;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.ImporterFactory;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.utils.RdbmsConfigurationUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Table slice execute task.
 */
@Slf4j
public final class InventoryDataScalingTask extends AbstractShardingScalingExecutor<InventoryPosition> implements ScalingTask<InventoryPosition> {
    
    private final InventoryDumperConfiguration inventoryDumperConfiguration;
    
    private final ImporterConfiguration importerConfiguration;
    
    private final DataSourceManager dataSourceManager;
    
    private long estimatedRows;
    
    private final AtomicLong syncedRows = new AtomicLong();
    
    private Dumper dumper;
    
    public InventoryDataScalingTask(final InventoryDumperConfiguration inventoryDumperConfiguration, final ImporterConfiguration importerConfiguration) {
        this(inventoryDumperConfiguration, importerConfiguration, new DataSourceManager());
    }
    
    @SuppressWarnings("unchecked")
    public InventoryDataScalingTask(final InventoryDumperConfiguration inventoryDumperConfiguration, final ImporterConfiguration importerConfiguration, final DataSourceManager dataSourceManager) {
        this.inventoryDumperConfiguration = inventoryDumperConfiguration;
        this.importerConfiguration = importerConfiguration;
        this.dataSourceManager = dataSourceManager;
        setTaskId(generateSyncTaskId(inventoryDumperConfiguration));
        setPositionManager(inventoryDumperConfiguration.getPositionManager());
    }
    
    private String generateSyncTaskId(final InventoryDumperConfiguration inventoryDumperConfiguration) {
        String result = String.format("%s.%s", inventoryDumperConfiguration.getDataSourceName(), inventoryDumperConfiguration.getTableName());
        return null == inventoryDumperConfiguration.getSpiltNum() ? result : result + "#" + inventoryDumperConfiguration.getSpiltNum();
    }
    
    @Override
    public void start() {
        getEstimatedRows();
        instanceDumper();
        Importer importer = ImporterFactory.newInstance(importerConfiguration, dataSourceManager);
        instanceChannel(importer);
        Future<?> future = ScalingContext.getInstance().getImporterExecuteEngine().submit(importer, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("get an error when migrating the inventory data", throwable);
                dumper.stop();
            }
        });
        dumper.start();
        waitForResult(future);
        dataSourceManager.close();
    }
    
    private void getEstimatedRows() {
        DataSource dataSource = dataSourceManager.getDataSource(inventoryDumperConfiguration.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.prepareStatement(String.format("SELECT COUNT(*) FROM %s %s",
                    inventoryDumperConfiguration.getTableName(),
                    RdbmsConfigurationUtil.getWhereCondition(inventoryDumperConfiguration)))
                    .executeQuery();
            resultSet.next();
            estimatedRows = resultSet.getInt(1);
        } catch (final SQLException ex) {
            throw new SyncTaskExecuteException("get estimated rows error.", ex);
        }
    }
    
    private void instanceDumper() {
        dumper = DumperFactory.newInstanceJdbcDumper(inventoryDumperConfiguration, dataSourceManager);
    }
    
    private void instanceChannel(final Importer importer) {
        MemoryChannel channel = new MemoryChannel(records -> {
            int count = 0;
            for (Record record : records) {
                if (record instanceof DataRecord) {
                    count++;
                } else if (record instanceof FinishedRecord && record.getPosition() instanceof InventoryPosition) {
                    getPositionManager().setPosition((InventoryPosition) record.getPosition());
                }
            }
            syncedRows.addAndGet(count);
        });
        dumper.setChannel(channel);
        importer.setChannel(channel);
    }
    
    private void waitForResult(final Future<?> future) {
        try {
            future.get();
        } catch (final InterruptedException ignored) {
        } catch (final ExecutionException ex) {
            throw new SyncTaskExecuteException(String.format("Task %s execute failed ", getTaskId()), ex.getCause());
        }
    }
    
    @Override
    public void stop() {
        if (null != dumper) {
            dumper.stop();
            dumper = null;
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        return new InventoryDataSyncTaskProgress(getTaskId(), estimatedRows, syncedRows.get());
    }
}
