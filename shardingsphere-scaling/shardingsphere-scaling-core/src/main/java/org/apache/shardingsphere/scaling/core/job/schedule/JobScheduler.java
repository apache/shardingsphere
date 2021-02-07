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

package org.apache.shardingsphere.scaling.core.job.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.executor.engine.ExecuteCallback;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.util.ScalingTaskUtil;

/**
 * Job scheduler.
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public final class JobScheduler implements Runnable {
    
    private final JobContext jobContext;
    
    /**
     * Start execute scaling job.
     */
    public void start() {
        new Thread(this).start();
    }
    
    /**
     * Stop all scaling task.
     */
    public void stop() {
        log.info("stop scaling job {}", jobContext.getJobId());
        if (jobContext.getStatus().isRunning()) {
            jobContext.setStatus(JobStatus.STOPPING);
        }
        for (ScalingTask each : jobContext.getInventoryTasks()) {
            log.info("stop inventory task {} - {}", jobContext.getJobId(), each.getTaskId());
            each.stop();
        }
        for (ScalingTask each : jobContext.getIncrementalTasks()) {
            log.info("stop incremental task {} - {}", jobContext.getJobId(), each.getTaskId());
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
        if (ScalingTaskUtil.allInventoryTasksFinished(jobContext.getInventoryTasks())) {
            log.info("All inventory tasks finished.");
            return true;
        }
        log.info("-------------- Start inventory task --------------");
        jobContext.setStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        ExecuteCallback inventoryTaskCallback = createInventoryTaskCallback();
        for (ScalingTask each : jobContext.getInventoryTasks()) {
            ScalingContext.getInstance().getInventoryDumperExecuteEngine().submit(each, inventoryTaskCallback);
        }
        return false;
    }
    
    private ExecuteCallback createInventoryTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                if (ScalingTaskUtil.allInventoryTasksFinished(jobContext.getInventoryTasks())) {
                    log.info("All inventory tasks finished.");
                    executeIncrementalTask();
                }
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Inventory task execute failed.", throwable);
                stop();
                jobContext.setStatus(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE);
            }
        };
    }
    
    private synchronized void executeIncrementalTask() {
        if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobContext.getStatus()) {
            return;
        }
        log.info("-------------- Start incremental task --------------");
        jobContext.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
        ExecuteCallback incrementalTaskCallback = createIncrementalTaskCallback();
        for (ScalingTask each : jobContext.getIncrementalTasks()) {
            ScalingContext.getInstance().getIncrementalDumperExecuteEngine().submit(each, incrementalTaskCallback);
        }
    }
    
    private ExecuteCallback createIncrementalTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                jobContext.setStatus(JobStatus.STOPPED);
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Incremental task execute failed.", throwable);
                stop();
                jobContext.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE);
            }
        };
    }
}
