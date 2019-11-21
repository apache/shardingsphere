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

package info.avalon566.shardingscaling.core.synctask;

import info.avalon566.shardingscaling.core.config.SyncConfiguration;

/**
 * Sync task factory.
 *
 * @author avalon566
 */
public interface SyncTaskFactory {

    /**
     * Create history data sync task group.
     *
     * @param syncConfiguration sync configuration
     * @return history data sync task group
     */
    HistoryDataSyncTaskGroup createHistoryDataSyncTaskGroup(SyncConfiguration syncConfiguration);

    /**
     * Create history data sync task.
     *
     * @param syncConfiguration sync configuration
     * @return history data sync task
     */
    HistoryDataSyncTask createHistoryDataSyncTask(SyncConfiguration syncConfiguration);

    /**
     * Create realtime data sync task.
     *
     * @param syncConfiguration sync configuration
     * @return realtime data sync task
     */
    RealtimeDataSyncTask createRealtimeDataSyncTask(SyncConfiguration syncConfiguration);
}
