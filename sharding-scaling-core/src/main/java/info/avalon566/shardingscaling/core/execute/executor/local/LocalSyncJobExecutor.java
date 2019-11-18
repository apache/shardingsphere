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

package info.avalon566.shardingscaling.core.execute.executor.local;

import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.controller.ReportCallback;
import info.avalon566.shardingscaling.core.controller.SyncTaskProgress;
import info.avalon566.shardingscaling.core.synctask.SyncTask;
import info.avalon566.shardingscaling.core.execute.Event;
import info.avalon566.shardingscaling.core.execute.Reporter;
import info.avalon566.shardingscaling.core.execute.executor.SyncJobExecutor;
import info.avalon566.shardingscaling.core.synctask.SyncTaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local execute job executor.
 *
 * @author avalon566
 */
public class LocalSyncJobExecutor implements SyncJobExecutor {

    private List<SyncTask> syncTasks;

    private final LocalReporter reporter = new LocalReporter();

    private final Map<String, ReportCallback> reportCallbackMap = new ConcurrentHashMap<>();

    public LocalSyncJobExecutor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Event event = reporter.consumeEvent();
                    if (null != event) {
                        reportCallbackMap.get(event.getTaskId()).onProcess(event);
                    }
                }
            }
        }).start();
    }

    @Override
    public final Reporter start(final List<SyncConfiguration> syncConfigurations, final ReportCallback reportCallback) {
        syncTasks = new ArrayList<>(syncConfigurations.size());
        for (SyncConfiguration syncConfiguration : syncConfigurations) {
            reportCallbackMap.put(syncConfiguration.getTaskId(), reportCallback);
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
