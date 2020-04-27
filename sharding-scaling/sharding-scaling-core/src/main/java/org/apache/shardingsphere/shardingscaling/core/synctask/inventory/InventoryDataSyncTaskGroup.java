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

package org.apache.shardingsphere.shardingscaling.core.synctask.inventory;

import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.AbstractShardingScalingExecutor;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * Inventory data sync task group.
 */
@Slf4j
public final class InventoryDataSyncTaskGroup extends AbstractShardingScalingExecutor implements SyncTask {
    
    private final Collection<SyncTask> syncTasks;
    
    public InventoryDataSyncTaskGroup(final Collection<SyncTask> inventoryDataSyncTasks) {
        syncTasks = inventoryDataSyncTasks;
    }
    
    @Override
    public void start() {
        super.start();
        for (SyncTask each : syncTasks) {
            each.start(null);
        }
    }
    
    @Override
    public void start(final ReportCallback callback) {
    }
    
    @Override
    public void stop() {
        for (SyncTask each : syncTasks) {
            each.stop();
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        InventoryDataSyncTaskProgressGroup result = new InventoryDataSyncTaskProgressGroup();
        for (SyncTask each : syncTasks) {
            result.addSyncProgress(each.getProgress());
        }
        return result;
    }
}
