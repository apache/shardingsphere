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
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobExecutionException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.IncrementalDumperCreatorFactory;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreatorFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.Dumper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Incremental task.
 */
@Slf4j
@ToString(exclude = {"incrementalDumperExecuteEngine", "channel", "dumper", "importers", "taskProgress"})
public final class IncrementalTask extends AbstractLifecycleExecutor implements PipelineTask, AutoCloseable {
    
    @Getter
    private final String taskId;
    
    private final ExecuteEngine incrementalDumperExecuteEngine;
    
    private final PipelineChannel channel;
    
    private final Dumper dumper;
    
    private final Collection<Importer> importers;
    
    @Getter
    private final IncrementalTaskProgress taskProgress;
    
    public IncrementalTask(final int concurrency, final DumperConfiguration dumperConfig, final ImporterConfiguration importerConfig,
                           final PipelineChannelCreator pipelineChannelCreator, final PipelineDataSourceManager dataSourceManager,
                           final PipelineTableMetaDataLoader sourceMetaDataLoader, final ExecuteEngine incrementalDumperExecuteEngine,
                           final PipelineJobProgressListener jobProgressListener) {
        this.incrementalDumperExecuteEngine = incrementalDumperExecuteEngine;
        taskId = dumperConfig.getDataSourceName();
        IngestPosition<?> position = dumperConfig.getPosition();
        taskProgress = createIncrementalTaskProgress(position);
        channel = createChannel(concurrency, pipelineChannelCreator, taskProgress);
        dumper = IncrementalDumperCreatorFactory.getInstance(dumperConfig.getDataSourceConfig().getDatabaseType().getType()).createIncrementalDumper(dumperConfig, position, channel,
                sourceMetaDataLoader);
        importers = createImporters(concurrency, importerConfig, dataSourceManager, channel, jobProgressListener);
    }
    
    private IncrementalTaskProgress createIncrementalTaskProgress(final IngestPosition<?> position) {
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress();
        incrementalTaskProgress.setPosition(position);
        return incrementalTaskProgress;
    }
    
    @Override
    protected void doStart() {
        taskProgress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
        Future<?> future = incrementalDumperExecuteEngine.submitAll(importers, getExecuteCallback());
        dumper.start();
        waitForResult(future);
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
    
    private ExecuteCallback getExecuteCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                log.info("importer onSuccess, taskId={}", taskId);
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("importer onFailure, taskId={}", taskId, throwable);
                stop();
            }
        };
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
        for (Importer each : importers) {
            each.stop();
        }
    }
    
    @Override
    public void close() {
        channel.close();
    }
}
