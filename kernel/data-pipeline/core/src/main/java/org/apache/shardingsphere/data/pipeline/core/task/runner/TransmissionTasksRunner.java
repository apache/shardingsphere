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
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Transmission tasks' runner.
 */
@RequiredArgsConstructor
@Slf4j
public final class TransmissionTasksRunner implements PipelineTasksRunner {
    
    @Getter
    private final TransmissionJobItemContext jobItemContext;
    
    private final Collection<PipelineTask> inventoryTasks;
    
    private final Collection<PipelineTask> incrementalTasks;
    
    private final PipelineJobType<?> jobType;
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager;
    
    public TransmissionTasksRunner(final TransmissionJobItemContext jobItemContext) {
        this.jobItemContext = jobItemContext;
        inventoryTasks = jobItemContext.getInventoryTasks();
        incrementalTasks = jobItemContext.getIncrementalTasks();
        jobType = TypedSPILoader.getService(PipelineJobType.class, PipelineJobIdUtils.parseJobType(jobItemContext.getJobId()).getType());
        jobItemManager = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper());
    }
    
    @Override
    public void start() {
        ShardingSpherePreconditions.checkState(!jobItemContext.isStopping(), PipelineJobCancelingException::new);
        jobItemManager.persistProgress(jobItemContext);
        if (PipelineJobProgressDetector.isAllInventoryTasksFinished(inventoryTasks)) {
            log.info("All inventory tasks finished.");
            executeIncrementalTasks();
        } else {
            executeInventoryTasks();
        }
    }
    
    private synchronized void executeInventoryTasks() {
        updateLocalAndRemoteJobItemStatusForInventory();
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (PipelineTask each : inventoryTasks) {
            if (each.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
                continue;
            }
            futures.addAll(each.start());
        }
        PipelineExecuteEngine.trigger(futures, new InventoryTaskExecuteCallback());
    }
    
    private void updateLocalAndRemoteJobItemStatusForInventory() {
        jobItemContext.setStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        jobItemManager.updateStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), JobStatus.EXECUTE_INVENTORY_TASK);
    }
    
    private synchronized void executeIncrementalTasks() {
        ShardingSpherePreconditions.checkState(!jobItemContext.isStopping(), PipelineJobCancelingException::new);
        if (incrementalTasks.isEmpty()) {
            log.info("Incremental tasks are empty, ignore.");
            return;
        }
        if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemContext.getStatus()) {
            log.info("Incremental tasks had already run, ignore.");
            return;
        }
        Collection<CompletableFuture<?>> futures = new LinkedList<>();
        for (PipelineTask each : incrementalTasks) {
            if (each.getTaskProgress().getPosition() instanceof IngestFinishedPosition) {
                continue;
            }
            futures.addAll(each.start());
        }
        updateLocalAndRemoteJobItemProgressForIncremental();
        PipelineExecuteEngine.trigger(futures, new IncrementalExecuteCallback());
    }
    
    private void updateLocalAndRemoteJobItemProgressForIncremental() {
        jobItemContext.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
        jobItemManager.updateProgress(jobItemContext);
    }
    
    @Override
    public void stop() {
        jobItemContext.setStopping(true);
        inventoryTasks.forEach(PipelineTask::stop);
        incrementalTasks.forEach(PipelineTask::stop);
    }
    
    private final class InventoryTaskExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            ShardingSpherePreconditions.checkState(!jobItemContext.isStopping(), PipelineJobCancelingException::new);
            if (PipelineJobProgressDetector.isAllInventoryTasksFinished(inventoryTasks)) {
                log.info("onSuccess, all inventory tasks finished.");
                PipelineJobProgressPersistService.persistNow(jobItemContext.getJobId(), jobItemContext.getShardingItem());
                executeIncrementalTasks();
            } else {
                log.info("onSuccess, inventory tasks did not finish.");
            }
        }
        
        @Override
        public void onFailure(final Throwable ignored) {
        }
    }
    
    private static final class IncrementalExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            log.info("onSuccess, all incremental tasks finished.");
        }
        
        @Override
        public void onFailure(final Throwable ignored) {
        }
    }
}
