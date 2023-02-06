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

package org.apache.shardingsphere.data.pipeline.core.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

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
    private final PipelineJobItemContext jobItemContext;
    
    private final Collection<InventoryTask> inventoryTasks;
    
    private final Collection<IncrementalTask> incrementalTasks;
    
    private final PipelineJobAPI jobAPI;
    
    public InventoryIncrementalTasksRunner(final PipelineJobItemContext jobItemContext, final Collection<InventoryTask> inventoryTasks, final Collection<IncrementalTask> incrementalTasks) {
        this.jobItemContext = jobItemContext;
        this.inventoryTasks = inventoryTasks;
        this.incrementalTasks = incrementalTasks;
        jobAPI = TypedSPILoader.getService(PipelineJobAPI.class, PipelineJobIdUtils.parseJobType(jobItemContext.getJobId()).getTypeName());
    }
    
    @Override
    public void stop() {
        jobItemContext.setStopping(true);
        for (InventoryTask each : inventoryTasks) {
            each.stop();
            each.close();
        }
        for (IncrementalTask each : incrementalTasks) {
            each.stop();
            each.close();
        }
    }
    
    @Override
    public void start() {
        if (jobItemContext.isStopping()) {
            return;
        }
        TypedSPILoader.getService(PipelineJobAPI.class, PipelineJobIdUtils.parseJobType(jobItemContext.getJobId()).getTypeName()).persistJobItemProgress(jobItemContext);
        if (PipelineJobProgressDetector.allInventoryTasksFinished(inventoryTasks)) {
            log.info("All inventory tasks finished.");
            executeIncrementalTask();
        } else {
            executeInventoryTask();
        }
    }
    
    private synchronized void executeInventoryTask() {
        updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (InventoryTask each : inventoryTasks) {
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
    
    protected synchronized void executeIncrementalTask() {
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
        for (IncrementalTask each : incrementalTasks) {
            if (each.getTaskProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            futures.addAll(each.start());
        }
        ExecuteEngine.trigger(futures, new IncrementalExecuteCallback());
    }
    
    protected void inventorySuccessCallback() {
        if (PipelineJobProgressDetector.allInventoryTasksFinished(inventoryTasks)) {
            log.info("onSuccess, all inventory tasks finished.");
            executeIncrementalTask();
        } else {
            log.info("onSuccess, inventory tasks not finished");
        }
    }
    
    private final class InventoryTaskExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            inventorySuccessCallback();
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            log.error("onFailure, inventory task execute failed.", throwable);
            updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE);
            String jobId = jobItemContext.getJobId();
            jobAPI.persistJobItemErrorMessage(jobId, jobItemContext.getShardingItem(), throwable);
            jobAPI.stop(jobId);
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
            updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE);
            String jobId = jobItemContext.getJobId();
            jobAPI.persistJobItemErrorMessage(jobId, jobItemContext.getShardingItem(), throwable);
            jobAPI.stop(jobId);
        }
    }
}
