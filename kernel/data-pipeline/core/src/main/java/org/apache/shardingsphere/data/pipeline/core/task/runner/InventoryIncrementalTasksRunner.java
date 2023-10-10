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

package org.apache.shardingsphere.data.pipeline.core.task.runner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.common.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Inventory incremental tasks' runner.
 */
@RequiredArgsConstructor
@Slf4j
public class InventoryIncrementalTasksRunner implements PipelineTasksRunner {
    
    @Getter
    private final InventoryIncrementalJobItemContext jobItemContext;
    
    private final Collection<PipelineTask> inventoryTasks;
    
    private final Collection<PipelineTask> incrementalTasks;
    
    private final PipelineJobAPI jobAPI;
    
    public InventoryIncrementalTasksRunner(final InventoryIncrementalJobItemContext jobItemContext) {
        this.jobItemContext = jobItemContext;
        inventoryTasks = jobItemContext.getInventoryTasks();
        incrementalTasks = jobItemContext.getIncrementalTasks();
        jobAPI = TypedSPILoader.getService(PipelineJobAPI.class, PipelineJobIdUtils.parseJobType(jobItemContext.getJobId()).getType());
    }
    
    @Override
    public void stop() {
        jobItemContext.setStopping(true);
        for (PipelineTask each : inventoryTasks) {
            each.stop();
            QuietlyCloser.close(each);
        }
        for (PipelineTask each : incrementalTasks) {
            each.stop();
            QuietlyCloser.close(each);
        }
    }
    
    @Override
    public void start() {
        if (jobItemContext.isStopping()) {
            return;
        }
        TypedSPILoader.getService(PipelineJobAPI.class, PipelineJobIdUtils.parseJobType(jobItemContext.getJobId()).getType()).persistJobItemProgress(jobItemContext);
        if (PipelineJobProgressDetector.isAllInventoryTasksFinished(inventoryTasks)) {
            log.info("All inventory tasks finished.");
            executeIncrementalTask();
        } else {
            executeInventoryTask();
        }
    }
    
    private synchronized void executeInventoryTask() {
        updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (PipelineTask each : inventoryTasks) {
            if (each.getTaskProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            futures.addAll(each.start());
        }
        ExecuteEngine.trigger(futures, new InventoryTaskExecuteCallback());
    }
    
    private void updateLocalAndRemoteJobItemStatus(final JobStatus jobStatus) {
        jobItemContext.setStatus(jobStatus);
        jobAPI.updateJobItemStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
    }
    
    private synchronized void executeIncrementalTask() {
        if (jobItemContext.isStopping()) {
            log.info("Stopping is true, ignore incremental task");
            return;
        }
        if (incrementalTasks.isEmpty()) {
            log.info("incrementalTasks empty, ignore");
            return;
        }
        if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemContext.getStatus()) {
            log.info("job status already EXECUTE_INCREMENTAL_TASK, ignore");
            return;
        }
        updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (PipelineTask each : incrementalTasks) {
            if (each.getTaskProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            futures.addAll(each.start());
        }
        ExecuteEngine.trigger(futures, new IncrementalExecuteCallback());
    }
    
    protected void inventorySuccessCallback() {
        if (PipelineJobProgressDetector.isAllInventoryTasksFinished(inventoryTasks)) {
            log.info("onSuccess, all inventory tasks finished.");
            PipelineJobProgressPersistService.persistNow(jobItemContext.getJobId(), jobItemContext.getShardingItem());
            executeIncrementalTask();
        } else {
            log.info("onSuccess, inventory tasks not finished");
        }
    }
    
    protected void inventoryFailureCallback(final Throwable throwable) {
        log.error("onFailure, inventory task execute failed.", throwable);
        String jobId = jobItemContext.getJobId();
        jobAPI.updateJobItemErrorMessage(jobId, jobItemContext.getShardingItem(), throwable);
        try {
            jobAPI.stop(jobId);
        } catch (final PipelineJobNotFoundException ignored) {
        }
    }
    
    private final class InventoryTaskExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            if (jobItemContext.isStopping()) {
                log.info("Inventory task onSuccess, stopping true, ignore");
                return;
            }
            inventorySuccessCallback();
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            inventoryFailureCallback(throwable);
        }
    }
    
    private final class IncrementalExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            log.info("onSuccess, all incremental tasks finished.");
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            log.error("onFailure, incremental task execute failed.", throwable);
            String jobId = jobItemContext.getJobId();
            jobAPI.updateJobItemErrorMessage(jobId, jobItemContext.getShardingItem(), throwable);
            try {
                jobAPI.stop(jobId);
            } catch (final PipelineJobNotFoundException ignored) {
            }
        }
    }
}
