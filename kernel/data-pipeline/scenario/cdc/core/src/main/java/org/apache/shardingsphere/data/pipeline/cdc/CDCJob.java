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
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.PipelineCDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.core.prepare.CDCJobPreparer;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCTasksRunner;
import org.apache.shardingsphere.data.pipeline.cdc.engine.CDCJobRunnerCleaner;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.PipelineRequiredColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.engine.PipelineJobRunnerManager;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * CDC job.
 */
@Slf4j
public final class CDCJob implements PipelineJob {
    
    @Getter
    private final PipelineJobRunnerManager jobRunnerManager;
    
    private final CDCJobAPI jobAPI = (CDCJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING");
    
    @Getter
    private final PipelineSink sink;
    
    public CDCJob(final PipelineSink sink) {
        jobRunnerManager = new PipelineJobRunnerManager(new CDCJobRunnerCleaner(sink));
        this.sink = sink;
    }
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        log.info("Execute job {}", jobId);
        PipelineJobType<?> jobType = PipelineJobIdUtils.parseJobType(jobId);
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(jobId);
        CDCJobConfiguration jobConfig = (CDCJobConfiguration) jobType.getOption().getYamlJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper());
        TransmissionProcessContext jobProcessContext = new TransmissionProcessContext(
                jobId, PipelineProcessConfigurationUtils.fillInDefaultValue(new PipelineProcessConfigurationPersistService().load(contextKey, jobType.getType())));
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(contextKey);
        Collection<CDCJobItemContext> jobItemContexts = new LinkedList<>();
        for (int shardingItem = 0; shardingItem < jobConfig.getJobShardingCount(); shardingItem++) {
            if (jobRunnerManager.isStopping()) {
                log.info("Job is stopping, ignore.");
                return;
            }
            TransmissionJobItemProgress jobItemProgress = jobItemManager.getProgress(shardingContext.getJobName(), shardingItem).orElse(null);
            CDCTaskConfiguration taskConfig = buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getProcessConfiguration());
            CDCJobItemContext jobItemContext = new CDCJobItemContext(jobConfig, shardingItem, jobItemProgress, jobProcessContext, taskConfig, getJobRunnerManager().getDataSourceManager(), sink);
            if (!jobRunnerManager.addTasksRunner(shardingItem, new CDCTasksRunner(jobItemContext))) {
                continue;
            }
            jobItemContexts.add(jobItemContext);
            governanceFacade.getJobItemFacade().getErrorMessage().clean(jobId, shardingItem);
            log.info("Start tasks runner, jobId={}, shardingItem={}.", jobId, shardingItem);
        }
        if (jobItemContexts.isEmpty()) {
            log.warn("Job item contexts are empty, ignore.");
            return;
        }
        initTasks(jobItemContexts, governanceFacade, jobItemManager);
        executeInventoryTasks(jobItemContexts, jobItemManager);
        executeIncrementalTasks(jobItemContexts, jobItemManager);
    }
    
    private CDCTaskConfiguration buildTaskConfiguration(final CDCJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        TableAndSchemaNameMapper mapper = new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames());
        IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, jobShardingItem, mapper);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, processConfig, mapper);
        return new CDCTaskConfiguration(dumperContext, importerConfig);
    }
    
    private IncrementalDumperContext buildDumperContext(final CDCJobConfiguration jobConfig, final int jobShardingItem, final TableAndSchemaNameMapper mapper) {
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfig = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        DumperCommonContext dumperCommonContext = new DumperCommonContext(dataSourceName, actualDataSourceConfig, JobDataNodeLineConvertUtils.buildTableNameMapper(dataNodeLine), mapper);
        return new IncrementalDumperContext(dumperCommonContext, jobConfig.getJobId(), jobConfig.isDecodeWithTX());
    }
    
    private ImporterConfiguration buildImporterConfiguration(final CDCJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig,
                                                             final TableAndSchemaNameMapper mapper) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                jobConfig.getDataSourceConfig().getType(), jobConfig.getDataSourceConfig().getParameter());
        Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumnsMap = getTableAndRequiredColumnsMap(jobConfig);
        PipelineWriteConfiguration write = pipelineProcessConfig.getWrite();
        JobRateLimitAlgorithm writeRateLimitAlgorithm = null == write.getRateLimiter()
                ? null
                : TypedSPILoader.getService(JobRateLimitAlgorithm.class, write.getRateLimiter().getType(), write.getRateLimiter().getProps());
        return new ImporterConfiguration(dataSourceConfig, tableAndRequiredColumnsMap, mapper, write.getBatchSize(), writeRateLimitAlgorithm, 0, 1);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<ShardingSphereIdentifier, Collection<String>> getTableAndRequiredColumnsMap(final CDCJobConfiguration jobConfig) {
        Map<ShardingSphereIdentifier, Collection<String>> result = new HashMap<>();
        Collection<YamlRuleConfiguration> yamlRuleConfigs = jobConfig.getDataSourceConfig().getRootConfig().getRules();
        Collection<ShardingSphereIdentifier> targetTableNames = jobConfig.getSchemaTableNames().stream().map(ShardingSphereIdentifier::new).collect(Collectors.toSet());
        for (Entry<YamlRuleConfiguration, PipelineRequiredColumnsExtractor> entry : OrderedSPILoader.getServices(PipelineRequiredColumnsExtractor.class, yamlRuleConfigs).entrySet()) {
            result.putAll(entry.getValue().getTableAndRequiredColumnsMap(entry.getKey(), targetTableNames));
        }
        return result;
    }
    
    private void initTasks(final Collection<CDCJobItemContext> jobItemContexts,
                           final PipelineGovernanceFacade governanceFacade, final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager) {
        try {
            new CDCJobPreparer(jobItemManager).initTasks(jobItemContexts);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            for (PipelineJobItemContext each : jobItemContexts) {
                initTasksFailed(each.getJobId(), each.getShardingItem(), ex, governanceFacade);
            }
            throw ex;
        }
    }
    
    private void initTasksFailed(final String jobId, final int shardingItem, final Exception ex, final PipelineGovernanceFacade governanceFacade) {
        log.error("Job {}-{} execution failed.", jobId, shardingItem, ex);
        governanceFacade.getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
        PipelineJobRegistry.stop(jobId);
        jobAPI.disable(jobId);
    }
    
    private void executeInventoryTasks(final Collection<CDCJobItemContext> jobItemContexts, final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager) {
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (CDCJobItemContext each : jobItemContexts) {
            updateJobItemStatus(each, JobStatus.EXECUTE_INVENTORY_TASK, jobItemManager);
            for (PipelineTask task : each.getInventoryTasks()) {
                if (task.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        if (futures.isEmpty()) {
            return;
        }
        PipelineExecuteEngine.trigger(futures, new CDCExecuteCallback("inventory", jobItemContexts.iterator().next()));
    }
    
    private void executeIncrementalTasks(final Collection<CDCJobItemContext> jobItemContexts, final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager) {
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (CDCJobItemContext each : jobItemContexts) {
            updateJobItemStatus(each, JobStatus.EXECUTE_INCREMENTAL_TASK, jobItemManager);
            for (PipelineTask task : each.getIncrementalTasks()) {
                if (task.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        PipelineExecuteEngine.trigger(futures, new CDCExecuteCallback("incremental", jobItemContexts.iterator().next()));
    }
    
    private void updateJobItemStatus(final CDCJobItemContext jobItemContext, final JobStatus jobStatus, final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager) {
        jobItemContext.setStatus(jobStatus);
        jobItemManager.updateStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
    }
    
    @RequiredArgsConstructor
    private class CDCExecuteCallback implements ExecuteCallback {
        
        private final String identifier;
        
        private final CDCJobItemContext jobItemContext;
        
        @Override
        public void onSuccess() {
            if (jobItemContext.isStopping()) {
                log.info("Job is stopping, ignore.");
                return;
            }
            log.info("All {} tasks finished successful.", identifier);
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            log.error("Task {} execute failed.", identifier, throwable);
            String jobId = jobItemContext.getJobId();
            PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().update(jobId, jobItemContext.getShardingItem(), throwable);
            if (jobItemContext.getSink() instanceof PipelineCDCSocketSink) {
                PipelineCDCSocketSink cdcSink = (PipelineCDCSocketSink) jobItemContext.getSink();
                cdcSink.getChannel().writeAndFlush(CDCResponseUtils.failed("", "", Optional.ofNullable(throwable.getMessage()).orElse("")));
            }
            PipelineJobRegistry.stop(jobId);
            jobAPI.disable(jobId);
        }
    }
}
