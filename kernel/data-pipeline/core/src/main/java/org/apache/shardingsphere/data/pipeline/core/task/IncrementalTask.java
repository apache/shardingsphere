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
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreatorFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumperCreatorFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Incremental task.
 */
@Slf4j
@ToString(exclude = {"incrementalExecuteEngine", "channel", "dumper", "importers", "taskProgress"})
public final class IncrementalTask implements PipelineTask, AutoCloseable {
    
    @Getter
    private final String taskId;
    
    private final ExecuteEngine incrementalExecuteEngine;
    
    private final PipelineChannel channel;
    
    private final Dumper dumper;
    
    private final Collection<Importer> importers;
    
    @Getter
    private final IncrementalTaskProgress taskProgress;
    
    // TODO simplify parameters
    public IncrementalTask(final int concurrency, final DumperConfiguration dumperConfig, final ImporterConfiguration importerConfig,
                           final PipelineChannelCreator pipelineChannelCreator, final PipelineDataSourceManager dataSourceManager,
                           final PipelineTableMetaDataLoader sourceMetaDataLoader, final ExecuteEngine incrementalExecuteEngine,
                           final InventoryIncrementalJobItemContext jobItemContext) {
        taskId = dumperConfig.getDataSourceName();
        this.incrementalExecuteEngine = incrementalExecuteEngine;
        IngestPosition<?> position = dumperConfig.getPosition();
        taskProgress = createIncrementalTaskProgress(position, jobItemContext.getInitProgress());
        channel = createChannel(concurrency, pipelineChannelCreator, taskProgress);
        dumper = IncrementalDumperCreatorFactory.getInstance(dumperConfig.getDataSourceConfig().getDatabaseType().getType()).createIncrementalDumper(dumperConfig, position, channel,
                sourceMetaDataLoader);
        importers = createImporters(concurrency, importerConfig, dataSourceManager, channel, jobItemContext);
    }
    
    private IncrementalTaskProgress createIncrementalTaskProgress(final IngestPosition<?> position, final InventoryIncrementalJobItemProgress jobItemProgress) {
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress();
        incrementalTaskProgress.setPosition(position);
        if (null != jobItemProgress && null != jobItemProgress.getIncremental()) {
            Optional.ofNullable(jobItemProgress.getIncremental().getIncrementalTaskProgress())
                    .ifPresent(optional -> incrementalTaskProgress.setIncrementalTaskDelay(jobItemProgress.getIncremental().getIncrementalTaskProgress().getIncrementalTaskDelay()));
        }
        return incrementalTaskProgress;
    }
    
    private Collection<Importer> createImporters(final int concurrency, final ImporterConfiguration importerConfig, final PipelineDataSourceManager dataSourceManager, final PipelineChannel channel,
                                                 final PipelineJobProgressListener jobProgressListener) {
        Collection<Importer> result = new LinkedList<>();
        for (int i = 0; i < concurrency; i++) {
            result.add(ImporterCreatorFactory.getInstance(importerConfig.getDataSourceConfig().getDatabaseType().getType()).createImporter(importerConfig, dataSourceManager, channel,
                    jobProgressListener));
        }
        return result;
    }
    
    private PipelineChannel createChannel(final int concurrency, final PipelineChannelCreator pipelineChannelCreator, final IncrementalTaskProgress progress) {
        return pipelineChannelCreator.createPipelineChannel(concurrency, records -> {
            Record lastHandledRecord = records.get(records.size() - 1);
            if (!(lastHandledRecord.getPosition() instanceof PlaceholderPosition)) {
                progress.setPosition(lastHandledRecord.getPosition());
                progress.getIncrementalTaskDelay().setLastEventTimestamps(lastHandledRecord.getCommitTime());
            }
            progress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
        });
    }
    
    @Override
    public CompletableFuture<?> start() {
        taskProgress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
        CompletableFuture<?> dumperFuture = incrementalExecuteEngine.submit(dumper, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                log.info("incremental dumper onSuccess, taskId={}", taskId);
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("incremental dumper onFailure, taskId={}", taskId);
                stop();
                close();
            }
        });
        CompletableFuture<?> importerFuture = incrementalExecuteEngine.submitAll(importers, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                log.info("importer onSuccess, taskId={}", taskId);
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("importer onFailure, taskId={}", taskId, throwable);
                stop();
                close();
            }
        });
        return CompletableFuture.allOf(dumperFuture, importerFuture);
    }
    
    @Override
    public void stop() {
        dumper.stop();
        for (Importer each : importers) {
            each.stop();
        }
    }
    
    @Override
    public void close() {
        channel.close();
    }
}
