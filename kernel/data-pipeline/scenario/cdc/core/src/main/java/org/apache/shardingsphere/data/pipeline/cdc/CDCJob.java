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

package org.apache.shardingsphere.data.pipeline.cdc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.CDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.core.prepare.CDCJobPreparer;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCTasksRunner;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.spi.algorithm.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * CDC job.
 */
@Slf4j
public final class CDCJob extends AbstractPipelineJob implements SimpleJob {
    
    @Getter
    private final PipelineSink sink;
    
    private final PipelineJobType jobType = TypedSPILoader.getService(PipelineJobType.class, "STREAMING");
    
    private final CDCJobAPI jobAPI = (CDCJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING");
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getYamlJobItemProgressSwapper());
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    private final CDCJobPreparer jobPreparer = new CDCJobPreparer();
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    public CDCJob(final String jobId, final PipelineSink sink) {
        super(jobId);
        this.sink = sink;
    }
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        log.info("Execute job {}", jobId);
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        List<CDCJobItemContext> jobItemContexts = new LinkedList<>();
        for (int shardingItem = 0; shardingItem < jobConfig.getJobShardingCount(); shardingItem++) {
            if (isStopping()) {
                log.info("stopping true, ignore");
                return;
            }
            CDCJobItemContext jobItemContext = buildCDCJobItemContext(jobConfig, shardingItem);
            if (!addTasksRunner(shardingItem, new CDCTasksRunner(jobItemContext))) {
                continue;
            }
            jobItemContexts.add(jobItemContext);
            PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().clean(jobId, shardingItem);
            log.info("start tasks runner, jobId={}, shardingItem={}", jobId, shardingItem);
        }
        if (jobItemContexts.isEmpty()) {
            log.warn("job item contexts empty, ignore");
            return;
        }
        prepare(jobItemContexts);
        executeInventoryTasks(jobItemContexts);
        executeIncrementalTasks(jobItemContexts);
    }
    
    private CDCJobItemContext buildCDCJobItemContext(final CDCJobConfiguration jobConfig, final int shardingItem) {
        Optional<TransmissionJobItemProgress> initProgress = jobItemManager.getProgress(jobConfig.getJobId(), shardingItem);
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.convertWithDefaultValue(
                processConfigPersistService.load(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()), jobType.getType()));
        TransmissionProcessContext jobProcessContext = new TransmissionProcessContext(jobConfig.getJobId(), processConfig);
        CDCTaskConfiguration taskConfig = buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        return new CDCJobItemContext(jobConfig, shardingItem, initProgress.orElse(null), jobProcessContext, taskConfig, dataSourceManager, sink);
    }
    
    private CDCTaskConfiguration buildTaskConfiguration(final CDCJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        TableAndSchemaNameMapper tableAndSchemaNameMapper = new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames());
        IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, jobShardingItem, tableAndSchemaNameMapper);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, processConfig, jobConfig.getSchemaTableNames(), tableAndSchemaNameMapper);
        CDCTaskConfiguration result = new CDCTaskConfiguration(dumperContext, importerConfig);
        log.debug("buildTaskConfiguration, result={}", result);
        return result;
    }
    
    private IncrementalDumperContext buildDumperContext(final CDCJobConfiguration jobConfig, final int jobShardingItem, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfig = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        return new IncrementalDumperContext(
                new DumperCommonContext(dataSourceName, actualDataSourceConfig, JobDataNodeLineConvertUtils.buildTableNameMapper(dataNodeLine), tableAndSchemaNameMapper),
                jobConfig.getJobId(), jobConfig.isDecodeWithTX());
    }
    
    private ImporterConfiguration buildImporterConfiguration(final CDCJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig, final Collection<String> schemaTableNames,
                                                             final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getDataSourceConfig().getType(),
                jobConfig.getDataSourceConfig().getParameter());
        TransmissionProcessContext processContext = new TransmissionProcessContext(jobConfig.getJobId(), pipelineProcessConfig);
        JobRateLimitAlgorithm writeRateLimitAlgorithm = processContext.getWriteRateLimitAlgorithm();
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor()
                .getShardingColumnsMap(jobConfig.getDataSourceConfig().getRootConfig().getRules(), schemaTableNames.stream().map(CaseInsensitiveIdentifier::new).collect(Collectors.toSet()));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, tableAndSchemaNameMapper, batchSize, writeRateLimitAlgorithm, 0, 1);
    }
    
    private void prepare(final Collection<CDCJobItemContext> jobItemContexts) {
        try {
            jobPreparer.initTasks(jobItemContexts);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            for (PipelineJobItemContext each : jobItemContexts) {
                processFailed(each.getJobId(), each.getShardingItem(), ex);
            }
            throw ex;
        }
    }
    
    private void processFailed(final String jobId, final int shardingItem, final Exception ex) {
        log.error("job execution failed, {}-{}", jobId, shardingItem, ex);
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
        PipelineJobRegistry.stop(jobId);
        jobAPI.disable(jobId);
    }
    
    private void executeInventoryTasks(final List<CDCJobItemContext> jobItemContexts) {
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (CDCJobItemContext each : jobItemContexts) {
            updateLocalAndRemoteJobItemStatus(each, JobStatus.EXECUTE_INVENTORY_TASK);
            for (PipelineTask task : each.getInventoryTasks()) {
                if (task.getTaskProgress().getPosition() instanceof FinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        if (futures.isEmpty()) {
            return;
        }
        ExecuteEngine.trigger(futures, new CDCExecuteCallback("inventory", jobItemContexts.get(0)));
    }
    
    private void updateLocalAndRemoteJobItemStatus(final PipelineJobItemContext jobItemContext, final JobStatus jobStatus) {
        jobItemContext.setStatus(jobStatus);
        jobItemManager.updateStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
    }
    
    private void executeIncrementalTasks(final List<CDCJobItemContext> jobItemContexts) {
        log.info("execute incremental tasks, jobId={}", getJobId());
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (CDCJobItemContext each : jobItemContexts) {
            if (JobStatus.EXECUTE_INCREMENTAL_TASK == each.getStatus()) {
                log.info("job status already EXECUTE_INCREMENTAL_TASK, ignore");
                return;
            }
            updateLocalAndRemoteJobItemStatus(each, JobStatus.EXECUTE_INCREMENTAL_TASK);
            for (PipelineTask task : each.getIncrementalTasks()) {
                if (task.getTaskProgress().getPosition() instanceof FinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        ExecuteEngine.trigger(futures, new CDCExecuteCallback("incremental", jobItemContexts.get(0)));
    }
    
    @Override
    protected void doPrepare(final PipelineJobItemContext jobItemContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void doClean() {
        dataSourceManager.close();
        QuietlyCloser.close(sink);
    }
    
    @RequiredArgsConstructor
    private final class CDCExecuteCallback implements ExecuteCallback {
        
        private final String identifier;
        
        private final CDCJobItemContext jobItemContext;
        
        @Override
        public void onSuccess() {
            if (jobItemContext.isStopping()) {
                log.info("onSuccess, stopping true, ignore");
                return;
            }
            log.info("onSuccess, all {} tasks finished.", identifier);
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            log.error("onFailure, {} task execute failed.", identifier, throwable);
            String jobId = jobItemContext.getJobId();
            PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().update(jobId, jobItemContext.getShardingItem(), throwable);
            if (jobItemContext.getSink() instanceof CDCSocketSink) {
                CDCSocketSink cdcSink = (CDCSocketSink) jobItemContext.getSink();
                cdcSink.getChannel().writeAndFlush(CDCResponseUtils.failed("", "", throwable.getMessage()));
            }
            PipelineJobRegistry.stop(jobId);
            jobAPI.disable(jobId);
        }
    }
}
