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
import info.avalon566.shardingscaling.core.job.MigrateProgress;
import info.avalon566.shardingscaling.core.job.sync.SyncJob;
import info.avalon566.shardingscaling.core.job.sync.executor.Reporter;
import info.avalon566.shardingscaling.core.job.sync.executor.SyncJobExecutor;
import info.avalon566.shardingscaling.core.job.sync.SyncJobFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Local sync job executor.
 *
 * @author avalon566
 */
public class LocalSyncJobExecutor implements SyncJobExecutor {

    private List<SyncJob> syncJobs;

    @Override
    public final Reporter start(final List<SyncConfiguration> syncConfigurations) {
        LocalReporter reporter = new LocalReporter();
        syncJobs = new ArrayList<>(syncConfigurations.size());
        for (SyncConfiguration syncConfiguration : syncConfigurations) {
            SyncJob syncJob = SyncJobFactory.createSyncJobInstance(syncConfiguration, reporter);
            syncJob.start();
            syncJobs.add(syncJob);
        }
        return reporter;
    }

    @Override
    public final void stop() {
        for (SyncJob syncJob : syncJobs) {
            syncJob.stop();
        }
    }

    @Override
    public final List<MigrateProgress> getProgresses() {
        List<MigrateProgress> result = new ArrayList<>();
        for (SyncJob syncJob : syncJobs) {
            result.add(syncJob.getProgress());
        }
        return result;
    }
}
