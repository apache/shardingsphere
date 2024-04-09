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

package org.apache.shardingsphere.data.pipeline.cdc.core.prepare;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJobType;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCChannelProgressPair;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporter;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCIncrementalTask;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCInventoryTask;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.DialectIncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.incremental.IncrementalTaskPositionManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CDC job preparer.
 */
@Slf4j
public final class CDCJobPreparer {
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(new CDCJobType().getYamlJobItemProgressSwapper());
    
    /**
     * Do prepare work.
     *
     * @param jobItemContexts job item contexts
     */
    public void initTasks(final Collection<CDCJobItemContext> jobItemContexts) {
        // TODO Use pipeline tree to build it
        AtomicBoolean inventoryImporterUsed = new AtomicBoolean();
        List<CDCChannelProgressPair> inventoryChannelProgressPairs = new CopyOnWriteArrayList<>();
        AtomicBoolean incrementalImporterUsed = new AtomicBoolean();
        List<CDCChannelProgressPair> incrementalChannelProgressPairs = new CopyOnWriteArrayList<>();
        for (CDCJobItemContext each : jobItemContexts) {
            initTasks0(each, inventoryImporterUsed, inventoryChannelProgressPairs, incrementalImporterUsed, incrementalChannelProgressPairs);
        }
    }
    
    private void initTasks0(final CDCJobItemContext jobItemContext, final AtomicBoolean inventoryImporterUsed, final List<CDCChannelProgressPair> inventoryChannelProgressPairs,
                            final AtomicBoolean incrementalImporterUsed, final List<CDCChannelProgressPair> incrementalChannelProgressPairs) {
        Optional<TransmissionJobItemProgress> jobItemProgress = jobItemManager.getProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        if (!jobItemProgress.isPresent()) {
            jobItemManager.persistProgress(jobItemContext);
        }
        if (jobItemContext.isStopping()) {
            PipelineJobRegistry.stop(jobItemContext.getJobId());
            return;
        }
        initIncrementalPosition(jobItemContext);
        if (jobItemContext.getJobConfig().isFull()) {
            initInventoryTasks(jobItemContext, inventoryImporterUsed, inventoryChannelProgressPairs);
        }
        initIncrementalTask(jobItemContext, incrementalImporterUsed, incrementalChannelProgressPairs);
    }
    
    private void initIncrementalPosition(final CDCJobItemContext jobItemContext) {
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        JobItemIncrementalTasksProgress initIncremental = null == jobItemContext.getInitProgress() ? null : jobItemContext.getInitProgress().getIncremental();
        try {
            DatabaseType databaseType = taskConfig.getDumperContext().getCommonContext().getDataSourceConfig().getDatabaseType();
            IngestPosition position = new IncrementalTaskPositionManager(databaseType).getPosition(initIncremental, taskConfig.getDumperContext(), jobItemContext.getDataSourceManager());
            taskConfig.getDumperContext().getCommonContext().setPosition(position);
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobItemContext.getJobId(), ex);
        }
    }
    
    private void initInventoryTasks(final CDCJobItemContext jobItemContext, final AtomicBoolean importerUsed, final List<CDCChannelProgressPair> channelProgressPairs) {
        long startTimeMillis = System.currentTimeMillis();
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        ImporterConfiguration importerConfig = taskConfig.getImporterConfig();
        TransmissionProcessContext processContext = jobItemContext.getJobProcessContext();
        for (InventoryDumperContext each : new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), new InventoryDumperContext(taskConfig.getDumperContext().getCommonContext()), importerConfig)
                .splitInventoryDumperContext(jobItemContext)) {
            AtomicReference<IngestPosition> position = new AtomicReference<>(each.getCommonContext().getPosition());
            PipelineChannel channel = PipelineTaskUtils.createInventoryChannel(processContext.getProcessConfig().getStreamChannel(), importerConfig.getBatchSize(), position);
            if (!(position.get() instanceof IngestFinishedPosition)) {
                channelProgressPairs.add(new CDCChannelProgressPair(channel, jobItemContext));
            }
            Dumper dumper = new InventoryDumper(each, channel, jobItemContext.getSourceDataSource(), jobItemContext.getSourceMetaDataLoader());
            Importer importer = importerUsed.get() ? null
                    : new CDCImporter(channelProgressPairs, importerConfig.getBatchSize(), 100L, jobItemContext.getSink(), false, importerConfig.getRateLimitAlgorithm());
            jobItemContext.getInventoryTasks().add(new CDCInventoryTask(PipelineTaskUtils.generateInventoryTaskId(each), processContext.getInventoryDumperExecuteEngine(),
                    processContext.getInventoryImporterExecuteEngine(), dumper, importer, position));
            if (!(position.get() instanceof IngestFinishedPosition)) {
                importerUsed.set(true);
            }
        }
        log.info("initInventoryTasks cost {} ms", System.currentTimeMillis() - startTimeMillis);
    }
    
    private void initIncrementalTask(final CDCJobItemContext jobItemContext, final AtomicBoolean importerUsed, final List<CDCChannelProgressPair> channelProgressPairs) {
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        IncrementalDumperContext dumperContext = taskConfig.getDumperContext();
        IncrementalTaskProgress taskProgress = PipelineTaskUtils.createIncrementalTaskProgress(dumperContext.getCommonContext().getPosition(), jobItemContext.getInitProgress());
        PipelineChannel channel = PipelineTaskUtils.createIncrementalChannel(jobItemContext.getJobProcessContext().getProcessConfig().getStreamChannel(), taskProgress);
        channelProgressPairs.add(new CDCChannelProgressPair(channel, jobItemContext));
        Dumper dumper = DatabaseTypedSPILoader.getService(DialectIncrementalDumperCreator.class, dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType())
                .createIncrementalDumper(dumperContext, dumperContext.getCommonContext().getPosition(), channel, jobItemContext.getSourceMetaDataLoader());
        boolean needSorting = jobItemContext.getJobConfig().isDecodeWithTX();
        Importer importer = importerUsed.get() ? null
                : new CDCImporter(channelProgressPairs, 1, 100L, jobItemContext.getSink(), needSorting, taskConfig.getImporterConfig().getRateLimitAlgorithm());
        PipelineTask incrementalTask = new CDCIncrementalTask(
                dumperContext.getCommonContext().getDataSourceName(), jobItemContext.getJobProcessContext().getIncrementalExecuteEngine(), dumper, importer, taskProgress);
        jobItemContext.getIncrementalTasks().add(incrementalTask);
        importerUsed.set(true);
    }
}
