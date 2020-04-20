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

package org.apache.shardingsphere.shardingscaling.core.synctask;

import java.util.Collection;

import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPosition;
import org.apache.shardingsphere.shardingscaling.core.synctask.inventory.InventoryDataSyncTask;
import org.apache.shardingsphere.shardingscaling.core.synctask.inventory.InventoryDataSyncTaskGroup;
import org.apache.shardingsphere.shardingscaling.core.synctask.incremental.IncrementalDataSyncTask;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;

/**
 * Default sync task factory.
 */
public final class DefaultSyncTaskFactory implements SyncTaskFactory {
    
    @Override
    public InventoryDataSyncTaskGroup createInventoryDataSyncTaskGroup(final SyncConfiguration syncConfiguration, final Collection<SyncTask> inventoryDataSyncTasks) {
        return new InventoryDataSyncTaskGroup(syncConfiguration, inventoryDataSyncTasks);
    }
    
    @Override
    public InventoryDataSyncTask createInventoryDataSyncTask(final SyncConfiguration syncConfiguration, final DataSourceManager dataSourceManager) {
        return new InventoryDataSyncTask(syncConfiguration, dataSourceManager);
    }
    
    @Override
    public IncrementalDataSyncTask createIncrementalDataSyncTask(final SyncConfiguration syncConfiguration, final LogPosition logPosition) {
        return new IncrementalDataSyncTask(syncConfiguration, logPosition);
    }
}
