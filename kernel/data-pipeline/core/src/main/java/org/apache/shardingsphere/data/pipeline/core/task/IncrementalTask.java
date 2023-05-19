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
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.importer.ImporterType;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreator;
import org.apache.shardingsphere.data.pipeline.spi.importer.connector.ImporterConnector;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Incremental task.
 */
@Slf4j
@ToString(exclude = {"incrementalExecuteEngine", "channel", "dumper", "importer", "taskProgress"})
public final class IncrementalTask implements PipelineTask, AutoCloseable {
    
    @Getter
    private final String taskId;
    
    private final ExecuteEngine incrementalExecuteEngine;
    
    private final PipelineChannel channel;
    
    private final Dumper dumper;
    
    private final Importer importer;
    
    @Getter
    private final IncrementalTaskProgress taskProgress;
    
    // TODO simplify parameters
    public IncrementalTask(final DumperConfiguration dumperConfig, final ImporterConfiguration importerConfig,
                           final PipelineChannelCreator pipelineChannelCreator, final ImporterConnector importerConnector,
                           final PipelineTableMetaDataLoader sourceMetaDataLoader, final ExecuteEngine incrementalExecuteEngine,
                           final InventoryIncrementalJobItemContext jobItemContext) {
        taskId = dumperConfig.getDataSourceName();
        this.incrementalExecuteEngine = incrementalExecuteEngine;
        IngestPosition position = dumperConfig.getPosition();
        taskProgress = createIncrementalTaskProgress(position, jobItemContext.getInitProgress());
        channel = createChannel(pipelineChannelCreator, taskProgress);
        dumper = PipelineTypedSPILoader.getDatabaseTypedService(
                IncrementalDumperCreator.class, dumperConfig.getDataSourceConfig().getDatabaseType().getType()).createIncrementalDumper(dumperConfig, position, channel, sourceMetaDataLoader);
        importer = TypedSPILoader.getService(ImporterCreator.class, importerConnector.getType()).createImporter(importerConfig, importerConnector, channel, jobItemContext,
                ImporterType.INCREMENTAL);
    }
    
    private IncrementalTaskProgress createIncrementalTaskProgress(final IngestPosition position, final InventoryIncrementalJobItemProgress jobItemProgress) {
        IncrementalTaskProgress result = new IncrementalTaskProgress(position);
        if (null != jobItemProgress && null != jobItemProgress.getIncremental()) {
            Optional.ofNullable(jobItemProgress.getIncremental().getIncrementalTaskProgress())
                    .ifPresent(optional -> result.setIncrementalTaskDelay(jobItemProgress.getIncremental().getIncrementalTaskProgress().getIncrementalTaskDelay()));
        }
        return result;
    }
    
    private PipelineChannel createChannel(final PipelineChannelCreator pipelineChannelCreator, final IncrementalTaskProgress progress) {
        return pipelineChannelCreator.createPipelineChannel(1, records -> {
            Record lastHandledRecord = records.get(records.size() - 1);
            progress.setPosition(lastHandledRecord.getPosition());
            progress.getIncrementalTaskDelay().setLastEventTimestamps(lastHandledRecord.getCommitTime());
        });
    }
    
    @Override
    public Collection<CompletableFuture<?>> start() {
        taskProgress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
        Collection<CompletableFuture<?>> result = new LinkedList<>();
        result.add(incrementalExecuteEngine.submit(dumper, new JobExecuteCallback(taskId, "incremental dumper")));
        result.add(incrementalExecuteEngine.submit(importer, new JobExecuteCallback(taskId, "incremental importer")));
        return result;
    }
    
    @Override
    public void stop() {
        dumper.stop();
        importer.stop();
    }
    
    @Override
    public void close() {
        channel.close();
    }
    
    @RequiredArgsConstructor
    private class JobExecuteCallback implements ExecuteCallback {
        
        private final String taskId;
        
        private final String jobType;
        
        @Override
        public void onSuccess() {
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            log.error("{} on failure, task ID={}", jobType, taskId);
            IncrementalTask.this.stop();
            IncrementalTask.this.close();
        }
    }
}
