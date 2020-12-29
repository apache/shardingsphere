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

package org.apache.shardingsphere.scaling.core.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.TaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Scaling task scheduler.
 */
@Slf4j
@RequiredArgsConstructor
public final class ScalingTaskScheduler implements Runnable {
    
    private final ScalingJob scalingJob;
    
    /**
     * Start execute scaling task.
     */
    public void start() {
        new Thread(this).start();
    }
    
    /**
     * Stop all scaling task.
     */
    public void stop() {
        if (JobStatus.valueOf(scalingJob.getStatus()).isRunning()) {
            scalingJob.setStatus(JobStatus.STOPPING.name());
        }
        for (ScalingTask each : scalingJob.getInventoryTasks()) {
            each.stop();
        }
        for (ScalingTask each : scalingJob.getIncrementalTasks()) {
            each.stop();
        }
    }
    
    @Override
    public void run() {
        if (executeInventoryTask()) {
            executeIncrementalTask();
        }
    }
    
    private synchronized boolean executeInventoryTask() {
        if (ScalingTaskUtil.allInventoryTasksFinished(scalingJob.getInventoryTasks())) {
            log.info("All inventory tasks finished.");
            return true;
        }
        log.info("-------------- Start inventory task --------------");
        scalingJob.setStatus(JobStatus.EXECUTE_INVENTORY_TASK.name());
        ExecuteCallback inventoryTaskCallback = createInventoryTaskCallback();
        for (ScalingTask each : scalingJob.getInventoryTasks()) {
            ScalingContext.getInstance().getInventoryDumperExecuteEngine().submit(each, inventoryTaskCallback);
        }
        return false;
    }
    
    private ExecuteCallback createInventoryTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                if (ScalingTaskUtil.allInventoryTasksFinished(scalingJob.getInventoryTasks())) {
                    log.info("All inventory tasks finished.");
                    executeIncrementalTask();
                }
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Inventory task execute failed.", throwable);
                stop();
                scalingJob.setStatus(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name());
            }
        };
    }
    
    private synchronized void executeIncrementalTask() {
        if (JobStatus.EXECUTE_INCREMENTAL_TASK.name().equals(scalingJob.getStatus())) {
            return;
        }
        log.info("-------------- Start incremental task --------------");
        scalingJob.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK.name());
        ExecuteCallback incrementalTaskCallback = createIncrementalTaskCallback();
        for (ScalingTask each : scalingJob.getIncrementalTasks()) {
            ScalingContext.getInstance().getIncrementalDumperExecuteEngine().submit(each, incrementalTaskCallback);
        }
    }
    
    private ExecuteCallback createIncrementalTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                scalingJob.setStatus(JobStatus.STOPPED.name());
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Incremental task execute failed.", throwable);
                stop();
                scalingJob.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name());
            }
        };
    }
    
    /**
     * Get inventory data task progress.
     *
     * @return all inventory data task progress
     */
    public Collection<TaskProgress> getInventoryTaskProgress() {
        return scalingJob.getInventoryTasks().stream()
                .map(ScalingTask::getProgress)
                .collect(Collectors.toList());
    }
    
    /**
     * Get incremental data task progress.
     *
     * @return all incremental data task progress
     */
    public Collection<TaskProgress> getIncrementalTaskProgress() {
        return scalingJob.getIncrementalTasks().stream().map(ScalingTask::getProgress).collect(Collectors.toList());
    }
}
