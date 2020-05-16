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

package org.apache.shardingsphere.shardingscaling.core.job.task;

import java.util.Collection;

import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.job.position.LogPosition;
import org.apache.shardingsphere.shardingscaling.core.job.task.incremental.IncrementalDataScalingTask;
import org.apache.shardingsphere.shardingscaling.core.job.task.inventory.InventoryDataScalingTask;
import org.apache.shardingsphere.shardingscaling.core.job.task.inventory.InventoryDataScalingTaskGroup;

/**
 * Sync task factory.
 */
public interface SyncTaskFactory {
    
    /**
     * Create inventory data sync task group.
     *
     * @param inventoryDataScalingTasks  inventory data sync tasks
     * @return inventory data sync task group
     */
    InventoryDataScalingTaskGroup createInventoryDataSyncTaskGroup(Collection<ScalingTask> inventoryDataScalingTasks);
    
    /**
     * Create inventory data sync task.
     *
     * @param syncConfiguration sync configuration
     * @return inventory data sync task
     */
    InventoryDataScalingTask createInventoryDataSyncTask(SyncConfiguration syncConfiguration);
    
    /**
     * Create incremental data sync task.
     *
     * @param syncConfiguration sync configuration
     * @param logPosition  log position of incremental data start
     * @return incremental data sync task
     */
    IncrementalDataScalingTask createIncrementalDataSyncTask(SyncConfiguration syncConfiguration, LogPosition logPosition);
}
