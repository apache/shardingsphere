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

package info.avalon566.shardingscaling.core.job.sync.executor.local;

import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.job.SyncTaskProgress;
import info.avalon566.shardingscaling.core.job.sync.SyncTask;
import info.avalon566.shardingscaling.core.job.sync.executor.Reporter;
import info.avalon566.shardingscaling.core.job.sync.executor.SyncJobExecutor;
import info.avalon566.shardingscaling.core.job.sync.SyncTaskFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Local sync job executor.
 *
 * @author avalon566
 */
public class LocalSyncJobExecutor implements SyncJobExecutor {

    private List<SyncTask> syncTasks;

    @Override
    public final Reporter start(final List<SyncConfiguration> syncConfigurations) {
        LocalReporter reporter = new LocalReporter();
        syncTasks = new ArrayList<>(syncConfigurations.size());
        for (SyncConfiguration syncConfiguration : syncConfigurations) {
            SyncTask syncTask = SyncTaskFactory.createSyncJobInstance(syncConfiguration, reporter);
            syncTask.start();
            syncTasks.add(syncTask);
        }
        return reporter;
    }

    @Override
    public final void stop() {
        for (SyncTask syncTask : syncTasks) {
            syncTask.stop();
        }
    }

    @Override
    public final List<SyncTaskProgress> getProgresses() {
        List<SyncTaskProgress> result = new ArrayList<>();
        for (SyncTask syncTask : syncTasks) {
            result.add(syncTask.getProgress());
        }
        return result;
    }
}
