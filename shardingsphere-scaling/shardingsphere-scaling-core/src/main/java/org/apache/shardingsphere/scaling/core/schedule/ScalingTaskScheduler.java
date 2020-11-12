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
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryDataSyncTaskProgressGroup;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Sharding scaling task scheduler.
 */
@Slf4j
@RequiredArgsConstructor
public final class ScalingTaskScheduler implements Runnable {
    
    private final ShardingScalingJob shardingScalingJob;
    
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
        if (!SyncTaskControlStatus.valueOf(shardingScalingJob.getStatus()).isStoppedStatus()) {
            shardingScalingJob.setStatus(SyncTaskControlStatus.STOPPING.name());
        }
        for (ScalingTask<InventoryPosition> each : shardingScalingJob.getInventoryDataTasks()) {
            each.stop();
        }
        for (ScalingTask<IncrementalPosition> each : shardingScalingJob.getIncrementalDataTasks()) {
            each.stop();
        }
    }
    
    @Override
    public void run() {
        shardingScalingJob.setStatus(SyncTaskControlStatus.MIGRATE_INVENTORY_DATA.name());
        if (ScalingTaskUtil.allInventoryTasksFinished(shardingScalingJob.getInventoryDataTasks())) {
            executeIncrementalDataSyncTask();
            return;
        }
        log.info("Start inventory data sync task.");
        ExecuteCallback inventoryDataTaskCallback = createInventoryDataTaskCallback();
        for (ScalingTask<InventoryPosition> each : shardingScalingJob.getInventoryDataTasks()) {
            ScalingContext.getInstance().getTaskExecuteEngine().submit(each, inventoryDataTaskCallback);
        }
    }
    
    private ExecuteCallback createInventoryDataTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                if (ScalingTaskUtil.allInventoryTasksFinished(shardingScalingJob.getInventoryDataTasks())) {
                    executeIncrementalDataSyncTask();
                }
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Inventory task execute failed.", throwable);
                stop();
                shardingScalingJob.setStatus(SyncTaskControlStatus.MIGRATE_INVENTORY_DATA_FAILURE.name());
            }
        };
    }
    
    private void executeIncrementalDataSyncTask() {
        log.info("Start incremental data sync task.");
        if (!SyncTaskControlStatus.MIGRATE_INVENTORY_DATA.name().equals(shardingScalingJob.getStatus())) {
            shardingScalingJob.setStatus(SyncTaskControlStatus.STOPPED.name());
            return;
        }
        ExecuteCallback incrementalDataTaskCallback = createIncrementalDataTaskCallback();
        for (ScalingTask<IncrementalPosition> each : shardingScalingJob.getIncrementalDataTasks()) {
            ScalingContext.getInstance().getTaskExecuteEngine().submit(each, incrementalDataTaskCallback);
        }
        shardingScalingJob.setStatus(SyncTaskControlStatus.SYNCHRONIZE_INCREMENTAL_DATA.name());
    }
    
    private ExecuteCallback createIncrementalDataTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                shardingScalingJob.setStatus(SyncTaskControlStatus.STOPPED.name());
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("Incremental task execute failed.", throwable);
                stop();
                shardingScalingJob.setStatus(SyncTaskControlStatus.SYNCHRONIZE_INCREMENTAL_DATA_FAILURE.name());
            }
        };
    }
    
    /**
     * Get inventory data task progress.
     *
     * @return all inventory data task progress
     */
    public Collection<SyncProgress> getInventoryDataTaskProgress() {
        return shardingScalingJob.getInventoryDataTasks().stream()
                .map(ScalingTask::getProgress)
                .flatMap(each -> ((InventoryDataSyncTaskProgressGroup) each).getInnerTaskProgresses().stream())
                .collect(Collectors.toList());
    }
    
    /**
     * Get incremental data task progress.
     *
     * @return all incremental data task progress
     */
    public Collection<SyncProgress> getIncrementalDataTaskProgress() {
        return shardingScalingJob.getIncrementalDataTasks().stream().map(ScalingTask::getProgress).collect(Collectors.toList());
    }
}
