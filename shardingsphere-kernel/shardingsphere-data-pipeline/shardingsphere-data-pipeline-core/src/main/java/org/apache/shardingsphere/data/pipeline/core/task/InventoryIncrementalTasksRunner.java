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
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobItemAPI;
import org.apache.shardingsphere.data.pipeline.core.api.impl.InventoryIncrementalJobItemAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;

import java.util.Collection;

/**
 * Inventory incremental tasks' runner.
 */
@RequiredArgsConstructor
@Slf4j
public final class InventoryIncrementalTasksRunner implements PipelineTasksRunner {
    
    private final PipelineJobItemAPI jobItemAPI = new InventoryIncrementalJobItemAPIImpl();
    
    @Getter
    private final PipelineJobItemContext jobItemContext;
    
    private final Collection<InventoryTask> inventoryTasks;
    
    private final Collection<IncrementalTask> incrementalTasks;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine incrementalDumperExecuteEngine;
    
    @Override
    public void stop() {
        jobItemContext.setStopping(true);
        log.info("stop, jobId={}, shardingItem={}", jobItemContext.getJobId(), jobItemContext.getShardingItem());
        // TODO blocking stop
        for (InventoryTask each : inventoryTasks) {
            log.info("stop inventory task {} - {}", jobItemContext.getJobId(), each.getTaskId());
            each.stop();
            each.close();
        }
        for (IncrementalTask each : incrementalTasks) {
            log.info("stop incremental task {} - {}", jobItemContext.getJobId(), each.getTaskId());
            each.stop();
            each.close();
        }
    }
    
    @Override
    public void start() {
        if (jobItemContext.isStopping()) {
            log.info("job stopping, ignore inventory task");
            return;
        }
        PipelineAPIFactory.getPipelineJobAPI(PipelineJobIdUtils.parseJobType(jobItemContext.getJobId())).persistJobItemProgress(jobItemContext);
        if (executeInventoryTask()) {
            if (jobItemContext.isStopping()) {
                log.info("stopping, ignore incremental task");
                return;
            }
            executeIncrementalTask();
        }
    }
    
    private synchronized boolean executeInventoryTask() {
        if (PipelineJobProgressDetector.allInventoryTasksFinished(inventoryTasks)) {
            log.info("All inventory tasks finished.");
            return true;
        }
        log.info("-------------- Start inventory task --------------");
        updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        ExecuteCallback inventoryTaskCallback = createInventoryTaskCallback();
        for (InventoryTask each : inventoryTasks) {
            if (each.getTaskProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            inventoryDumperExecuteEngine.submit(each, inventoryTaskCallback);
        }
        return false;
    }
    
    private void updateLocalAndRemoteJobItemStatus(final JobStatus jobStatus) {
        jobItemContext.setStatus(jobStatus);
        jobItemAPI.updateJobItemStatus(jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobStatus);
    }
    
    private ExecuteCallback createInventoryTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                if (PipelineJobProgressDetector.allInventoryTasksFinished(inventoryTasks)) {
                    log.info("onSuccess, all inventory tasks finished.");
                    executeIncrementalTask();
                }
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Inventory task execute failed.", throwable);
                updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE);
                stop();
            }
        };
    }
    
    private synchronized void executeIncrementalTask() {
        if (incrementalTasks.isEmpty()) {
            log.info("incrementalTasks empty, ignore");
            return;
        }
        if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemContext.getStatus()) {
            log.info("job status already EXECUTE_INCREMENTAL_TASK, ignore");
            return;
        }
        log.info("-------------- Start incremental task --------------");
        updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
        ExecuteCallback incrementalTaskCallback = createIncrementalTaskCallback();
        for (IncrementalTask each : incrementalTasks) {
            if (each.getTaskProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            incrementalDumperExecuteEngine.submit(each, incrementalTaskCallback);
        }
    }
    
    private ExecuteCallback createIncrementalTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Incremental task execute failed.", throwable);
                updateLocalAndRemoteJobItemStatus(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE);
                stop();
            }
        };
    }
}
