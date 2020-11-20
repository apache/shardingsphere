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

package org.apache.shardingsphere.scaling.core.job.task.inventory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.execute.executor.AbstractShardingScalingExecutor;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;

import java.util.Collection;

/**
 * Inventory data sync task group.
 */
@Slf4j
@Getter
public final class InventoryDataScalingTaskGroup extends AbstractShardingScalingExecutor implements ScalingTask {
    
    private final Collection<ScalingTask> scalingTasks;
    
    public InventoryDataScalingTaskGroup(final Collection<ScalingTask> inventoryDataScalingTasks) {
        scalingTasks = inventoryDataScalingTasks;
    }
    
    @Override
    public void start() {
        super.start();
        for (ScalingTask each : scalingTasks) {
            PositionManager positionManager = each.getPositionManager();
            if (null != positionManager && null != positionManager.getPosition() && !(positionManager.getPosition() instanceof FinishedPosition)) {
                each.start();
            }
        }
    }
    
    @Override
    public void stop() {
        for (ScalingTask each : scalingTasks) {
            each.stop();
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        InventoryDataSyncTaskProgressGroup result = new InventoryDataSyncTaskProgressGroup();
        for (ScalingTask each : scalingTasks) {
            result.addSyncProgress(each.getProgress());
        }
        return result;
    }
}
