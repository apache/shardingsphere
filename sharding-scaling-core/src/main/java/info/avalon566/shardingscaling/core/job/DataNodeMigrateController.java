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

import info.avalon566.shardingscaling.core.config.RdbmsConfiguration;
import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.config.SyncType;
import info.avalon566.shardingscaling.core.job.sync.executor.Event;
import info.avalon566.shardingscaling.core.job.sync.executor.EventType;
import info.avalon566.shardingscaling.core.job.sync.executor.Reporter;
import info.avalon566.shardingscaling.core.job.sync.executor.SyncJobExecutor;
import info.avalon566.shardingscaling.core.job.sync.executor.local.LocalSyncJobExecutor;
import info.avalon566.shardingscaling.core.job.sync.RealtimeDataSyncJob;
import info.avalon566.shardingscaling.core.sync.reader.LogPosition;
import info.avalon566.shardingscaling.core.sync.reader.ReaderFactory;
import info.avalon566.shardingscaling.core.sync.util.DataSourceFactory;
import info.avalon566.shardingscaling.core.sync.util.DbMetaDataUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data node migrate controller, synchronize history data and realtime data.
 *
 * @author avalon566
 */
@Slf4j
public final class DataNodeMigrateController implements Runnable {

    private final SyncJobExecutor syncJobExecutor = new LocalSyncJobExecutor();

    private final SyncConfiguration syncConfiguration;

    public DataNodeMigrateController(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    /**
     * Start synchronize data.
     */
    public void start() {
        new Thread(this).start();
    }

    /**
     * Stop synchronize data.
     */
    public void stop() {
        syncJobExecutor.stop();
        //TODO skip remain job
    }

    /**
     * Get synchronize progress.
     *
     * @return migrate progress
     */
    public MigrateProgress getProgress() {
        List<MigrateProgress> result = syncJobExecutor.getProgresses();
        // if history data sync job, only return first migrate progress.
        // if realtime data sync job, there only one migrate progress.
        return result.get(0);
    }

    @Override
    public void run() {
        LogPosition position = new RealtimeDataSyncJob(syncConfiguration, null).preRun();
        syncHistoryData();
        syncRealtimeData(position);
    }

    private void syncHistoryData() {
        List<SyncConfiguration> configs = split(syncConfiguration);
        Reporter reporter = syncJobExecutor.start(configs);
        waitSlicesFinished(configs, reporter);
    }

    private List<SyncConfiguration> split(final SyncConfiguration syncConfiguration) {
        List<SyncConfiguration> syncConfigurations = new ArrayList<>();
        // split by table
        DataSource dataSource = DataSourceFactory.getDataSource(syncConfiguration.getReaderConfiguration().getDataSourceConfiguration());
        for (String tableName : new DbMetaDataUtil(dataSource).getTableNames()) {
            RdbmsConfiguration readerConfig = RdbmsConfiguration.clone(syncConfiguration.getReaderConfiguration());
            readerConfig.setTableName(tableName);
            // split by primary key range
            for (RdbmsConfiguration sliceConfig : ReaderFactory.newInstanceJdbcReader(readerConfig).split(syncConfiguration.getConcurrency())) {
                syncConfigurations.add(new SyncConfiguration(SyncType.TableSlice, syncConfiguration.getConcurrency(),
                        sliceConfig, RdbmsConfiguration.clone(syncConfiguration.getWriterConfiguration())));
            }
        }
        return syncConfigurations;
    }

    private void waitSlicesFinished(final List<SyncConfiguration> syncConfigurations, final Reporter reporter) {
        int counter = 0;
        boolean hasException = false;
        while (true) {
            Event event = reporter.consumeEvent();
            if (EventType.FINISHED == event.getEventType()) {
                counter++;
            }
            if (EventType.EXCEPTION_EXIT == event.getEventType()) {
                hasException = true;
                System.exit(1);
            }
            if (syncConfigurations.size() == counter) {
                log.info("history data sync finish");
                break;
            }
        }
    }

    private void syncRealtimeData(final LogPosition position) {
        syncConfiguration.setPosition(position);
        SyncConfiguration realConfiguration = new SyncConfiguration(
                SyncType.Realtime, syncConfiguration.getConcurrency(),
                syncConfiguration.getReaderConfiguration(),
                syncConfiguration.getWriterConfiguration());
        realConfiguration.setPosition(position);
        Reporter realtimeReporter = syncJobExecutor.start(Collections.singletonList(realConfiguration));
        while (true) {
            Event event = realtimeReporter.consumeEvent();
            if (EventType.FINISHED == event.getEventType()) {
                return;
            }
            if (EventType.EXCEPTION_EXIT == event.getEventType()) {
                System.exit(1);
            }
            if (EventType.REALTIME_SYNC_POSITION == event.getEventType()) {

            }
        }
    }
}
