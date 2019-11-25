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

import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.ReaderFactory;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;
import org.apache.shardingsphere.shardingscaling.core.util.DbMetaDataUtil;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * History data sync task group.
 *
 * @author avalon566
 */
public class HistoryDataSyncTaskGroup implements SyncTask {

    private final SyncConfiguration syncConfiguration;

    private final List<SyncTask> syncTasks = new LinkedList<>();

    public HistoryDataSyncTaskGroup(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    @Override
    public final void prepare() {
        List<SyncConfiguration> tableSliceConfigurations = split(syncConfiguration);
        for (SyncConfiguration each : tableSliceConfigurations) {
            SyncTask syncTask = new DefaultSyncTaskFactory().createHistoryDataSyncTask(each);
            syncTask.prepare();
            syncTasks.add(syncTask);
        }
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
                syncConfigurations.add(new SyncConfiguration(syncConfiguration.getConcurrency(),
                        sliceConfig, RdbmsConfiguration.clone(syncConfiguration.getWriterConfiguration())));
            }
        }
        return syncConfigurations;
    }

    @Override
    public final void start(final ReportCallback callback) {
        final List<Event> events = new LinkedList<>();
        for (final SyncTask each : syncTasks) {
            each.start(new ReportCallback() {

                @Override
                public void onProcess(final Event event) {
                    events.add(event);
                    if (syncTasks.size() == events.size()) {
                        //TODO check error
                        callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
                    }
                }
            });
        }
    }

    @Override
    public final void stop() {
        for (SyncTask each : syncTasks) {
            each.stop();
        }
    }

    @Override
    public final SyncProgress getProgress() {
        return new SyncProgress() { };
    }
}
