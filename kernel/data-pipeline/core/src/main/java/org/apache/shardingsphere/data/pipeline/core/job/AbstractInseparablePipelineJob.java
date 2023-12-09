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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract inseparable pipeline job.
 */
@Slf4j
public abstract class AbstractInseparablePipelineJob extends AbstractPipelineJob {
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager;
    
    protected AbstractInseparablePipelineJob(final String jobId) {
        super(jobId);
        jobItemManager = new PipelineJobItemManager<>(getJobType().getYamlJobItemProgressSwapper());
    }
    
    @Override
    public final void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        log.info("Execute job {}", jobId);
        PipelineJobConfiguration jobConfig = getJobType().getYamlJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        Collection<PipelineJobItemContext> jobItemContexts = new LinkedList<>();
        for (int shardingItem = 0; shardingItem < jobConfig.getJobShardingCount(); shardingItem++) {
            if (isStopping()) {
                log.info("Stopping true, ignore");
                return;
            }
            PipelineJobItemContext jobItemContext = buildJobItemContext(jobConfig, shardingItem);
            if (!addTasksRunner(shardingItem, buildTasksRunner(jobItemContext))) {
                continue;
            }
            jobItemContexts.add(jobItemContext);
            PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().clean(jobId, shardingItem);
            log.info("Start tasks runner, jobId={}, shardingItem={}", jobId, shardingItem);
        }
        if (jobItemContexts.isEmpty()) {
            log.warn("Job item contexts is empty, ignore");
            return;
        }
        prepare(jobItemContexts);
        executeInventoryTasks(jobItemContexts);
        executeIncrementalTasks(jobItemContexts);
    }
    
    protected abstract PipelineJobItemContext buildJobItemContext(PipelineJobConfiguration jobConfig, int shardingItem);
    
    protected abstract PipelineTasksRunner buildTasksRunner(PipelineJobItemContext jobItemContext);
    
    private void prepare(final Collection<PipelineJobItemContext> jobItemContexts) {
        try {
            doPrepare(jobItemContexts);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            for (PipelineJobItemContext each : jobItemContexts) {
                processFailed(each.getJobId(), each.getShardingItem(), ex);
            }
            throw ex;
        }
    }
    
    protected abstract void doPrepare(Collection<PipelineJobItemContext> jobItemContexts);
    
    private void processFailed(final String jobId, final int shardingItem, final Exception ex) {
        log.error("Job execution failed, {}-{}", jobId, shardingItem, ex);
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
        PipelineJobRegistry.stop(jobId);
        processFailed(jobId);
    }
    
    protected abstract void processFailed(String jobId);
    
    private void executeInventoryTasks(final Collection<PipelineJobItemContext> jobItemContexts) {
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (PipelineJobItemContext each : jobItemContexts) {
            updateJobItemStatus(each, JobStatus.EXECUTE_INVENTORY_TASK);
            for (PipelineTask task : ((TransmissionJobItemContext) each).getInventoryTasks()) {
                if (task.getTaskProgress().getPosition() instanceof FinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        if (futures.isEmpty()) {
            return;
        }
        executeInventoryTasks(futures, jobItemContexts);
    }
    
    protected abstract void executeInventoryTasks(Collection<CompletableFuture<?>> futures, Collection<PipelineJobItemContext> jobItemContexts);
    
    private void updateJobItemStatus(final PipelineJobItemContext jobItemContext, final JobStatus jobStatus) {
        jobItemContext.setStatus(jobStatus);
        jobItemManager.updateStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
    }
    
    private void executeIncrementalTasks(final Collection<PipelineJobItemContext> jobItemContexts) {
        log.info("Execute incremental tasks, jobId={}", getJobId());
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (PipelineJobItemContext each : jobItemContexts) {
            if (JobStatus.EXECUTE_INCREMENTAL_TASK == each.getStatus()) {
                log.info("job status already EXECUTE_INCREMENTAL_TASK, ignore");
                return;
            }
            updateJobItemStatus(each, JobStatus.EXECUTE_INCREMENTAL_TASK);
            for (PipelineTask task : ((TransmissionJobItemContext) each).getIncrementalTasks()) {
                if (task.getTaskProgress().getPosition() instanceof FinishedPosition) {
                    continue;
                }
                futures.addAll(task.start());
            }
        }
        executeIncrementalTasks(futures, jobItemContexts);
    }
    
    protected abstract void executeIncrementalTasks(Collection<CompletableFuture<?>> futures, Collection<PipelineJobItemContext> jobItemContexts);
}
