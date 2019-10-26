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

package info.avalon566.shardingscaling.core.job;

import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.config.SyncType;
import info.avalon566.shardingscaling.core.job.schedule.Event;
import info.avalon566.shardingscaling.core.job.schedule.EventType;
import info.avalon566.shardingscaling.core.job.schedule.Reporter;
import info.avalon566.shardingscaling.core.job.schedule.standalone.InProcessScheduler;
import info.avalon566.shardingscaling.core.sync.reader.LogPosition;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;

/**
 * Database sync job.
 *
 * @author avalon566
 */
@Slf4j
public class DatabaseSyncJob {

    private final HistoryDataSyncer historyDataSyncer;

    private final RealtimeDataSyncer realtimeDataSyncer;

    private final Reporter reporter;

    private final SyncConfiguration syncConfiguration;

    public DatabaseSyncJob(final SyncConfiguration syncConfiguration, final Reporter reporter) {
        this.syncConfiguration = syncConfiguration;
        this.reporter = reporter;
        this.historyDataSyncer = new HistoryDataSyncer(syncConfiguration);
        this.realtimeDataSyncer = new RealtimeDataSyncer(syncConfiguration, reporter);

    }

    /**
     * Run.
     */
    public void run() {
        LogPosition position = realtimeDataSyncer.preRun();
        historyDataSyncer.run();
        syncConfiguration.setPosition(position);
        SyncConfiguration realConfiguration = new SyncConfiguration(
                SyncType.Realtime, syncConfiguration.getConcurrency(),
                syncConfiguration.getReaderConfiguration(),
                syncConfiguration.getWriterConfiguration());
        realConfiguration.setPosition(position);
        Reporter realtimeReporter = new InProcessScheduler().schedule(Collections.singletonList(realConfiguration));
        Event event = realtimeReporter.consumeEvent();
        if (EventType.FINISHED == event.getEventType()) {
            return;
        }
        if (EventType.EXCEPTION_EXIT == event.getEventType()) {
            System.exit(1);
        }
    }
}
