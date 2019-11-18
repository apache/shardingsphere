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
import info.avalon566.shardingscaling.core.config.SyncType;
import info.avalon566.shardingscaling.core.execute.engine.local.LocalReporter;

/**
 * Sync task factory.
 *
 * @author avalon566
 */
public final class SyncTaskFactory {

    /**
     * create execute job instance by execute configuration.
     *
     * @param syncConfiguration value
     * @param reporter value
     * @return execute job
     */
    public static SyncTask createSyncJobInstance(final SyncConfiguration syncConfiguration, final LocalReporter reporter) {
        if (SyncType.TableSlice.equals(syncConfiguration.getSyncType())) {
            return new HistoryDataSyncTask(syncConfiguration, reporter);
        } else if (SyncType.Realtime.equals(syncConfiguration.getSyncType())) {
            return new RealtimeDataSyncTask(syncConfiguration, reporter);
        }
        throw new UnsupportedOperationException();
    }
}
