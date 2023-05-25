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
import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.importer.ImporterType;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.cdc.context.job.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.prepare.PipelineJobPreparerUtils;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreator;
import org.apache.shardingsphere.data.pipeline.spi.importer.connector.PipelineSink;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CDC job preparer.
 */
@Slf4j
public final class CDCJobPreparer {
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
    /**
     * Do prepare work.
     *
     * @param jobItemContext job item context
     */
    public void initTasks(final CDCJobItemContext jobItemContext) {
        Optional<InventoryIncrementalJobItemProgress> jobItemProgress = jobAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        if (!jobItemProgress.isPresent()) {
            jobAPI.persistJobItemProgress(jobItemContext);
        }
        if (jobItemContext.isStopping()) {
            PipelineJobCenter.stop(jobItemContext.getJobId());
            return;
        }
        prepareIncremental(jobItemContext);
        if (jobItemContext.getJobConfig().isFull()) {
            initInventoryTasks(jobItemContext);
        }
        initIncrementalTasks(jobItemContext);
    }
    
    private void prepareIncremental(final CDCJobItemContext jobItemContext) {
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        JobItemIncrementalTasksProgress initIncremental = null == jobItemContext.getInitProgress() ? null : jobItemContext.getInitProgress().getIncremental();
        try {
            taskConfig.getDumperConfig().setPosition(PipelineJobPreparerUtils.getIncrementalPosition(initIncremental, taskConfig.getDumperConfig(), jobItemContext.getDataSourceManager()));
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobItemContext.getJobId(), ex);
        }
    }
    
    private void initInventoryTasks(final CDCJobItemContext jobItemContext) {
        jobItemContext.getInventoryTasks().addAll(splitInventoryData(jobItemContext));
    }
    
    private List<PipelineTask> splitInventoryData(final CDCJobItemContext jobItemContext) {
        List<PipelineTask> result = new LinkedList<>();
        long startTimeMillis = System.currentTimeMillis();
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        ImporterConfiguration importerConfig = taskConfig.getImporterConfig();
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), new InventoryDumperConfiguration(taskConfig.getDumperConfig()), importerConfig);
        CDCProcessContext processContext = jobItemContext.getJobProcessContext();
        DataSource sourceDataSource = jobItemContext.getSourceDataSource();
        for (InventoryDumperConfiguration each : inventoryTaskSplitter.splitInventoryDumperConfig(jobItemContext)) {
            AtomicReference<IngestPosition> position = new AtomicReference<>(each.getPosition());
            PipelineChannel channel = PipelineTaskUtils.createInventoryChannel(processContext.getPipelineChannelCreator(), importerConfig.getBatchSize(), position);
            Dumper dumper = new InventoryDumper(each, channel, sourceDataSource, jobItemContext.getSourceMetaDataLoader());
            // TODO now Init importer
            // TypedSPILoader.getService(ImporterCreator.class, importerConnector.getType()).createImporter(importerConfig, channel, jobProgressListener, ImporterType.INVENTORY);
            Importer importer = null;
            result.add(new InventoryTask(PipelineTaskUtils.generateInventoryTaskId(each), processContext.getInventoryDumperExecuteEngine(),
                    processContext.getInventoryImporterExecuteEngine(), channel, dumper, importer, position));
        }
        log.info("splitInventoryData cost {} ms", System.currentTimeMillis() - startTimeMillis);
        return result;
    }
    
    private void initIncrementalTasks(final CDCJobItemContext jobItemContext) {
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        PipelineChannelCreator pipelineChannelCreator = jobItemContext.getJobProcessContext().getPipelineChannelCreator();
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobItemContext.getSourceMetaDataLoader();
        ExecuteEngine incrementalExecuteEngine = jobItemContext.getJobProcessContext().getIncrementalExecuteEngine();
        DumperConfiguration dumperConfig = taskConfig.getDumperConfig();
        ImporterConfiguration importerConfig = taskConfig.getImporterConfig();
        IncrementalTaskProgress taskProgress = PipelineTaskUtils.createIncrementalTaskProgress(dumperConfig.getPosition(), jobItemContext.getInitProgress());
        PipelineChannel channel = PipelineTaskUtils.createIncrementalChannel(importerConfig.getConcurrency(), pipelineChannelCreator, taskProgress);
        Dumper dumper = PipelineTypedSPILoader.getDatabaseTypedService(IncrementalDumperCreator.class, dumperConfig.getDataSourceConfig().getDatabaseType().getType())
                .createIncrementalDumper(dumperConfig, dumperConfig.getPosition(), channel, sourceMetaDataLoader);
        // TODO now Remove importerConnector
        Collection<Importer> importers = createImporters(importerConfig, jobItemContext.getPipelineSink(), channel, jobItemContext);
        PipelineTask incrementalTask = new IncrementalTask(dumperConfig.getDataSourceName(), incrementalExecuteEngine, channel, dumper, importers, taskProgress);
        jobItemContext.getIncrementalTasks().add(incrementalTask);
    }
    
    private Collection<Importer> createImporters(final ImporterConfiguration importerConfig, final PipelineSink pipelineSink, final PipelineChannel channel,
                                                 final PipelineJobProgressListener jobProgressListener) {
        Collection<Importer> result = new LinkedList<>();
        for (int i = 0; i < importerConfig.getConcurrency(); i++) {
            result.add(TypedSPILoader.getService(ImporterCreator.class, pipelineSink.getType()).createImporter(importerConfig, pipelineSink, channel, jobProgressListener,
                    ImporterType.INCREMENTAL));
        }
        return result;
    }
}
