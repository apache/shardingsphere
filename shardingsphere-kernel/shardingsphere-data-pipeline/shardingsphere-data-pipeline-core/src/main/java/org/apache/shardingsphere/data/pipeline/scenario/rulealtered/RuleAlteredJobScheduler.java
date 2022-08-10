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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobContext;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;

import java.util.Collection;

/**
 * Rule altered job scheduler.
 */
@RequiredArgsConstructor
@Slf4j
public final class RuleAlteredJobScheduler implements PipelineTasksRunner {
    
    @Getter
    private final PipelineJobContext jobContext;
    
    private final Collection<InventoryTask> inventoryTasks;
    
    private final Collection<IncrementalTask> incrementalTasks;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine incrementalDumperExecuteEngine;
    
    @Override
    public void stop() {
        jobContext.setStopping(true);
        log.info("stop, jobId={}, shardingItem={}", jobContext.getJobId(), jobContext.getShardingItem());
        // TODO blocking stop
        for (InventoryTask each : inventoryTasks) {
            log.info("stop inventory task {} - {}", jobContext.getJobId(), each.getTaskId());
            each.stop();
            each.close();
        }
        for (IncrementalTask each : incrementalTasks) {
            log.info("stop incremental task {} - {}", jobContext.getJobId(), each.getTaskId());
            each.stop();
            each.close();
        }
    }
    
    @Override
    public void start() {
        if (jobContext.isStopping()) {
            log.info("job stopping, ignore inventory task");
            return;
        }
        RuleAlteredJobAPIFactory.getInstance().persistJobProgress(jobContext);
        if (executeInventoryTask()) {
            if (jobContext.isStopping()) {
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
        jobContext.setStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        ExecuteCallback inventoryTaskCallback = createInventoryTaskCallback();
        for (InventoryTask each : inventoryTasks) {
            if (each.getProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            inventoryDumperExecuteEngine.submit(each, inventoryTaskCallback);
        }
        return false;
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
                jobContext.setStatus(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE);
                stop();
            }
        };
    }
    
    private synchronized void executeIncrementalTask() {
        if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobContext.getStatus()) {
            log.info("job status already EXECUTE_INCREMENTAL_TASK, ignore");
            return;
        }
        log.info("-------------- Start incremental task --------------");
        jobContext.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
        ExecuteCallback incrementalTaskCallback = createIncrementalTaskCallback();
        for (IncrementalTask each : incrementalTasks) {
            if (each.getProgress().getPosition() instanceof FinishedPosition) {
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
                jobContext.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE);
                stop();
            }
        };
    }
}
