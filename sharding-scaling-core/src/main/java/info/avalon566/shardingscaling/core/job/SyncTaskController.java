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
import info.avalon566.shardingscaling.core.job.sync.RealtimeDataSyncTask;
import info.avalon566.shardingscaling.core.job.sync.executor.Event;
import info.avalon566.shardingscaling.core.job.sync.executor.EventType;
import info.avalon566.shardingscaling.core.job.sync.executor.SyncJobExecutor;
import info.avalon566.shardingscaling.core.job.sync.executor.local.LocalSyncJobExecutor;
import info.avalon566.shardingscaling.core.sync.reader.LogPosition;
import info.avalon566.shardingscaling.core.sync.reader.ReaderFactory;
import info.avalon566.shardingscaling.core.sync.util.DataSourceFactory;
import info.avalon566.shardingscaling.core.sync.util.DbMetaDataUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sync task controller, synchronize history data and realtime data.
 *
 * @author avalon566
 */
@Slf4j
public final class SyncTaskController implements ReportCallback, Runnable {

    private static final String STAGE_SYNC_HISTORY_DATA = "SYNC_HISTORY_DATA";

    private static final String STAGE_SYNC_REALTIME_DATA = "SYNC_REALTIME_DATA";

    private final SyncJobExecutor syncJobExecutor = new LocalSyncJobExecutor();

    private final SyncConfiguration syncConfiguration;

    private final Map<String, Object> migrateProgresses = new HashMap<>();

    private LogPosition startLogPosition;

    private String stage = STAGE_SYNC_HISTORY_DATA;

    public SyncTaskController(final SyncConfiguration syncConfiguration) {
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
    public SyncTaskProgress getProgress() {
        List<SyncTaskProgress> result = syncJobExecutor.getProgresses();
        // if history data sync job, only return first migrate progress.
        // if realtime data sync job, there only one migrate progress.
        return result.get(0);
    }

    @Override
    public void run() {
        startLogPosition = new RealtimeDataSyncTask(syncConfiguration, null).preRun();
        syncHistoryData();
    }

    @Override
    public void onProcess(final Event event) {
        migrateProgresses.put(event.getTaskId(), event);
        if (EventType.FINISHED == event.getEventType()) {
            boolean finished = true;
            for (Object each : migrateProgresses.values()) {
                if (null == each || EventType.FINISHED != ((Event) each).getEventType()) {
                    finished = false;
                }
            }
            if (finished) {
                log.info("data sync finish");
                if (STAGE_SYNC_HISTORY_DATA.equals(stage)) {
                    stage = STAGE_SYNC_REALTIME_DATA;
                    syncRealtimeData(startLogPosition);
                }
            }
        }
        if (EventType.EXCEPTION_EXIT == event.getEventType()) {
            System.exit(1);
        }
    }

    private void syncHistoryData() {
        List<SyncConfiguration> configs = split(syncConfiguration);
        migrateProgresses.clear();
        for (SyncConfiguration each : configs) {
            migrateProgresses.put(each.getTaskId(), null);
        }
        syncJobExecutor.start(configs, this);
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

    private void syncRealtimeData(final LogPosition position) {
        syncConfiguration.setPosition(position);
        SyncConfiguration realConfiguration = new SyncConfiguration(
                SyncType.Realtime, syncConfiguration.getConcurrency(),
                syncConfiguration.getReaderConfiguration(),
                syncConfiguration.getWriterConfiguration());
        realConfiguration.setPosition(position);
        migrateProgresses.clear();
        migrateProgresses.put(syncConfiguration.getTaskId(), null);
        syncJobExecutor.start(Collections.singletonList(realConfiguration), this);
    }
}
