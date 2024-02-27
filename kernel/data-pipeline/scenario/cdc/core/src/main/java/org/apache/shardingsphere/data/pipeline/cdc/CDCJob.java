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
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.PipelineCDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.core.prepare.CDCJobPreparer;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCTasksRunner;
import org.apache.shardingsphere.data.pipeline.cdc.engine.CDCJobRunnerCleaner;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractInseparablePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.engine.PipelineJobRunnerManager;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.util.ShardingColumnsExtractor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CDC job.
 */
@Slf4j
public final class CDCJob extends AbstractInseparablePipelineJob<CDCJobItemContext> {
    
    private final CDCJobAPI jobAPI = (CDCJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING");
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(new CDCJobType().getYamlJobItemProgressSwapper());
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    private final CDCJobPreparer jobPreparer = new CDCJobPreparer();
    
    @Getter
    private final PipelineSink sink;
    
    public CDCJob(final PipelineSink sink) {
        super(new PipelineJobRunnerManager(new CDCJobRunnerCleaner(sink)));
        this.sink = sink;
    }
    
    @Override
    protected CDCJobItemContext buildJobItemContext(final PipelineJobConfiguration jobConfig, final int shardingItem) {
        Optional<TransmissionJobItemProgress> initProgress = jobItemManager.getProgress(jobConfig.getJobId(), shardingItem);
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.convertWithDefaultValue(
                processConfigPersistService.load(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()), "STREAMING"));
        TransmissionProcessContext jobProcessContext = new TransmissionProcessContext(jobConfig.getJobId(), processConfig);
        CDCTaskConfiguration taskConfig = buildTaskConfiguration((CDCJobConfiguration) jobConfig, shardingItem, jobProcessContext.getProcessConfig());
        return new CDCJobItemContext((CDCJobConfiguration) jobConfig, shardingItem, initProgress.orElse(null), jobProcessContext, taskConfig, getJobRunnerManager().getDataSourceManager(), sink);
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
        PipelineWriteConfiguration write = pipelineProcessConfig.getWrite();
        JobRateLimitAlgorithm writeRateLimitAlgorithm = null == write.getRateLimiter() ? null
                : TypedSPILoader.getService(JobRateLimitAlgorithm.class, write.getRateLimiter().getType(), write.getRateLimiter().getProps());
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, mapper, write.getBatchSize(), writeRateLimitAlgorithm, 0, 1);
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
    protected ExecuteCallback buildExecuteCallback(final String identifier, final CDCJobItemContext jobItemContext) {
        return new CDCExecuteCallback(identifier, jobItemContext);
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
            if (jobItemContext.getSink() instanceof PipelineCDCSocketSink) {
                PipelineCDCSocketSink cdcSink = (PipelineCDCSocketSink) jobItemContext.getSink();
                cdcSink.getChannel().writeAndFlush(CDCResponseUtils.failed("", "", Optional.ofNullable(throwable.getMessage()).orElse("")));
            }
            PipelineJobRegistry.stop(jobId);
            jobAPI.disable(jobId);
        }
    }
}
