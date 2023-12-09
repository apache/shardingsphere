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
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.CDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.core.prepare.CDCJobPreparer;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCTasksRunner;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
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
import org.apache.shardingsphere.data.pipeline.core.job.AbstractInseparablePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.spi.algorithm.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * CDC job.
 */
@Slf4j
public final class CDCJob extends AbstractInseparablePipelineJob<CDCJobItemContext> implements SimpleJob {
    
    @Getter
    private final PipelineSink sink;
    
    private final CDCJobAPI jobAPI;
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager;
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService;
    
    private final PipelineDataSourceManager dataSourceManager;
    
    private final CDCJobPreparer jobPreparer;
    
    public CDCJob(final String jobId, final PipelineSink sink) {
        super(jobId);
        this.sink = sink;
        jobAPI = (CDCJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING");
        jobItemManager = new PipelineJobItemManager<>(getJobType().getYamlJobItemProgressSwapper());
        processConfigPersistService = new PipelineProcessConfigurationPersistService();
        dataSourceManager = new DefaultPipelineDataSourceManager();
        jobPreparer = new CDCJobPreparer();
    }
    
    @Override
    protected CDCJobItemContext buildJobItemContext(final PipelineJobConfiguration jobConfig, final int shardingItem) {
        Optional<TransmissionJobItemProgress> initProgress = jobItemManager.getProgress(jobConfig.getJobId(), shardingItem);
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.convertWithDefaultValue(
                processConfigPersistService.load(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()), getJobType().getType()));
        TransmissionProcessContext jobProcessContext = new TransmissionProcessContext(jobConfig.getJobId(), processConfig);
        CDCTaskConfiguration taskConfig = buildTaskConfiguration((CDCJobConfiguration) jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        return new CDCJobItemContext((CDCJobConfiguration) jobConfig, shardingItem, initProgress.orElse(null), jobProcessContext, taskConfig, dataSourceManager, sink);
    }
    
    private CDCTaskConfiguration buildTaskConfiguration(final CDCJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration processConfig) {
        TableAndSchemaNameMapper mapper = new TableAndSchemaNameMapper(jobConfig.getSchemaTableNames());
        IncrementalDumperContext dumperContext = buildDumperContext(jobConfig, jobShardingItem, mapper);
        ImporterConfiguration importerConfig = buildImporterConfiguration(jobConfig, processConfig, jobConfig.getSchemaTableNames(), mapper);
        return new CDCTaskConfiguration(dumperContext, importerConfig);
    }
    
    private IncrementalDumperContext buildDumperContext(final CDCJobConfiguration jobConfig, final int jobShardingItem, final TableAndSchemaNameMapper mapper) {
        JobDataNodeLine dataNodeLine = jobConfig.getJobShardingDataNodes().get(jobShardingItem);
        String dataSourceName = dataNodeLine.getEntries().iterator().next().getDataNodes().iterator().next().getDataSourceName();
        StandardPipelineDataSourceConfiguration actualDataSourceConfig = jobConfig.getDataSourceConfig().getActualDataSourceConfiguration(dataSourceName);
        DumperCommonContext dumperCommonContext = new DumperCommonContext(dataSourceName, actualDataSourceConfig, JobDataNodeLineConvertUtils.buildTableNameMapper(dataNodeLine), mapper);
        return new IncrementalDumperContext(dumperCommonContext, jobConfig.getJobId(), jobConfig.isDecodeWithTX());
    }
    
    private ImporterConfiguration buildImporterConfiguration(final CDCJobConfiguration jobConfig, final PipelineProcessConfiguration pipelineProcessConfig, final Collection<String> schemaTableNames,
                                                             final TableAndSchemaNameMapper mapper) {
        PipelineDataSourceConfiguration dataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                jobConfig.getDataSourceConfig().getType(), jobConfig.getDataSourceConfig().getParameter());
        Map<CaseInsensitiveIdentifier, Set<String>> shardingColumnsMap = new ShardingColumnsExtractor()
                .getShardingColumnsMap(jobConfig.getDataSourceConfig().getRootConfig().getRules(), schemaTableNames.stream().map(CaseInsensitiveIdentifier::new).collect(Collectors.toSet()));
        int batchSize = pipelineProcessConfig.getWrite().getBatchSize();
        JobRateLimitAlgorithm writeRateLimitAlgorithm = new TransmissionProcessContext(jobConfig.getJobId(), pipelineProcessConfig).getWriteRateLimitAlgorithm();
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, mapper, batchSize, writeRateLimitAlgorithm, 0, 1);
    }
    
    @Override
    protected PipelineTasksRunner buildTasksRunner(final CDCJobItemContext jobItemContext) {
        return new CDCTasksRunner(jobItemContext);
    }
    
    @Override
    protected void doPrepare(final Collection<CDCJobItemContext> jobItemContexts) {
        jobPreparer.initTasks(jobItemContexts);
    }
    
    @Override
    protected void processFailed(final String jobId) {
        jobAPI.disable(jobId);
    }
    
    @Override
    protected void executeInventoryTasks(final Collection<CompletableFuture<?>> futures, final Collection<CDCJobItemContext> jobItemContexts) {
        ExecuteEngine.trigger(futures, new CDCExecuteCallback("inventory", jobItemContexts.iterator().next()));
    }
    
    @Override
    protected void executeIncrementalTasks(final Collection<CompletableFuture<?>> futures, final Collection<CDCJobItemContext> jobItemContexts) {
        ExecuteEngine.trigger(futures, new CDCExecuteCallback("incremental", jobItemContexts.iterator().next()));
    }
    
    @Override
    protected void clean() {
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
