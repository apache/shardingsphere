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

import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.synctask.history.HistoryDataSyncTask;
import org.apache.shardingsphere.shardingscaling.core.synctask.history.HistoryDataSyncTaskGroup;
import org.apache.shardingsphere.shardingscaling.core.synctask.realtime.RealtimeDataSyncTask;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;

/**
 * Sync task factory.
 */
public interface SyncTaskFactory {
    
    /**
     * Create history data sync task group.
     *
     * @param syncConfiguration sync configuration
     * @param dataSourceManager  data source factory
     * @return history data sync task group
     */
    HistoryDataSyncTaskGroup createHistoryDataSyncTaskGroup(SyncConfiguration syncConfiguration, DataSourceManager dataSourceManager);
    
    /**
     * Create history data sync task.
     *
     * @param syncConfiguration sync configuration
     * @param dataSourceManager  data source factory
     * @return history data sync task
     */
    HistoryDataSyncTask createHistoryDataSyncTask(SyncConfiguration syncConfiguration, DataSourceManager dataSourceManager);
    
    /**
     * Create realtime data sync task.
     *
     * @param syncConfiguration sync configuration
     * @param dataSourceManager  data source factory
     * @return realtime data sync task
     */
    RealtimeDataSyncTask createRealtimeDataSyncTask(SyncConfiguration syncConfiguration, DataSourceManager dataSourceManager);
}
