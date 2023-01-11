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
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreator;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterType;
import org.apache.shardingsphere.data.pipeline.spi.importer.connector.ImporterConnector;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Inventory task.
 */
@ToString(exclude = {"inventoryDumperExecuteEngine", "inventoryImporterExecuteEngine", "channel", "dumper", "importer"})
@Slf4j
public final class InventoryTask implements PipelineTask, AutoCloseable {
    
    @Getter
    private final String taskId;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine inventoryImporterExecuteEngine;
    
    private final PipelineChannel channel;
    
    private final Dumper dumper;
    
    private final Importer importer;
    
    private volatile IngestPosition<?> position;
    
    public InventoryTask(final InventoryDumperConfiguration inventoryDumperConfig, final ImporterConfiguration importerConfig,
                         final PipelineChannelCreator pipelineChannelCreator, final ImporterConnector importerConnector,
                         final DataSource sourceDataSource, final PipelineTableMetaDataLoader sourceMetaDataLoader,
                         final ExecuteEngine inventoryDumperExecuteEngine, final ExecuteEngine inventoryImporterExecuteEngine,
                         final PipelineJobProgressListener jobProgressListener) {
        taskId = generateTaskId(inventoryDumperConfig);
        this.inventoryDumperExecuteEngine = inventoryDumperExecuteEngine;
        this.inventoryImporterExecuteEngine = inventoryImporterExecuteEngine;
        channel = createChannel(pipelineChannelCreator);
        dumper = new InventoryDumper(inventoryDumperConfig, channel, sourceDataSource, sourceMetaDataLoader);
        importer = TypedSPIRegistry.getRegisteredService(ImporterCreator.class, importerConnector.getType()).createImporter(importerConfig, importerConnector, channel, jobProgressListener,
                ImporterType.INVENTORY);
        position = inventoryDumperConfig.getPosition();
    }
    
    private String generateTaskId(final InventoryDumperConfiguration inventoryDumperConfig) {
        String result = String.format("%s.%s", inventoryDumperConfig.getDataSourceName(), inventoryDumperConfig.getActualTableName());
        return null == inventoryDumperConfig.getShardingItem() ? result : result + "#" + inventoryDumperConfig.getShardingItem();
    }
    
    @Override
    public Collection<CompletableFuture<?>> start() {
        Collection<CompletableFuture<?>> result = new LinkedList<>();
        result.add(inventoryDumperExecuteEngine.submit(dumper, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("dumper onFailure, taskId={}", taskId, throwable);
                stop();
                close();
            }
        }));
        result.add(inventoryImporterExecuteEngine.submit(importer, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("importer onFailure, taskId={}", taskId, throwable);
                stop();
                close();
            }
        }));
        return result;
    }
    
    private PipelineChannel createChannel(final PipelineChannelCreator pipelineChannelCreator) {
        return pipelineChannelCreator.createPipelineChannel(1, records -> {
            Record lastNormalRecord = RecordUtil.getLastNormalRecord(records);
            if (null != lastNormalRecord) {
                position = lastNormalRecord.getPosition();
            }
        });
    }
    
    @Override
    public void stop() {
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
