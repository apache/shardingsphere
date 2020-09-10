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
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.FinishedInventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryDataScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryDataScalingTaskGroup;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Sharding scaling task scheduler.
 */
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
        ExecuteCallback inventoryDataTaskCallback = createInventoryDataTaskCallback();
        if (isFinished(shardingScalingJob.getInventoryDataTasks())) {
            executeIncrementalDataSyncTask();
            return;
        }
        for (ScalingTask<InventoryPosition> each : shardingScalingJob.getInventoryDataTasks()) {
            ScalingContext.getInstance().getTaskExecuteEngine().submit(each, inventoryDataTaskCallback);
        }
    }
    
    private boolean isFinished(final List<ScalingTask<InventoryPosition>> inventoryDataTasks) {
        return inventoryDataTasks.stream().allMatch(each -> ((InventoryDataScalingTaskGroup) each).getScalingTasks().stream().allMatch(getFinishPredicate()));
    }
    
    private Predicate<ScalingTask<InventoryPosition>> getFinishPredicate() {
        return each -> ((InventoryDataScalingTask) each).getPositionManager().getPosition() instanceof FinishedInventoryPosition;
    }
    
    private ExecuteCallback createInventoryDataTaskCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                if (isFinished(shardingScalingJob.getInventoryDataTasks())) {
                    executeIncrementalDataSyncTask();
                }
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                stop();
                shardingScalingJob.setStatus(SyncTaskControlStatus.MIGRATE_INVENTORY_DATA_FAILURE.name());
            }
        };
    }
    
    private void executeIncrementalDataSyncTask() {
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
        return shardingScalingJob.getInventoryDataTasks().stream().map(ScalingTask::getProgress).collect(Collectors.toList());
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
