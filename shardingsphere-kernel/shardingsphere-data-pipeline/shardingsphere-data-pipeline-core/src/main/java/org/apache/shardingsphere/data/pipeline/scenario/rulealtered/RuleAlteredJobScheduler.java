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
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineIgnoredException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;

/**
 * Rule altered job scheduler.
 */
@Slf4j
@RequiredArgsConstructor
@Getter
// TODO extract JobScheduler
public final class RuleAlteredJobScheduler implements Runnable {
    
    private final RuleAlteredJobContext jobContext;
    
    /**
     * Start execute job.
     */
    public void start() {
        new Thread(this).start();
    }
    
    /**
     * Stop all task.
     */
    public void stop() {
        jobContext.setStopping(true);
        log.info("stop, jobId={}, shardingItem={}", jobContext.getJobId(), jobContext.getShardingItem());
        // TODO blocking stop
        for (InventoryTask each : jobContext.getInventoryTasks()) {
            log.info("stop inventory task {} - {}", jobContext.getJobId(), each.getTaskId());
            each.stop();
            each.close();
        }
        for (IncrementalTask each : jobContext.getIncrementalTasks()) {
            log.info("stop incremental task {} - {}", jobContext.getJobId(), each.getTaskId());
            each.stop();
            each.close();
        }
    }
    
    @Override
    public void run() {
        String jobId = jobContext.getJobId();
        GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        try {
            jobContext.getJobPreparer().prepare(jobContext);
        } catch (final PipelineIgnoredException ex) {
            log.info("pipeline ignore exception: {}", ex.getMessage());
            RuleAlteredJobCenter.stop(jobId);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("job prepare failed, {}-{}", jobId, jobContext.getShardingItem(), ex);
            RuleAlteredJobCenter.stop(jobId);
            jobContext.setStatus(JobStatus.PREPARING_FAILURE);
            governanceRepositoryAPI.persistJobProgress(jobContext);
            throw ex;
        }
        if (jobContext.isStopping()) {
            log.info("job stopping, ignore inventory task");
            return;
        }
        governanceRepositoryAPI.persistJobProgress(jobContext);
        if (executeInventoryTask()) {
            if (jobContext.isStopping()) {
                log.info("stopping, ignore incremental task");
                return;
            }
            executeIncrementalTask();
        }
    }
    
    private synchronized boolean executeInventoryTask() {
        if (RuleAlteredJobProgressDetector.allInventoryTasksFinished(jobContext.getInventoryTasks())) {
            log.info("All inventory tasks finished.");
            return true;
        }
        log.info("-------------- Start inventory task --------------");
        jobContext.setStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        ExecuteCallback inventoryTaskCallback = createInventoryTaskCallback();
        for (InventoryTask each : jobContext.getInventoryTasks()) {
            if (each.getProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            jobContext.getRuleAlteredContext().getInventoryDumperExecuteEngine().submit(each, inventoryTaskCallback);
        }
        return false;
    }
    
    private ExecuteCallback createInventoryTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                if (RuleAlteredJobProgressDetector.allInventoryTasksFinished(jobContext.getInventoryTasks())) {
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
        for (IncrementalTask each : jobContext.getIncrementalTasks()) {
            if (each.getProgress().getPosition() instanceof FinishedPosition) {
                continue;
            }
            jobContext.getRuleAlteredContext().getIncrementalDumperExecuteEngine().submit(each, incrementalTaskCallback);
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
