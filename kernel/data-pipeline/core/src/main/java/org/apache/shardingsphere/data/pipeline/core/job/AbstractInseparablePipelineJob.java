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

package org.apache.shardingsphere.data.pipeline.core.job;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.engine.PipelineJobRunnerManager;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract inseparable pipeline job.
 *
 * @param <T> type of pipeline job configuration
 * @param <I> type of pipeline job item context
 * @param <P> type of pipeline job item progress
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class AbstractInseparablePipelineJob<T extends PipelineJobConfiguration, I extends PipelineJobItemContext, P extends PipelineJobItemProgress> implements PipelineJob {
    
    private final PipelineJobRunnerManager jobRunnerManager;
    
    @SuppressWarnings("unchecked")
    @Override
    public final void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        log.info("Execute job {}", jobId);
        PipelineJobType jobType = PipelineJobIdUtils.parseJobType(jobId);
        T jobConfig = (T) jobType.getYamlJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        TransmissionProcessContext jobProcessContext = jobType.isTransmissionJob() ? createTransmissionProcessContext(jobId) : null;
        Collection<I> jobItemContexts = new LinkedList<>();
        PipelineJobItemManager<P> jobItemManager = new PipelineJobItemManager<>(jobType.getYamlJobItemProgressSwapper());
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId));
        for (int shardingItem = 0; shardingItem < jobConfig.getJobShardingCount(); shardingItem++) {
            if (jobRunnerManager.isStopping()) {
                log.info("Job is stopping, ignore.");
                return;
            }
            P jobItemProgress = jobItemManager.getProgress(shardingContext.getJobName(), shardingItem).orElse(null);
            I jobItemContext = buildJobItemContext(jobConfig, shardingItem, jobItemProgress, jobProcessContext);
            if (!jobRunnerManager.addTasksRunner(shardingItem, buildTasksRunner(jobItemContext))) {
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
        prepare(jobItemContexts, governanceFacade);
        executeInventoryTasks(jobItemContexts, jobItemManager);
        executeIncrementalTasks(jobItemContexts, jobItemManager);
    }
    
    private TransmissionProcessContext createTransmissionProcessContext(final String jobId) {
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.fillInDefaultValue(
                new PipelineProcessConfigurationPersistService().load(PipelineJobIdUtils.parseContextKey(jobId), PipelineJobIdUtils.parseJobType(jobId).getType()));
        return new TransmissionProcessContext(jobId, processConfig);
    }
    
    protected abstract I buildJobItemContext(T jobConfig, int shardingItem, P jobItemProgress, TransmissionProcessContext jobProcessContext);
    
    protected abstract PipelineTasksRunner buildTasksRunner(I jobItemContext);
    
    private void prepare(final Collection<I> jobItemContexts, final PipelineGovernanceFacade governanceFacade) {
        try {
            doPrepare(jobItemContexts);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            for (PipelineJobItemContext each : jobItemContexts) {
                processFailed(each.getJobId(), each.getShardingItem(), ex, governanceFacade);
            }
            throw ex;
        }
    }
    
    protected abstract void doPrepare(Collection<I> jobItemContexts);
    
    private void processFailed(final String jobId, final int shardingItem, final Exception ex, final PipelineGovernanceFacade governanceFacade) {
        log.error("Job {}-{} execution failed.", jobId, shardingItem, ex);
        governanceFacade.getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
        PipelineJobRegistry.stop(jobId);
        processFailed(jobId);
    }
    
    protected abstract void processFailed(String jobId);
    
    private void executeInventoryTasks(final Collection<I> jobItemContexts, final PipelineJobItemManager<P> jobItemManager) {
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (I each : jobItemContexts) {
            updateJobItemStatus(each, JobStatus.EXECUTE_INVENTORY_TASK, jobItemManager);
            for (PipelineTask task : ((TransmissionJobItemContext) each).getInventoryTasks()) {
                if (task.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        if (futures.isEmpty()) {
            return;
        }
        ExecuteEngine.trigger(futures, buildExecuteCallback("inventory", jobItemContexts.iterator().next()));
    }
    
    private void executeIncrementalTasks(final Collection<I> jobItemContexts, final PipelineJobItemManager<P> jobItemManager) {
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (I each : jobItemContexts) {
            if (JobStatus.EXECUTE_INCREMENTAL_TASK == each.getStatus()) {
                log.info("Job status has already EXECUTE_INCREMENTAL_TASK, ignore.");
                return;
            }
            updateJobItemStatus(each, JobStatus.EXECUTE_INCREMENTAL_TASK, jobItemManager);
            for (PipelineTask task : ((TransmissionJobItemContext) each).getIncrementalTasks()) {
                if (task.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        ExecuteEngine.trigger(futures, buildExecuteCallback("incremental", jobItemContexts.iterator().next()));
    }
    
    private void updateJobItemStatus(final I jobItemContext, final JobStatus jobStatus, final PipelineJobItemManager<P> jobItemManager) {
        jobItemContext.setStatus(jobStatus);
        jobItemManager.updateStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
    }
    
    protected abstract ExecuteCallback buildExecuteCallback(String identifier, I jobItemContext);
}
