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
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCancelingException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Transmission tasks' runner.
 */
@RequiredArgsConstructor
@Slf4j
public class TransmissionTasksRunner implements PipelineTasksRunner {
    
    @Getter
    private final TransmissionJobItemContext jobItemContext;
    
    private final Collection<PipelineTask> inventoryTasks;
    
    private final Collection<PipelineTask> incrementalTasks;
    
    private final PipelineJobType jobType;
    
    private final PipelineJobManager jobManager;
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager;
    
    public TransmissionTasksRunner(final TransmissionJobItemContext jobItemContext) {
        this.jobItemContext = jobItemContext;
        inventoryTasks = jobItemContext.getInventoryTasks();
        incrementalTasks = jobItemContext.getIncrementalTasks();
        jobType = TypedSPILoader.getService(PipelineJobType.class, PipelineJobIdUtils.parseJobType(jobItemContext.getJobId()).getType());
        jobManager = new PipelineJobManager(jobType);
        jobItemManager = new PipelineJobItemManager<>(jobType.getYamlJobItemProgressSwapper());
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
            throw new PipelineJobCancelingException();
        }
        new PipelineJobItemManager<>(TypedSPILoader.getService(PipelineJobType.class, PipelineJobIdUtils.parseJobType(jobItemContext.getJobId()).getType())
                .getYamlJobItemProgressSwapper()).persistProgress(jobItemContext);
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
            if (each.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
                continue;
            }
            futures.addAll(each.start());
        }
        ExecuteEngine.trigger(futures, new InventoryTaskExecuteCallback());
    }
    
    private void updateLocalAndRemoteJobItemStatus(final JobStatus jobStatus) {
        jobItemContext.setStatus(jobStatus);
        jobItemManager.updateStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
    }
    
    private synchronized void executeIncrementalTask() {
        if (jobItemContext.isStopping()) {
            throw new PipelineJobCancelingException();
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
            if (each.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
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
    
    private final class InventoryTaskExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            if (jobItemContext.isStopping()) {
                throw new PipelineJobCancelingException();
            }
            inventorySuccessCallback();
        }
        
        @Override
        public void onFailure(final Throwable ignored) {
        }
    }
    
    private final class IncrementalExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            log.info("onSuccess, all incremental tasks finished.");
        }
        
        @Override
        public void onFailure(final Throwable ignored) {
        }
    }
}
