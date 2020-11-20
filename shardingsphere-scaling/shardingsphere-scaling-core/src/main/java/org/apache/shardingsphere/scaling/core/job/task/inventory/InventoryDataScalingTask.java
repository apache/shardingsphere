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
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Table slice execute task.
 */
@Slf4j
public final class InventoryDataScalingTask extends AbstractShardingScalingExecutor implements ScalingTask {
    
    private final InventoryDumperConfiguration inventoryDumperConfig;
    
    private final ImporterConfiguration importerConfig;
    
    private final DataSourceManager dataSourceManager;
    
    private Dumper dumper;
    
    public InventoryDataScalingTask(final InventoryDumperConfiguration inventoryDumperConfig, final ImporterConfiguration importerConfig) {
        this(inventoryDumperConfig, importerConfig, new DataSourceManager());
    }
    
    public InventoryDataScalingTask(final InventoryDumperConfiguration inventoryDumperConfig, final ImporterConfiguration importerConfig, final DataSourceManager dataSourceManager) {
        this.inventoryDumperConfig = inventoryDumperConfig;
        this.importerConfig = importerConfig;
        this.dataSourceManager = dataSourceManager;
        setTaskId(generateSyncTaskId(inventoryDumperConfig));
        setPositionManager(inventoryDumperConfig.getPositionManager());
    }
    
    private String generateSyncTaskId(final InventoryDumperConfiguration inventoryDumperConfig) {
        String result = String.format("%s.%s", inventoryDumperConfig.getDataSourceName(), inventoryDumperConfig.getTableName());
        return null == inventoryDumperConfig.getSpiltNum() ? result : result + "#" + inventoryDumperConfig.getSpiltNum();
    }
    
    @Override
    public void start() {
        instanceDumper();
        Importer importer = ImporterFactory.newInstance(importerConfig, dataSourceManager);
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
    
    private void instanceDumper() {
        dumper = DumperFactory.newInstanceJdbcDumper(inventoryDumperConfig, dataSourceManager);
    }
    
    private void instanceChannel(final Importer importer) {
        MemoryChannel channel = new MemoryChannel(records -> {
            Optional<Record> record = records.stream().filter(each -> !(each.getPosition() instanceof PlaceholderPosition)).reduce((a, b) -> b);
            record.ifPresent(value -> getPositionManager().setPosition(value.getPosition()));
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
        return new InventoryDataSyncTaskProgress(getTaskId(), getPositionManager().getPosition() instanceof FinishedPosition);
    }
}
