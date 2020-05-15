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

package org.apache.shardingsphere.shardingscaling.core.schedule;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.shardingscaling.core.job.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.job.task.ScalingTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

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
        for (ScalingTask each : shardingScalingJob.getInventoryDataTasks()) {
            each.stop();
        }
        for (ScalingTask each : shardingScalingJob.getIncrementalDataTasks()) {
            each.stop();
        }
    }
    
    @Override
    public void run() {
        shardingScalingJob.setStatus(SyncTaskControlStatus.MIGRATE_INVENTORY_DATA.name());
        ExecuteCallback inventoryDataTaskCallback = createInventoryDataTaskCallback();
        for (ScalingTask each : shardingScalingJob.getInventoryDataTasks()) {
            ScalingContext.getInstance().getTaskExecuteEngine().submit(each, inventoryDataTaskCallback);
        }
    }
    
    private ExecuteCallback createInventoryDataTaskCallback() {
        return new ExecuteCallback() {
            
            private final AtomicInteger finishedTaskNumber = new AtomicInteger(0);
            
            @Override
            public void onSuccess() {
                if (shardingScalingJob.getInventoryDataTasks().size() == finishedTaskNumber.incrementAndGet()) {
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
        for (ScalingTask each : shardingScalingJob.getIncrementalDataTasks()) {
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
        Collection<SyncProgress> result = new LinkedList<>();
        for (ScalingTask each : shardingScalingJob.getInventoryDataTasks()) {
            result.add(each.getProgress());
        }
        return result;
    }
    
    /**
     * Get incremental data task progress.
     *
     * @return all incremental data task progress
     */
    public Collection<SyncProgress> getIncrementalDataTaskProgress() {
        Collection<SyncProgress> result = new LinkedList<>();
        for (ScalingTask each : shardingScalingJob.getIncrementalDataTasks()) {
            result.add(each.getProgress());
        }
        return result;
    }
}
