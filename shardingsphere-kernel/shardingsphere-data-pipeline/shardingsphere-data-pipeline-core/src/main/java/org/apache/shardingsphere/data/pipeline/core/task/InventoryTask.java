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

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobExecutionException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreatorFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.InventoryDumperCreatorFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Inventory task.
 */
@Slf4j
@ToString(exclude = {"importerExecuteEngine", "channel", "dumper", "importer"})
public final class InventoryTask extends AbstractLifecycleExecutor implements PipelineTask, AutoCloseable {
    
    @Getter
    private final String taskId;
    
    private final ExecuteEngine importerExecuteEngine;
    
    private final PipelineChannel channel;
    
    private final Dumper dumper;
    
    private final Importer importer;
    
    private volatile IngestPosition<?> position;
    
    public InventoryTask(final InventoryDumperConfiguration inventoryDumperConfig, final ImporterConfiguration importerConfig,
                         final PipelineChannelCreator pipelineChannelCreator, final PipelineDataSourceManager dataSourceManager,
                         final DataSource sourceDataSource, final PipelineTableMetaDataLoader sourceMetaDataLoader,
                         final ExecuteEngine importerExecuteEngine, final PipelineJobProgressListener jobProgressListener) {
        this.importerExecuteEngine = importerExecuteEngine;
        taskId = generateTaskId(inventoryDumperConfig);
        channel = createChannel(pipelineChannelCreator);
        dumper = InventoryDumperCreatorFactory.getInstance(inventoryDumperConfig.getDataSourceConfig().getDatabaseType().getType())
                .createInventoryDumper(inventoryDumperConfig, channel, sourceDataSource, sourceMetaDataLoader);
        importer = ImporterCreatorFactory.getInstance(importerConfig.getDataSourceConfig().getDatabaseType().getType()).createImporter(importerConfig, dataSourceManager, channel, jobProgressListener);
        position = inventoryDumperConfig.getPosition();
    }
    
    private String generateTaskId(final InventoryDumperConfiguration inventoryDumperConfig) {
        String result = String.format("%s.%s", inventoryDumperConfig.getDataSourceName(), inventoryDumperConfig.getActualTableName());
        return null == inventoryDumperConfig.getShardingItem() ? result : result + "#" + inventoryDumperConfig.getShardingItem();
    }
    
    @Override
    protected void doStart() {
        Future<?> future = importerExecuteEngine.submit(importer, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                log.info("importer onSuccess, taskId={}", taskId);
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("importer onFailure, taskId={}", taskId, throwable);
                stop();
            }
        });
        dumper.start();
        waitForResult(future);
        log.info("importer future done");
    }
    
    private PipelineChannel createChannel(final PipelineChannelCreator pipelineChannelCreator) {
        return pipelineChannelCreator.createPipelineChannel(1, records -> {
            Record lastNormalRecord = getLastNormalRecord(records);
            if (null != lastNormalRecord) {
                position = lastNormalRecord.getPosition();
            }
        });
    }
    
    private Record getLastNormalRecord(final List<Record> records) {
        for (int index = records.size() - 1; index >= 0; index--) {
            Record record = records.get(index);
            if (record.getPosition() instanceof PlaceholderPosition) {
                continue;
            }
            return record;
        }
        return null;
    }
    
    private void waitForResult(final Future<?> future) {
        try {
            future.get();
        } catch (final InterruptedException ignored) {
        } catch (final ExecutionException ex) {
            throw new PipelineJobExecutionException(String.format("Task %s execute failed ", taskId), ex.getCause());
        }
    }
    
    @Override
    protected void doStop() {
        dumper.stop();
        importer.stop();
    }
    
    @Override
    public InventoryTaskProgress getTaskProgress() {
        return new InventoryTaskProgress(position);
    }
    
    @Override
    public void close() {
        channel.close();
    }
}
