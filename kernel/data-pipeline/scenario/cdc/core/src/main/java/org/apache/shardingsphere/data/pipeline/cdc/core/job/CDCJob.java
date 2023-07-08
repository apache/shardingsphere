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

package org.apache.shardingsphere.data.pipeline.cdc.core.job;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.prepare.CDCJobPreparer;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCTasksRunner;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.common.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * CDC job.
 */
@Slf4j
public final class CDCJob extends AbstractPipelineJob implements SimpleJob {
    
    @Getter
    private final PipelineSink sink;
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
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
            CDCJobItemContext jobItemContext = buildPipelineJobItemContext(jobConfig, shardingItem);
            PipelineTasksRunner tasksRunner = new CDCTasksRunner(jobItemContext);
            if (!addTasksRunner(shardingItem, tasksRunner)) {
                continue;
            }
            jobItemContexts.add(jobItemContext);
            jobAPI.cleanJobItemErrorMessage(jobId, shardingItem);
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
    
    private CDCJobItemContext buildPipelineJobItemContext(final CDCJobConfiguration jobConfig, final int shardingItem) {
        Optional<InventoryIncrementalJobItemProgress> initProgress = jobAPI.getJobItemProgress(jobConfig.getJobId(), shardingItem);
        CDCProcessContext jobProcessContext = jobAPI.buildPipelineProcessContext(jobConfig);
        CDCTaskConfiguration taskConfig = jobAPI.buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        return new CDCJobItemContext(jobConfig, shardingItem, initProgress.orElse(null), jobProcessContext, taskConfig, dataSourceManager, sink);
    }
    
    private void prepare(final Collection<CDCJobItemContext> jobItemContexts) {
        try {
            jobPreparer.initTasks(jobItemContexts);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            for (PipelineJobItemContext each : jobItemContexts) {
                processFailed(each, ex);
            }
            throw ex;
        }
    }
    
    @Override
    protected void processFailed(final PipelineJobItemContext jobItemContext, final Exception ex) {
        String jobId = jobItemContext.getJobId();
        log.error("job prepare failed, {}-{}", jobId, jobItemContext.getShardingItem(), ex);
        jobAPI.persistJobItemErrorMessage(jobItemContext.getJobId(), jobItemContext.getShardingItem(), ex);
        PipelineJobCenter.stop(jobId);
        jobAPI.updateJobConfigurationDisabled(jobId, true);
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
        jobAPI.updateJobItemStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
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
        
        private final PipelineJobItemContext jobItemContext;
        
        @Override
        public void onSuccess() {
            log.info("onSuccess, all {} tasks finished.", identifier);
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            log.error("onFailure, {} task execute failed.", identifier, throwable);
            String jobId = jobItemContext.getJobId();
            jobAPI.persistJobItemErrorMessage(jobId, jobItemContext.getShardingItem(), throwable);
            PipelineJobCenter.stop(jobId);
            jobAPI.updateJobConfigurationDisabled(jobId, true);
        }
    }
}
